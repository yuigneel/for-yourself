package com.yulgnier.center.common.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yulgnier.center.common.user.config.properties.CloudflareProperties;
import com.yulgnier.center.common.user.config.properties.LinkProperties;
import com.yulgnier.center.common.user.config.properties.MailProperties;
import com.yulgnier.center.common.user.config.properties.MiscellaneousProperties;
import com.yulgnier.center.common.user.mapper.UserMapper;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.*;
import com.yulgnier.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.user.api.model.enums.BanLevelEnum;
import com.yulgnier.center.user.api.model.enums.BusinessTypeEnum;
import com.yulgnier.center.user.api.model.enums.GenderEnum;
import com.yulgnier.center.common.user.model.vo.UserLoginResponseVO;
import com.yulgnier.center.common.user.service.UserService;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yulgnier.common.config.properties.JwtProperties;
import com.yulgnier.common.model.constants.AuthConstants;
import com.yulgnier.common.utils.*;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * 用户服务实现类
 *
 * @author yulgnier
 * @since 2023-09-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<UserMapper, CommonUser>
        implements UserService {    // 👈 必须加这一行！实现接口  行知道了！！窝是废物

    private final CloudflareProperties cloudflareProperties;
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final MiscellaneousProperties miscellaneousProperties;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Snowflake snowflake;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final LinkProperties linkProperties;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    /**
     * 获取邮箱验证码
     *
     * @param request 邮箱验证码请求参数
     * @return 邮箱验证码
     */
    @Override
    public String getEmailCode(EmailCodeRequestDTO request) {
        String receiveEmail = request.getEmail();
        //  检验是否人机
        log.debug("前端传来cloud flare的token{}", request.getCfTurnstileResponse());
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("邮箱{}：传来无效cloud flare令牌", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        //  检验邮箱格式是否正确
        if (!ValidateUtil.isValidEmail(receiveEmail)) {
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        //  检验业务是否符合参数
        if (request.getBusinessType() == null) {
            log.warn("邮箱{}：业务类型为空", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.INCOMPLETE_PARAMETERS, null);
        }
        if (request.getBusinessType() == BusinessTypeEnum.SEND_EMAIL)
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        String emailCodeKey = request.getBusinessType().getName() + AuthConstants.SEPARATOR + receiveEmail; //eg: 注册:example@163.com
        String code = generateCode();   // 生成6位随机验证码    あなたのことが大好きです。付き合ってください-愚人节快乐
        String emailCodeValue = code + AuthConstants.SEPARATOR + miscellaneousProperties.getEmailTryTimes(); //eg: 123456:6
        String emailSendCountKey = BusinessTypeEnum.SEND_EMAIL.getName() + AuthConstants.SEPARATOR + receiveEmail;
        log.info("用户申请获取邮箱验证码：email={}, businessType={}", receiveEmail, request.getBusinessType());
        //  检验该邮箱是否允许发送验证码
        Integer needWaitTime = getNeedWaitTime(emailSendCountKey);
        if (needWaitTime > 0) {
            log.debug("邮箱{}：已超过发送限制，请稍后再试", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_REQUEST_TOO_FREQUENT, needWaitTime);
        }
        //  检验验证码是否已经发送
        if (redisUtil.hasKey(emailCodeKey)) {
            log.debug("邮箱{}：已发送验证码，请勿重复发送", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ALREADY_SENT, null);
        }

        //  发送验证码
        //      将邮箱和业务作为 key，验证码:剩余试错次数 作为 value 保存到缓存中
        try {
            redisUtil.set(emailCodeKey, emailCodeValue, miscellaneousProperties.getEmailExpireMinutes(), TimeUnit.MINUTES); // 保存验证码到缓存，有效期 5 分钟
        } catch (Exception e) {
            log.error("缓存保存失败！", e);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        log.info("开始向邮箱{}发送验证码", receiveEmail);
        try {
            //  核心：构建并发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());       // 发件人（你的 163 邮箱）
            message.setTo(receiveEmail);      // 收件人（前端传的真实邮箱）
            message.setSubject(request.getBusinessType().getName() + "验证码通知"); // 邮件标题
            message.setText("您的验证码是：" + code + "，" + miscellaneousProperties.getEmailExpireMinutes() + "分钟内有效！"); // 邮件内容
            //  执行发送！！！
            javaMailSender.send(message);
            log.info("验证码发送成功！邮箱：{}", receiveEmail);
            return "✅ 发送成功！验证码已发送至邮箱：" + receiveEmail;
        } catch (Exception e) {
            log.error("邮件发送失败！", e);
            // 5.3 删除 key
            try {
                redisUtil.delete(emailCodeKey);
            } catch (Exception ex) {
                log.error("缓存删除失败！", ex);
                throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
            }
            throw new ForYourselfException(ResultCodeEnum.THIRD_PARTY_SERVICE_ERROR, "❌ 发送失败!!!");
        }
    }

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return jwt 令牌
     */
    @Override
    public String register(UserRegisterRequestDTO request) {
        String nickname = request.getNickname();
        log.info("用户{}申请注册", nickname);
        //  检验是否人机
        log.debug("前端传来cloud flare的token{}", request.getCfTurnstileResponse());
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", nickname);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        //  检查昵称格式
        if (!ValidateUtil.isValidUsername(nickname)) {
            log.debug("用户{}：昵称格式错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
        }
        //  检查昵称是否已存在
        CommonUser user = userMapper.selectOneByNicknameIgnoreLogicDelete(nickname);
        if (user != null) {
            log.debug("用户{}：昵称已存在", nickname);
            throw new ForYourselfException(ResultCodeEnum.USERNAME_ALREADY_EXISTS, null);
        }
        //  检验邮箱格式是否正确
        String userEmail = request.getEmail();
        if (!ValidateUtil.isValidEmail(userEmail)) {
            log.debug("用户{}：邮箱格式错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        //  检查邮箱是否已注册
        user = userMapper.selectOneByEmailIgnoreLogicDelete(request.getEmail());
        if (user != null) {
            log.debug("用户{}：邮箱已注册", nickname);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_ALREADY_EXISTS, null);
        }
        //  检查密码
        if (!ValidateUtil.isValidPassword(request.getPassword())) {
            log.debug("用户{}：密码格式错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        }
        //  是否有日期，有检查，无，跳过
        if (request.getBirthday() != null) {
            if (request.getBirthday().isAfter(LocalDate.now())) {
                log.debug("用户{}：生日格式错误", nickname);
                throw new ForYourselfException(ResultCodeEnum.DATE_FORMAT_ERROR, null);
            }
        }
        //  检查是否有性别输入，有跳过，无设为默认未知
        if (request.getGenderEnum() == null) {
            request.setGenderEnum(GenderEnum.UNKNOWN);
        }
        //  检查邮箱和验证码是否匹配
        String emailCodeKey = BusinessTypeEnum.REGISTER.getName() + AuthConstants.SEPARATOR + userEmail;
        String emailCodeValue = redisUtil.get(emailCodeKey);
        if (emailCodeValue == null) {
            log.debug("用户{}：验证码已过期，请重新获取", nickname);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }
        String code = null;
        Integer remainTimes = null;
        try {
            code = emailCodeValue.split(AuthConstants.SEPARATOR)[0];
            remainTimes = Integer.parseInt(emailCodeValue.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误，请检查！", nickname);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        if (!checkCode(request.getCode(), userEmail, emailCodeKey, code, remainTimes)) {
            log.debug("用户{}：验证码错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, remainTimes - 1);
        }
        //  录入数据库
        //      生成密码密文
        String encryptedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        //      创建用户
        user = new CommonUser();
        //      完善用户信息
        long uid = snowflake.nextId();
        LocalDate joinDate = LocalDate.now();
        //      组装用户
        BeanUtil.copyProperties(request, user);
        user.setUid(uid);
        user.setPassword(encryptedPassword);
        user.setJoinDate(joinDate);
        //      保存用户
        try {
            userMapper.insert(user);
            log.info("用户{}：保存用户成功", nickname);
        } catch (Exception e) {
            log.error("用户{}：保存用户失败", nickname, e);
            throw new ForYourselfException(ResultCodeEnum.SAVE_USER_FAILED, null);
        }
        //  生成 jwt 令牌
        return generateToken(nickname, uid);
    }

    /**
     * 登录
     *
     * @param request 登录请求参数
     * @return jwt 令牌
     */
    @Override
    public UserLoginResponseVO login(UserLoginRequestDTO request) {
        String name = request.getName();
        UserLoginResponseVO userLoginResponseVO = new UserLoginResponseVO();
        log.debug("用户{}：开始登录", name);
        // 人机检测
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", name);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 检验密码格式
        if (!ValidateUtil.isValidPassword(request.getPw()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        // switch 到不同的登录方式
        switch (request.getLoginType()) {
            // 用户名登录
            case USERNAME -> {
                // 检验用户名格式
                if (!ValidateUtil.isValidUsername(name)) {
                    throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
                }
                CommonUser user = userMapper.selectOneByNicknameIgnoreLogicDelete(name);
                if (user == null) {
                    log.debug("用户{}：用户名不存在", name);
                    throw new ForYourselfException(ResultCodeEnum.USERNAME_NOT_FOUND, null);
                }
                if (!bCryptPasswordEncoder.matches(request.getPw(), user.getPassword())) {
                    log.debug("用户{}：账户或密码错误", name);
                    throw new ForYourselfException(ResultCodeEnum.ACCOUNT_OR_PASSWORD_ERROR, null);
                }
                userLoginResponseVO.setResultCodeEnum(ResultCodeEnum.USER_NORMAL_LOGIN);
                if (user.getIsDeleted() == 1) {
                    log.info("用户{}：账户已注销,正在恢复。。。", name);
                    int i = userMapper.restoreUserIgnoreLogicDelete(user);
                    if (i == 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                    log.info("用户{}：账户已恢复", name);
                    userLoginResponseVO.setResultCodeEnum(ResultCodeEnum.USER_CANCELLED_UNDO_LOGIN);
                }
                userLoginResponseVO.setToken(generateToken(name, user.getUid()));
                return userLoginResponseVO;
            }
            // 邮箱登录
            case EMAIL -> {
                // 检验邮箱格式
                if (!ValidateUtil.isValidEmail(name)) {
                    throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
                }
                CommonUser user = userMapper.selectOneByEmailIgnoreLogicDelete(name);
                if (user == null) {
                    log.debug("用户{}：邮箱不存在", name);
                    throw new ForYourselfException(ResultCodeEnum.EMAIL_NOT_FOUND, null);
                }
                if (!bCryptPasswordEncoder.matches(request.getPw(), user.getPassword())) {
                    log.debug("用户{}：账户或密码错误", name);
                    throw new ForYourselfException(ResultCodeEnum.ACCOUNT_OR_PASSWORD_ERROR, null);
                }
                userLoginResponseVO.setResultCodeEnum(ResultCodeEnum.USER_NORMAL_LOGIN);
                if (user.getIsDeleted() == 1) {
                    log.info("用户{}：账户已注销,正在恢复。。。", name);
                    int i = userMapper.restoreUserIgnoreLogicDelete(user);
                    if (i == 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                    log.info("用户{}：账户已恢复", name);
                    userLoginResponseVO.setResultCodeEnum(ResultCodeEnum.USER_CANCELLED_UNDO_LOGIN);
                }
                userLoginResponseVO.setToken(generateToken(name, user.getUid()));
                return userLoginResponseVO;
            }
            // 手机号登录
            case PHONE -> {
                // TODO
                log.warn("用户{}：手机号登录未实现", name);
                throw new ForYourselfException(ResultCodeEnum.TODO, null);
            }
            // UID 登录
            case UID -> {
                CommonUser user = userMapper.selectOneByUidIgnoreLogicDelete(Long.valueOf(name));
                if (user == null) {
                    log.debug("用户{}：UID不存在", name);
                    throw new ForYourselfException(ResultCodeEnum.UID_NOT_FOUND, null);
                }
                if (!bCryptPasswordEncoder.matches(request.getPw(), user.getPassword())) {
                    log.debug("用户{}：账户或密码错误", name);
                    throw new ForYourselfException(ResultCodeEnum.ACCOUNT_OR_PASSWORD_ERROR, null);
                }
                userLoginResponseVO.setResultCodeEnum(ResultCodeEnum.USER_NORMAL_LOGIN);
                if (user.getIsDeleted() == 1) {
                    log.info("用户{}：账户已注销,正在恢复。。。", name);
                    int i = userMapper.restoreUserIgnoreLogicDelete(user);
                    if (i == 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                    log.info("用户{}：账户已恢复", name);
                    userLoginResponseVO.setResultCodeEnum(ResultCodeEnum.USER_CANCELLED_UNDO_LOGIN);
                }
                userLoginResponseVO.setToken(generateToken(name, user.getUid()));
                return userLoginResponseVO;
            }

            default -> {
                log.warn("用户{}：登录方式错误", name);
                throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
            }
        }
    }

    /**
     * 用户注销
     * <p>校验流程：Cloudflare人机验证 → 用户名/邮箱/密码格式校验 → 验证码校验 → 用户名邮箱匹配校验 → 密码校验 → 逻辑删除</p>
     *
     * @param request 用户注销请求DTO，包含昵称、邮箱、密码、验证码和Cloudflare令牌
     */
    @Override
    public void cancel(UserCancelRequestDTO request) {
        String nickname = request.getNickname();
        log.info("用户{}：开始注销", nickname);
        // 检验是否人机
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", nickname);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 检验用户名格式
        if (!ValidateUtil.isValidUsername(nickname)) {
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
        }
        // 检验邮箱格式
        if (!ValidateUtil.isValidEmail(request.getEmail())) {
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        // 检验密码格式
        if (!ValidateUtil.isValidPassword(request.getPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        }
        // 检验验证码是否正确 yulgnier: (key=用户注销:邮箱 value=验证码:剩余尝试次数) (●ˇ∀ˇ●)
        //   拼接key
        String key = BusinessTypeEnum.CANCEL_USER.getName() + AuthConstants.SEPARATOR + request.getEmail();
        //   获取value，为null，抛错误
        String value = redisUtil.get(key);
        if (value == null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }
        //   分离验证码和剩余尝试次数
        String code = null;
        Integer remainTimes = null;
        try {
            code = value.split(AuthConstants.SEPARATOR)[0];
            remainTimes = Integer.valueOf(value.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误，请检查！", nickname);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        //   匹配验证码 (包装成方法，返回布尔值，自动执行减次数等措施)
        if (!checkCode(request.getCode(), request.getEmail(), key, code, remainTimes)) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, remainTimes - 1);
        }
        // 构造条件：昵称 + 邮箱 匹配
        LambdaQueryWrapper<CommonUser> lambdaQueryWrapper = new LambdaQueryWrapper<CommonUser>()
                .eq(CommonUser::getNickname, nickname)
                .eq(CommonUser::getEmail, request.getEmail());

        // 只查一次数据库 获取用户
        CommonUser user = this.getOne(lambdaQueryWrapper);

        // 用户不存在 → 抛异常
        if (user == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }

        // 校验密码
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        }

        // 全部匹配 → 执行逻辑删除（自动走 MP 逻辑删除，更新 isDeleted=1）
        this.remove(lambdaQueryWrapper);
        log.info("用户{}：注销成功！", nickname);
    }

    /**
     * 用户找回密码
     * <p>校验流程：Cloudflare人机验证 → 邮箱格式校验 → 验证码校验 → 查询用户(忽略逻辑删除) → 生成随机密码 → 加密更新</p>
     *
     * @param request 找回密码请求DTO，包含邮箱、验证码和Cloudflare令牌
     * @return 新生成的随机密码(明文)
     */
    @Override
    public String forgetPassword(UserForgetPasswordRequestDTO request) {
        // 提取常用变量
        String email = request.getEmail();
        // 检验人机
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", email);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 检验邮箱格式
        if (!ValidateUtil.isValidEmail(email)) {
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        // 验证验证码
        String key = BusinessTypeEnum.FORGET_PASSWORD.getName() + AuthConstants.SEPARATOR + email;
        String value = redisUtil.get(key);
        if (value == null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }
        String code = null;
        Integer remainTimes = null;
        try {
            code = value.split(AuthConstants.SEPARATOR)[0];
            remainTimes = Integer.valueOf(value.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误，请检查！", email);
        }
        if (!checkCode(request.getVerificationCode(), email, key, code, remainTimes)) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, remainTimes - 1);
        }
        // 获取用户
        CommonUser user = userMapper.selectOneByEmailIgnoreLogicDelete(email);
        if (user == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }
        log.info("用户{}：开始找回密码", user.getNickname());
        // 生成随机的密码
        String password = generateRandomPassword(16);
        // 密码重置
        user.setPassword(bCryptPasswordEncoder.encode(password));
        try {
            userMapper.updatePasswordByUidIgnoreLogicDelete(user.getUid(), user.getPassword());
        } catch (Exception e) {
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
        log.info("用户{}：密码重置成功", user.getNickname());
        // 返回密码
        return password;
    }

    /**
     * 更新用户信息
     *
     * @param request 用户信息更新请求参数，包含昵称、性别和生日
     */
    @Override
    public void updateUserInfo(UserUpdateInfoRequestDTO request) {
        // 检查昵称格式
        if (!ValidateUtil.isValidUsername(request.getNickname())) {
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
        }
        // 检查生日格式
        if (request.getBirthday() != null) {
            if (request.getBirthday().isAfter(LocalDate.now())) {
                throw new ForYourselfException(ResultCodeEnum.DATE_FORMAT_ERROR, null);
            }
        }
        // 获取用户
        LambdaQueryWrapper<CommonUser> eq = new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getUid, UserContextUtil.getUid());
        CommonUser user = this.getOne(eq);
        if (user == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }

        // 检查是否有更新项（不能一个都不更新和数据库一样）
        boolean hasChanges = false;

        // 检查昵称是否变化（必填字段，直接比较）
        if (!request.getNickname().equals(user.getNickname())) {
            hasChanges = true;
        }

        // 检查性别是否变化（可选字段）
        if (request.getGenderEnum() == null) {
            // 前端没传性别，但数据库有值 → 用户想清空性别
            if (user.getGender() != null) {
                hasChanges = true;
            }
        } else {
            // 前端传了性别，比较值是否不同
            if (!request.getGenderEnum().equals(user.getGender())) {
                hasChanges = true;
            }
        }

        // 检查生日是否变化（可选字段）
        if (request.getBirthday() == null) {
            // 前端没传生日，但数据库有值 → 用户想清空生日
            if (user.getBirthday() != null) {
                hasChanges = true;
            }
        } else {
            // 前端传了生日，比较值是否不同
            if (!request.getBirthday().equals(user.getBirthday())) {
                hasChanges = true;
            }
        }

        // 如果没有任何变化，抛出异常
        if (!hasChanges) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }

        log.info("用户{}：开始更新用户信息", user.getNickname());

        LambdaUpdateWrapper<CommonUser> yulgnier = new LambdaUpdateWrapper<CommonUser>().eq(CommonUser::getId, user.getId())
                .set(CommonUser::getNickname, request.getNickname())
                .set(CommonUser::getBirthday, request.getBirthday())
                .set(CommonUser::getGender, request.getGenderEnum().getCode());
        try {
            this.update(yulgnier);
            log.info("用户{}：更新用户信息成功", user.getNickname());
        } catch (Exception e) {
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
    }

    /**
     * 用户换绑邮箱
     * <p>校验流程：查询用户 → 新邮箱格式校验 → 验证码校验 → 更新邮箱</p>
     *
     * @param request 换绑邮箱请求DTO，包含新邮箱、用户密码和新邮箱验证码
     */
    @Override
    public void changeEmail(UserChangeEmailRequestDTO request) {
        CommonUser user = this.getOne(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getUid, UserContextUtil.getUid()));
        if (user == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }
        log.info("用户{}：开始换绑邮箱", user.getNickname());
        // 检验邮箱格式
        if (!ValidateUtil.isValidEmail(request.getNewEmail())) {
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        if (user.getEmail().equals(request.getNewEmail())) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }
        // 密码
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        }
        // 验证码
        String key = BusinessTypeEnum.BIND_EMAIL.getName() + AuthConstants.SEPARATOR + request.getNewEmail();
        String value = redisUtil.get(key);
        if (value == null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }
        String code = null;
        Integer remainTimes = null;
        try {
            code = value.split(AuthConstants.SEPARATOR)[0];
            remainTimes = Integer.valueOf(value.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误，请检查！", user.getNickname());
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        if (!checkCode(request.getVerificationCode(), request.getNewEmail(), key, code, remainTimes)) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, remainTimes - 1);
        }
        // 更新用户
        LambdaUpdateWrapper<CommonUser> set = new LambdaUpdateWrapper<CommonUser>().eq(CommonUser::getId, user.getId()).set(CommonUser::getEmail, request.getNewEmail());
        try {
            this.update(set);
            log.info("用户{}：换绑邮箱成功", user.getNickname());
        } catch (Exception e) {
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
    }

    /**
     * 获取用户信息
     * <p>根据当前登录用户的UID查询并返回用户详细信息</p>
     *
     * @return 用户信息响应VO，包含用户的基本信息
     */
    @Override
    public CommonUserInfoResponseVO getUserInfo() {
        LambdaQueryWrapper<CommonUser> eq = new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getUid, UserContextUtil.getUid());
        Optional<CommonUser> oneOpt = this.getOneOpt(eq);
        if (oneOpt.isEmpty()) throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        // 用Hutool工具包拷贝到VO
        return BeanUtil.copyProperties(oneOpt.get(), CommonUserInfoResponseVO.class);
    }

    /**
     * 用户修改密码
     * <p>校验流程：新旧密码一致性校验 → 密码格式校验 → 查询用户 → 原密码验证 → 加密更新密码</p>
     * <p>使用 LambdaUpdateWrapper 局部更新，避免触发 update_time 自动更新</p>
     *
     * @param request 修改密码请求DTO，包含原始密码和新密码
     */
    @Override
    public void updatePassword(UserUpdatePasswordRequestDTO request) {
        // 先验证新旧密码是否相同（避免不必要的数据库查询）
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }

        // 验证两个密码是否符合规范
        if (!ValidateUtil.isValidPassword(request.getNewPassword()) || !ValidateUtil.isValidPassword(request.getOldPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        }

        // 获取用户
        CommonUser commonUser = this.getOne(new LambdaQueryWrapper<CommonUser>()
                .eq(CommonUser::getUid, UserContextUtil.getUid()));

        if (commonUser == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }

        // 验证原密码
        if (!bCryptPasswordEncoder.matches(request.getOldPassword(), commonUser.getPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        }

        log.info("用户{}：开始修改密码", commonUser.getNickname());

        // 修改密码
        LambdaUpdateWrapper<CommonUser> updateWrapper = new LambdaUpdateWrapper<CommonUser>()
                .eq(CommonUser::getUid, commonUser.getUid()) // 条件：根据UID更新
                .set(CommonUser::getPassword, bCryptPasswordEncoder.encode(request.getNewPassword())); // 只更新密码

        // 执行局部更新（不会更新create_time/update_time，数据库自动生效！）
        this.update(updateWrapper);
        log.info("用户{}：修改密码成功", commonUser.getNickname());
    }

    /**
     * 分页查询普通用户列表（仅管理员可调用）
     * <p>内部会校验调用者身份，非管理员直接拒绝</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public IPage<CommonUserInfoResponseVO> pageUsers(CommonUserPageQueryDTO query) {
        //1. 身份校验，必须是admin传来的请求
        String token = UserContextUtil.getLink();
        if (token == null) throw new ForYourselfException(ResultCodeEnum.ILLEGAL_ACCESS, "我容易吗我，go out!");
        if (!InnerFlexibleTokenSecurityUtil.verifyToken(linkProperties.getAdminCommon().getSecretKey(), linkProperties.getAdminCommon().getExpireMilliseconds(), token))
            throw new ForYourselfException(ResultCodeEnum.ILLEGAL_ACCESS, "我容易吗我，go out!");
        // 2. 创建分页对象
        Page<CommonUser> page = new Page<>(query.getCurrent(), query.getSize());

        // 3. 调用 Mapper 的分页查询方法（MyBatis-Plus 自动处理分页）
        IPage<CommonUser> userPage = userMapper.selectPageByCondition(page, query);

        // 4. 转换为 VO
        return userPage.convert(user -> BeanUtil.copyProperties(user, CommonUserInfoResponseVO.class));
    }

    /**
     * 根据UID获取普通用户信息
     * <p>需要验证内部灵活Token，确保访问合法性</p>
     *
     * @param uid 用户UID
     * @return 普通用户信息VO
     * @throws ForYourselfException 当Token无效、访问非法或用户不存在时抛出异常
     */
    @Override
    public CommonUserInfoResponseVO getOneById(Long uid) {
        String token = UserContextUtil.getLink();
        if (token == null) throw new ForYourselfException(ResultCodeEnum.ILLEGAL_ACCESS, "我容易吗我，go out!");
        if (!InnerFlexibleTokenSecurityUtil.verifyToken(linkProperties.getAdminCommon().getSecretKey(), linkProperties.getAdminCommon().getExpireMilliseconds(), token))
            throw new ForYourselfException(ResultCodeEnum.ILLEGAL_ACCESS, "我容易吗我，go out!");
        CommonUser one = this.getOne(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getUid, uid));
        if (one == null) throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        return BeanUtil.copyProperties(one, CommonUserInfoResponseVO.class);
    }

    /**
     * 管理员修改普通用户账号状态
     * <p>校验流程：验证管理员内部访问令牌 → 执行状态更新操作</p>
     *
     * @param request 状态更新请求参数，包含普通用户UID和新的账户状态
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#ILLEGAL_ACCESS} - 管理员访问令牌缺失或验证失败</li>
     *                                <li>{@link ResultCodeEnum#DATABASE_SERVICE_ERROR} - 数据库更新失败</li>
     *                              </ul>
     */
    @Override
    public void updateCommonUserStatus(com.yulgnier.center.user.api.model.dto.CommonUserStatusUpdateRequestDTO request) {
        String token = UserContextUtil.getLink();
        if (token == null) throw new ForYourselfException(ResultCodeEnum.ILLEGAL_ACCESS, "我容易吗我，go out!");
        if (!InnerFlexibleTokenSecurityUtil.verifyToken(linkProperties.getAdminCommon().getSecretKey(), linkProperties.getAdminCommon().getExpireMilliseconds(), token))
            throw new ForYourselfException(ResultCodeEnum.ILLEGAL_ACCESS, "我容易吗我，go out!");
        boolean update = this.update(new LambdaUpdateWrapper<CommonUser>().eq(CommonUser::getUid, request.getUid())
                .set(CommonUser::getAccountStatus, request.getNewStatus())
                .set(CommonUser::getUpdateBy, UserContextUtil.getUid()));
        if (!update) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
    }


    //===================================内部方法===================================

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }

    /**
     * 检查邮箱是否允许发送验证码
     *
     * @return 还剩余多少时间解冻
     */
    private Integer getNeedWaitTime(String key) {
        /*
            最大时间-计划冻结时间=计划剩余时间
            最大时间-实际冻结时间=实际剩余时间

         */
        //  提取计划冻结时间
        try {
            String value = redisUtil.get(key); //eg: A:1440
            Integer needTime = value == null ? 0 : Integer.valueOf(value.split(AuthConstants.SEPARATOR)[1]);
            //  获得实际剩余时间
            Long ttl = redisUtil.getTtl(key, TimeUnit.MINUTES);
            ttl = ttl <= 0 ? 0 : ttl;
            //  计算实际冻结的时间（最大时间-实际剩余时间）
            long actualTime = BanLevelEnum.LEVEL_S.getCode() - ttl;
            //  计算并返回还剩多长冻结时间（计划冻结时间-实际冻结时间）
            return needTime - (int) actualTime;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("获取剩余冻结时间失败", e);
            throw new ForYourselfException(ResultCodeEnum.SYSTEM_EXECUTION_ERROR, null);
        }
    }

    /**
     * 标记该邮箱因验证码错误被冻结（等级自动升级）
     *
     * @param key 业务名称+分隔符+邮箱
     */
    private void markEmailAsCaptchaFreeze(String key) {
        // 🔥 核心优化：只调用1次枚举，全部提取到局部变量（后续直接用，不重复调用）
        String S = BanLevelEnum.LEVEL_S.getName();
        String A = BanLevelEnum.LEVEL_A.getName();
        String B = BanLevelEnum.LEVEL_B.getName();
        String C = BanLevelEnum.LEVEL_C.getName();
        String D = BanLevelEnum.LEVEL_D.getName();
        String E = BanLevelEnum.LEVEL_E.getName();
        String F = BanLevelEnum.LEVEL_F.getName();
        String G = BanLevelEnum.LEVEL_G.getName();

        Integer timeS = BanLevelEnum.LEVEL_S.getCode();
        Integer timeA = BanLevelEnum.LEVEL_A.getCode();
        Integer timeB = BanLevelEnum.LEVEL_B.getCode();
        Integer timeC = BanLevelEnum.LEVEL_C.getCode();
        Integer timeD = BanLevelEnum.LEVEL_D.getCode();
        Integer timeE = BanLevelEnum.LEVEL_E.getCode();
        Integer timeF = BanLevelEnum.LEVEL_F.getCode();
        Integer timeG = BanLevelEnum.LEVEL_G.getCode();

        // 1. 安全获取缓存，防空指针
        String value = redisUtil.get(key);
        String currentBanLevel = (value == null) ? "x" : value.split(AuthConstants.SEPARATOR)[0];

        // 2. switch 必须用【字符串字面量】（Java语法强制要求，解决报错）
        // 内部直接用上面提取的变量，无任何冗余调用
        switch (currentBanLevel) {
            case "S" -> redisUtil.set(key, S + AuthConstants.SEPARATOR + timeS, timeS, TimeUnit.MINUTES);
            case "A" -> redisUtil.set(key, S + AuthConstants.SEPARATOR + timeS, timeS, TimeUnit.MINUTES);
            case "B" -> redisUtil.set(key, A + AuthConstants.SEPARATOR + timeA, timeS, TimeUnit.MINUTES);
            case "C" -> redisUtil.set(key, B + AuthConstants.SEPARATOR + timeB, timeS, TimeUnit.MINUTES);
            case "D" -> redisUtil.set(key, C + AuthConstants.SEPARATOR + timeC, timeS, TimeUnit.MINUTES);
            case "E" -> redisUtil.set(key, D + AuthConstants.SEPARATOR + timeD, timeS, TimeUnit.MINUTES);
            case "F" -> redisUtil.set(key, E + AuthConstants.SEPARATOR + timeE, timeS, TimeUnit.MINUTES);
            case "G" -> redisUtil.set(key, F + AuthConstants.SEPARATOR + timeF, timeS, TimeUnit.MINUTES);
            default -> redisUtil.set(key, G + AuthConstants.SEPARATOR + timeG, timeS, TimeUnit.MINUTES);
        }
    }

    /**
     * 生成jwt 令牌
     *
     * @param nickname 昵称（用于打日志）
     * @param uid      实际存入信息
     */
    private String generateToken(String nickname, Long uid) {
        HashMap<String, Object> loadHashMap = new HashMap<>(Map.of(AuthConstants.UID_KEY, uid));
        try {
            return jwtUtil.generateToken(loadHashMap, jwtProperties.getExpireHour(), TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("用户{}：生成jwt令牌失败", nickname, e);
            throw new ForYourselfException(ResultCodeEnum.SYSTEM_EXECUTION_ERROR, null);
        }
    }

    /**
     * 检验验证码是否正确
     *
     * @param code        用户输入的验证码
     * @param email       邮箱
     * @param key         业务名称+分隔符+邮箱
     * @param trueCode    缓存中的验证码
     * @param remainTimes 缓存中的剩余尝试次数
     * @return true:验证码正确
     */
    private boolean checkCode(String code, String email, String key, String trueCode, Integer remainTimes) {
        // 验证码正确，返回true
        if (code.equals(trueCode)) return true;
        // 验证码错误，尝试次数减一
        remainTimes -= 1;
        // 存入缓存
        String value = trueCode + AuthConstants.SEPARATOR + remainTimes;
        //    如果时间不足一个单位，则设置一秒
        long time = redisUtil.getTtl(key, TimeUnit.MINUTES);
        if (time <= 0) {
            redisUtil.set(key, value, 1, TimeUnit.SECONDS);
        } else redisUtil.set(key, value, time, TimeUnit.MINUTES);
        // 如果尝试次数为0，则开始冻结
        if (remainTimes <= 0) {
            String BanYULGNIERKey = BusinessTypeEnum.SEND_EMAIL.getName() + AuthConstants.SEPARATOR + email;
            markEmailAsCaptchaFreeze(BanYULGNIERKey);
            // 删除验证码的缓存
            redisUtil.delete(key);
        }
        // 返回false
        return false;
    }

    /**
     * 生成符合规则的随机密码
     * <p>密码规则：8-32位，必须包含字母、数字和特殊符号，不允许首尾空格</p>
     * <p>生成策略：确保至少包含1 个大写字母、1个小写字母、1 个数字、1个特殊符号，其余字符随机填充</p>
     *
     * @param length 密码长度（默认16位）
     * @return 符合规则的随机密码
     */
    private String generateRandomPassword(int length) {
        // 定义字符集
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";      // 大写字母
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";      // 小写字母
        String digits = "0123456789";                          // 数字
        String specialChars = "!@#$%^&*()-_=+[]{}|;:',.<>?/"; // 特殊符号

        Random random = new Random();
        StringBuilder password = new StringBuilder(length);

        // 第一步：确保每种类型至少有一个字符（满足密码强度要求）
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));   // 至少1个大写字母
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));   // 至少1个小写字母
        password.append(digits.charAt(random.nextInt(digits.length())));         // 至少1个数字
        password.append(specialChars.charAt(random.nextInt(specialChars.length()))); // 至少1个特殊符号

        // 第二步：剩余位置从所有字符集中随机选择
        String allChars = upperCase + lowerCase + digits + specialChars;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 第三步：打乱字符顺序（避免前4位固定是某种类型）
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // 交换位置
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        return new String(passwordArray);
    }

}

