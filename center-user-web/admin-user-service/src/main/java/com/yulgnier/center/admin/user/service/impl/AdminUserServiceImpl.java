package com.yulgnier.center.admin.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yulgnier.center.admin.user.config.properties.CloudflareProperties;
import com.yulgnier.center.admin.user.config.properties.MailProperties;
import com.yulgnier.center.admin.user.config.properties.MiscellaneousProperties;
import com.yulgnier.center.admin.user.model.domain.AdminUser;
import com.yulgnier.center.admin.user.model.dto.*;
import com.yulgnier.center.admin.user.model.enums.BanLevelEnum;
import com.yulgnier.center.admin.user.model.enums.BusinessTypeEnum;
import com.yulgnier.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yulgnier.center.admin.user.model.vo.AdminUserLoginResponseVO;
import com.yulgnier.center.admin.user.service.AdminUserService;
import com.yulgnier.center.admin.user.mapper.AdminUserMapper;
import com.yulgnier.common.config.properties.JwtProperties;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.constants.AuthConstants;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu_Lgnier
 * @description 针对表【t_admin_user(管理员用户基础信息表)】的数据库操作Service实现
 * @createDate 2026-04-18 16:37:15
 * 这个类被逻辑删除后，并不允许像普通用户那样靠登录来取消，所以不需要自己构建mapper，用MybatisPlus默认的方法就行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser>
        implements AdminUserService {

    private final CloudflareProperties cloudflareProperties;
    private final MailProperties mailProperties;
    private final MiscellaneousProperties miscellaneousProperties;
    private final JavaMailSender javaMailSender;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtProperties jwtProperties;

    /**
     * 获取邮箱验证码
     * <p>业务流程：</p>
     * <ol>
     *   <li>验证 Cloudflare Turnstile 人机验证令牌</li>
     *   <li>校验邮箱格式合法性</li>
     *   <li>检查邮箱发送频率限制（防止频繁请求）</li>
     *   <li>检查是否已存在未过期的验证码</li>
     *   <li>生成6位随机数字验证码并存入Redis缓存</li>
     *   <li>通过邮件服务发送验证码到目标邮箱</li>
     * </ol>
     * <p>异常处理：若邮件发送失败，会自动清理Redis中的验证码缓存</p>
     *
     * @param request 邮箱验证码请求参数，包含邮箱地址、业务类型、Cloudflare验证响应
     * @return 发送成功提示消息，格式："✅ 发送成功！验证码已发送至邮箱：{email}"
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_VERIFICATION_FAILED} - 人机验证失败</li>
     *                                <li>{@link ResultCodeEnum#EMAIL_FORMAT_ERROR} - 邮箱格式错误</li>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_REQUEST_TOO_FREQUENT} - 请求过于频繁，需等待冷却时间</li>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_ALREADY_SENT} - 验证码已发送且未过期</li>
     *                                <li>{@link ResultCodeEnum#CACHE_SERVICE_ERROR} - Redis缓存服务异常</li>
     *                                <li>{@link ResultCodeEnum#THIRD_PARTY_SERVICE_ERROR} - 邮件发送服务异常</li>
     *                              </ul>
     */
    @Override
    public String getEmailCode(EmailCodeRequestDTO request) {
        String receiveEmail = request.getEmail();
        log.info("用户申请获取邮箱验证码：email={}, businessType={}", receiveEmail, request.getBusinessType());
        // 检查人机
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("邮箱{}：传来无效cloud flare令牌", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 检查业务类型（为空已经判断过了，还有不能为0）
        if (request.getBusinessType() == BusinessTypeEnum.SEND_EMAIL)
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        // 检查邮箱
        //    检查邮箱格式
        if (!ValidateUtil.isValidEmail(receiveEmail)) {
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        //    检查邮箱是否允许发送验证码
        String sendEmailKey = BusinessTypeEnum.SEND_EMAIL.getName() + AuthConstants.SEPARATOR + receiveEmail;
        if (getNeedWaitTime(sendEmailKey) > 0) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_REQUEST_TOO_FREQUENT, null);
        }
        //    检查是否已经发送了验证码
        if (RedisUtil.get(sendEmailKey) != null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ALREADY_SENT, null);
        }
        // 发送验证码
        //    创建key和value
        String emailCodeKey = request.getBusinessType().getName() + AuthConstants.SEPARATOR + receiveEmail;
        String emailCode = generateCode();
        String emailCodeValue = emailCode + AuthConstants.SEPARATOR + miscellaneousProperties.getEmailTryTimes();
        //    存入缓存
        try {
            RedisUtil.set(emailCodeKey, emailCodeValue, miscellaneousProperties.getEmailExpireMinutes(), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("RedisUtil.set()异常", e);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        try {
            //  核心：构建并发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());       // 发件人（你的 163 邮箱）
            message.setTo(receiveEmail);      // 收件人（前端传的真实邮箱）
            message.setSubject(request.getBusinessType().getName() + "验证码通知"); // 邮件标题
            message.setText("您的验证码是：" + emailCode + "，" + miscellaneousProperties.getEmailExpireMinutes() + "分钟内有效！"); // 邮件内容
            //  执行发送！！！
            javaMailSender.send(message);
            log.info("{}邮件发送成功！业务类型；{}", receiveEmail, request.getBusinessType().getName());
            return "✅ 发送成功！验证码已发送至邮箱：" + receiveEmail;
        } catch (Exception e) {
            log.error("邮件发送失败！", e);
            //    删除 key
            try {
                RedisUtil.delete(emailCodeKey);
            } catch (Exception ex) {
                log.error("缓存删除失败！", ex);
                throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
            }
            throw new ForYourselfException(ResultCodeEnum.THIRD_PARTY_SERVICE_ERROR, "❌ 发送失败!!!");
        }
    }

    /**
     * 管理员忘记密码重置
     * <p>业务流程：</p>
     * <ol>
     *   <li>验证 Cloudflare Turnstile 人机验证令牌</li>
     *   <li>校验邮箱格式合法性</li>
     *   <li>从Redis中获取该邮箱对应的验证码信息（验证码+剩余尝试次数）</li>
     *   <li>验证用户输入的验证码是否正确</li>
     *   <li>生成新的随机密码并加密存储到数据库</li>
     *   <li>返回新生成的明文密码供后续处理</li>
     * </ol>
     * <p>安全机制：</p>
     * <ul>
     *   <li>验证码错误会递减剩余尝试次数，达到0次后触发邮箱冻结机制</li>
     *   <li>新密码使用BCrypt算法进行加密存储</li>
     *   <li>生成的密码符合强度要求：8-32位，包含大小写字母、数字和特殊符号</li>
     * </ul>
     *
     * @param request 忘记密码请求参数，包含邮箱地址、验证码、Cloudflare验证响应
     * @return 新生成的明文密码（建议通过安全渠道传递给用户）
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_VERIFICATION_FAILED} - 人机验证失败</li>
     *                                <li>{@link ResultCodeEnum#EMAIL_FORMAT_ERROR} - 邮箱格式错误</li>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_EXPIRED} - 验证码不存在或已过期</li>
     *                                <li>{@link ResultCodeEnum#CACHE_SERVICE_ERROR} - Redis缓存数据格式错误或缓存服务异常</li>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_ERROR} - 验证码错误（附带剩余尝试次数）</li>
     *                              </ul>
     */
    @Override
    public String forgetPassword(UserForgetPasswordRequestDTO request) {
        String receiveEmail = request.getEmail();
        log.info("{}：开始忘记密码重置", receiveEmail);
        // 检查人机
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("邮箱{}：传来无效cloud flare令牌", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 检验邮箱
        //    检验格式
        if (!ValidateUtil.isValidEmail(receiveEmail)) {
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        //    检验是否已发送code
        String emailCodeKey = BusinessTypeEnum.FORGET_PASSWORD.getName() + AuthConstants.SEPARATOR + receiveEmail;
        String emailCodeValue = RedisUtil.get(emailCodeKey);
        if (emailCodeValue == null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }
        String code = null;
        Integer remainTimes = null;
        try {
            code = emailCodeValue.split(AuthConstants.SEPARATOR)[0];
            remainTimes = Integer.parseInt(emailCodeValue.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误，请检查！", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        // 检验验证码
        if (!checkCode(request.getVerificationCode(), receiveEmail, emailCodeKey, code, remainTimes)) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, remainTimes - 1);
        }
        // 修改密码
        String newPlaintext = generateRandomPassword(16);
        String newCiphertext = bCryptPasswordEncoder.encode(newPlaintext);
        LambdaUpdateWrapper<AdminUser> set = new LambdaUpdateWrapper<AdminUser>().eq(AdminUser::getEmail, receiveEmail).set(AdminUser::getPassword, newCiphertext);
        boolean update = this.update(set);
        if (!update) {
            log.error("用户{}：密码修改失败！", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.THIRD_PARTY_SERVICE_ERROR, "密码修改失败！");
        }
        log.info("{}重置密码成功！", receiveEmail);
        return newPlaintext;
    }

    /**
     * 管理员用户登录
     * 支持用户名、邮箱、手机号三种登录方式，使用 Cloudflare Turnstile 进行人机验证
     *
     * @param request 登录请求参数，包含登录类型、账号、密码和 Cloudflare 验证令牌
     * @return 登录响应，包含 JWT Token 和结果码
     * @throws ForYourselfException 当验证码验证失败、格式错误、用户不存在或密码错误时抛出
     */
    @Override
    public AdminUserLoginResponseVO login(UserLoginRequestDTO request) {
        String name = request.getName();
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", name);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        switch (request.getLoginType()) {
            // 用户名登录
            case USERNAME -> {
                // 检验名字是否符合格式
                if (!ValidateUtil.isValidUsername(name)) {
                    throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
                }
                LambdaQueryWrapper<AdminUser> eq = new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getNickname, name);
                AdminUser adminUser = this.getOne(eq);
                if (adminUser == null) throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
                if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
                    throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
                String token = generateToken(adminUser.getNickname(), adminUser.getUid());
                return new AdminUserLoginResponseVO(token, ResultCodeEnum.USER_NORMAL_LOGIN);
            }
            // 邮箱登录
            case EMAIL -> {
                if (!ValidateUtil.isValidEmail(name))
                    throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
                LambdaQueryWrapper<AdminUser> eq = new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getEmail, name);
                AdminUser adminUser = this.getOne(eq);
                if (adminUser == null) throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
                if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
                    throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
                String token = generateToken(adminUser.getNickname(), adminUser.getUid());
                return new AdminUserLoginResponseVO(token, ResultCodeEnum.USER_NORMAL_LOGIN);
            }
            // 手机号登录
            case PHONE -> {
                //TODO
                throw new ForYourselfException(ResultCodeEnum.TODO, null);
            }
            // UID登录
            case UID -> {
                LambdaQueryWrapper<AdminUser> eq = new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, name);
                AdminUser adminUser = this.getOne(eq);
                if (adminUser == null) throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
                if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
                    throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
                String token = generateToken(adminUser.getNickname(), adminUser.getUid());
                return new AdminUserLoginResponseVO(token, ResultCodeEnum.USER_NORMAL_LOGIN);
            }
            default -> throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }
    }

    /**
     * 管理员换绑邮箱
     * <p>校验流程：新邮箱格式 → 密码验证 → 验证码验证 → 更新数据库</p>
     *
     * @param request 换绑邮箱请求参数，包含新邮箱、密码、验证码
     * @throws ForYourselfException 账户已注销、邮箱格式错误、参数错误、密码格式错误、密码错误、验证码过期、缓存服务异常、验证码错误、数据库写入失败
     */
    @Override
    public void changeEmail(UserChangeEmailRequestDTO request) {
        // 获取用户
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        if (adminUser == null) throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        log.info("用户{}：正在换绑邮箱", adminUser.getNickname());
        // 检验邮箱
        if (!ValidateUtil.isValidEmail(request.getNewEmail()))
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        if (request.getNewEmail().equals(adminUser.getEmail()))
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        // 检验密码
        if (!ValidateUtil.isValidPassword(request.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        // 检验验证码
        String emailCodeKey = BusinessTypeEnum.BIND_EMAIL + AuthConstants.SEPARATOR + adminUser.getEmail();
        String emailCode = null;
        Integer tryTimes = null;
        try {
            String emailCodeValue = RedisUtil.get(emailCodeKey);
            if (emailCodeValue == null) throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
            emailCode = emailCodeValue.split(AuthConstants.SEPARATOR)[0];
            tryTimes = Integer.valueOf(emailCodeValue.split(AuthConstants.SEPARATOR)[1]);
        } catch (Exception e) {
            log.error("用户{}：验证码获取失败", adminUser.getNickname());
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        if (!checkCode(request.getVerificationCode(), request.getNewEmail(), emailCodeKey, emailCode, tryTimes))
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, null);
        // 换绑邮箱
        LambdaUpdateWrapper<AdminUser> set = new LambdaUpdateWrapper<AdminUser>().eq(AdminUser::getId, adminUser.getId()).set(AdminUser::getEmail, request.getNewEmail());
        try {
            this.update(set);
            log.info("用户{}：换绑邮箱成功", adminUser.getNickname());
        } catch (Exception e) {
            log.error("用户{}：数据库写入失败", adminUser.getNickname());
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
    }

    /**
     * 更新管理员用户信息
     * <p>业务流程：</p>
     * <ol>
     *   <li>校验昵称格式是否符合规范（2-20位，不能包含首尾空格，不能是敏感用户名）</li>
     *   <li>从上下文获取当前登录用户的UID并查询用户信息</li>
     *   <li>检查是否有实际更新项（昵称、性别、生日至少有一项与数据库不同）</li>
     *   <li>校验生日是否合法（不能是未来日期）</li>
     *   <li>构建更新条件并执行数据库更新操作</li>
     * </ol>
     *
     * @param request 用户信息更新请求参数，包含昵称（必填）、性别（可选）、生日（可选）
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#USERNAME_FORMAT_ERROR} - 昵称格式不符合规范</li>
     *                                <li>{@link ResultCodeEnum#USER_NOT_FOUND_OR_CANCELLED} - 用户不存在或已注销（防御性编程）</li>
     *                                <li>{@link ResultCodeEnum#PARAMETER_ERROR} - 没有任何字段发生变化</li>
     *                                <li>{@link ResultCodeEnum#DATE_FORMAT_ERROR} - 生日不能是未来日期</li>
     *                                <li>{@link ResultCodeEnum#DATABASE_SERVICE_ERROR} - 数据库更新失败</li>
     *                              </ul>
     */
    @Override
    public void updateUserInfo(UserUpdateInfoRequestDTO request) {
        // 1. 检查昵称是否符合规范
        if (!ValidateUtil.isValidUsername(request.getNickname())) {
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
        }

        // 2. 获取当前登录用户
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        if (adminUser == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }

        // 3. 检查是否有更新项（不能一个都不更新和数据库一样）
        boolean hasChanges = false;

        // 检查昵称是否变化
        if (!request.getNickname().equals(adminUser.getNickname())) {
            hasChanges = true;
        }

        // 检查性别是否变化（只有传入了才比较）
        if (request.getGender() != null && !request.getGender().getCode().equals(adminUser.getGender())) {
            hasChanges = true;
        }

        // 检查生日是否变化（只有传入了才比较）
        if (request.getBirthday() != null && !request.getBirthday().equals(adminUser.getBirthday())) {
            hasChanges = true;
        }

        // 如果没有任何变化，抛出异常
        if (!hasChanges) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }

        // 4. 构建更新条件
        LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, adminUser.getId());
        
        // 设置昵称（必填）
        updateWrapper.set(AdminUser::getNickname, request.getNickname());
        
        // 设置生日（可选，需校验）
        if (request.getBirthday() != null) {
            if (request.getBirthday().isAfter(java.time.LocalDate.now())) {
                throw new ForYourselfException(ResultCodeEnum.DATE_FORMAT_ERROR, null);
            }
            updateWrapper.set(AdminUser::getBirthday, request.getBirthday());
        }
        
        // 设置性别（可选）
        if (request.getGender() != null) {
            updateWrapper.set(AdminUser::getGender, request.getGender().getCode());
        }
        
        // 5. 执行更新
        try {
            this.update(updateWrapper);
        } catch (Exception e) {
            log.error("管理员{}：数据库写入失败", adminUser.getNickname(), e);
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
    }

    /**
     * 获取管理员用户信息
     * <p>根据当前登录管理员的UID查询并返回用户详细信息</p>
     *
     * @return 管理员用户信息响应VO，包含用户的基本信息
     * @throws ForYourselfException 当账户不存在或已注销时抛出 {@link ResultCodeEnum#USER_NOT_FOUND_OR_CANCELLED}
     */
    @Override
    public AdminUserInfoResponseVO getUserInfo() {
        // 获取用户
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUid, UserContextUtil.getUid()));
        
        if (adminUser == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }
        
        // 用Hutool工具包拷贝到VO
        AdminUserInfoResponseVO responseVO = BeanUtil.copyProperties(adminUser, AdminUserInfoResponseVO.class);
        
        // 返回用户信息
        return responseVO;
    }


    /**
     * 管理员修改密码
     * <p>校验流程：新旧密码一致性校验 → 查询用户 → 原密码验证 → 加密更新密码</p>
     * <p>使用 LambdaUpdateWrapper 局部更新，避免触发 update_time 自动更新</p>
     *
     * @param request 修改密码请求DTO，包含原始密码和新密码
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#PARAMETER_ERROR} - 新旧密码相同</li>
     *                                <li>{@link ResultCodeEnum#USER_NOT_FOUND_OR_CANCELLED} - 账户不存在或已注销</li>
     *                                <li>{@link ResultCodeEnum#PASSWORD_ERROR} - 原密码错误</li>
     *                              </ul>
     */
    @Override
    public void updatePassword(UserUpdatePasswordRequestDTO request) {
        // 先验证新旧密码是否相同（避免不必要的数据库查询）
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }
        
        // 获取用户
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUid, UserContextUtil.getUid()));
        
        if (adminUser == null) {
            throw new ForYourselfException(ResultCodeEnum.USER_NOT_FOUND_OR_CANCELLED, null);
        }
        // 验证原密码
        if (!bCryptPasswordEncoder.matches(request.getOldPassword(), adminUser.getPassword())) {
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        }
        
        log.info("管理员{}：开始修改密码", adminUser.getNickname());
        
        // 修改密码
        LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getUid, adminUser.getUid()) // 条件：根据UID更新
                .set(AdminUser::getPassword, bCryptPasswordEncoder.encode(request.getNewPassword())); // 只更新密码
        
        // 执行局部更新（不会更新create_time/update_time，数据库自动生效！）
        this.update(updateWrapper);
        log.info("管理员{}：修改密码成功", adminUser.getNickname());
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
            String value = RedisUtil.get(key); //eg: A:1440
            Integer needTime = value == null ? 0 : Integer.valueOf(value.split(AuthConstants.SEPARATOR)[1]);
            //  获得实际剩余时间
            Long ttl = RedisUtil.getTtl(key, TimeUnit.MINUTES);
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
        String value = RedisUtil.get(key);
        String currentBanLevel = (value == null) ? "x" : value.split(AuthConstants.SEPARATOR)[0];

        // 2. switch 必须用【字符串字面量】（Java语法强制要求，解决报错）
        // 内部直接用上面提取的变量，无任何冗余调用
        switch (currentBanLevel) {
            case "S" -> RedisUtil.set(key, S + AuthConstants.SEPARATOR + timeS, timeS, TimeUnit.MINUTES);
            case "A" -> RedisUtil.set(key, S + AuthConstants.SEPARATOR + timeS, timeS, TimeUnit.MINUTES);
            case "B" -> RedisUtil.set(key, A + AuthConstants.SEPARATOR + timeA, timeS, TimeUnit.MINUTES);
            case "C" -> RedisUtil.set(key, B + AuthConstants.SEPARATOR + timeB, timeS, TimeUnit.MINUTES);
            case "D" -> RedisUtil.set(key, C + AuthConstants.SEPARATOR + timeC, timeS, TimeUnit.MINUTES);
            case "E" -> RedisUtil.set(key, D + AuthConstants.SEPARATOR + timeD, timeS, TimeUnit.MINUTES);
            case "F" -> RedisUtil.set(key, E + AuthConstants.SEPARATOR + timeE, timeS, TimeUnit.MINUTES);
            case "G" -> RedisUtil.set(key, F + AuthConstants.SEPARATOR + timeF, timeS, TimeUnit.MINUTES);
            default -> RedisUtil.set(key, G + AuthConstants.SEPARATOR + timeG, timeS, TimeUnit.MINUTES);
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
            return JwtUtil.generateToken(loadHashMap, jwtProperties.getExpireHour(), TimeUnit.HOURS);
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
        long time = RedisUtil.getTtl(key, TimeUnit.MINUTES);
        if (time <= 0) {
            RedisUtil.set(key, value, 1, TimeUnit.SECONDS);
        } else RedisUtil.set(key, value, time, TimeUnit.MINUTES);
        // 如果尝试次数为0，则开始冻结
        if (remainTimes <= 0) {
            String BanYULGNIERKey = BusinessTypeEnum.SEND_EMAIL.getName() + AuthConstants.SEPARATOR + email;
            markEmailAsCaptchaFreeze(BanYULGNIERKey);
            // 删除验证码的缓存
            RedisUtil.delete(key);
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




