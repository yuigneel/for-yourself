package com.yuigneel.center.admin.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuigneel.center.admin.user.config.properties.CloudflareProperties;
import com.yuigneel.center.admin.user.config.properties.LinkProperties;
import com.yuigneel.center.admin.user.config.properties.MailProperties;
import com.yuigneel.center.admin.user.config.properties.MiscellaneousProperties;
import com.yuigneel.center.admin.user.mapper.AccountExceptionStatusTimeMapper;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import com.yuigneel.center.admin.user.model.domain.AccountExceptionStatusTime;
import com.yuigneel.center.admin.user.model.domain.AdminUser;
import com.yuigneel.center.admin.user.model.dto.*;
import com.yuigneel.center.admin.user.service.AdminUserFileService;
import com.yuigneel.center.user.api.model.dto.AccountStatusUpdateRequestDTO;
import com.yuigneel.center.user.api.model.enums.AccountBanLevel;
import com.yuigneel.center.user.api.model.enums.CaptchaFreezeLevelEnum;
import com.yuigneel.center.user.api.model.enums.LoginStatusEnum;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import com.yuigneel.common.utils.*;
import com.yuigneel.center.admin.user.model.vo.AdminUserCreateResponseVO;
import com.yuigneel.center.user.api.client.CommonLinkAdminClient;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yuigneel.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yuigneel.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yuigneel.center.admin.user.model.vo.AdminUserLoginResponseVO;
import com.yuigneel.center.admin.user.mapper.AdminUserMapper;
import com.yuigneel.center.admin.user.service.AdminUserService;
import com.yuigneel.center.user.api.model.enums.BusinessTypeEnum;
import com.yuigneel.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yuigneel.common.config.properties.JwtProperties;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.constants.AuthConstants;
import com.yuigneel.common.model.enums.AdminPermissionsEnum;
import com.yuigneel.common.model.result.Result;
import com.yuigneel.common.model.result.ResultCodeEnum;
import kotlin.jvm.internal.Lambda;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 管理员用户自助服务实现类
 * <p>针对表【t_admin_user(管理员用户基础信息表)】的数据库操作Service实现</p>
 * <p>注意：管理员账户被逻辑删除后，不允许像普通用户那样靠登录来恢复，因此不需要自定义Mapper，使用MyBatis-Plus默认方法即可</p>
 *
 * @author Yu_Lgnier
 * @since 2026-04-18
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
    private final LinkProperties linkProperties;
    private final CommonLinkAdminClient commonLinkAdminClient;
    private final Snowflake snowflake;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    private final MinioUtil minioUtil;
    private final AdminUserFileService adminUserFileServiceByMinIOImpl;
    private final TransactionTemplate transactionTemplate;
    private final AdminUserMapper adminUserMapper;
    private final AccountExceptionStatusTimeMapper accountExceptionStatusTimeMapper;

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
        //      创建key
        String emailCodeKey = request.getBusinessType().getName() + AuthConstants.SEPARATOR + receiveEmail;
        if (redisUtil.get(emailCodeKey) != null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ALREADY_SENT, null);
        }
        // 发送验证码
        //    创建value
        String emailCode = generateCode();
        String emailCodeValue = emailCode + AuthConstants.SEPARATOR + miscellaneousProperties.getEmailTryTimes();
        //    存入缓存
        try {
            redisUtil.set(emailCodeKey, emailCodeValue, miscellaneousProperties.getEmailExpireMinutes(), TimeUnit.MINUTES);
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
                redisUtil.delete(emailCodeKey);
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
     *   <li>新密码使用 BCrypt 算法进行加密存储</li>
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
        String emailCodeValue = redisUtil.get(emailCodeKey);
        if (emailCodeValue == null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }
        String code;
        int remainTimes;
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
        AdminUserLoginResponseVO adminUserLoginResponseVO = new AdminUserLoginResponseVO();
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", name);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 检验密码格式
        if (!ValidateUtil.isValidPassword(request.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        switch (request.getLoginType()) {
            // 用户名登录
            case USERNAME -> {
                // 检验名字是否符合格式
                if (!ValidateUtil.isValidUsername(name))
                    throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
                AdminUser adminUser = adminUserMapper.selectOneByNicknameIgnoreLogicDelete(name);
                if (adminUser == null)
                    throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
                if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
                    throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
                adminUserLoginResponseVO.setResultCodeENum(LoginStatusEnum.NORMAL_LOGIN);
                if (adminUser.getIsDeleted() == 1) {
                    log.info("用户{}：账户已注销,正在恢复。。。", name);
                    int i = adminUserMapper.restoreUserIgnoreLogicDelete(adminUser);
                    if (i == 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                    log.info("用户{}：账户已恢复", name);
                    adminUserLoginResponseVO.setResultCodeENum(LoginStatusEnum.CANCEL_UNREGISTER_LOGIN);
                }
                String token = generateToken(adminUser.getNickname(), adminUser.getUid());
                adminUserLoginResponseVO.setToken(token);
                return adminUserLoginResponseVO;
            }
            // 邮箱登录
            case EMAIL -> {
                if (!ValidateUtil.isValidEmail(name))
                    throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
                AdminUser adminUser = adminUserMapper.selectOneByEmailIgnoreLogicDelete(name);
                if (adminUser == null)
                    throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
                if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
                    throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
                adminUserLoginResponseVO.setResultCodeENum(LoginStatusEnum.NORMAL_LOGIN);
                if (adminUser.getIsDeleted() == 1) {
                    log.info("用户{}：账户已注销,正在恢复。。。", name);
                    int i = adminUserMapper.restoreUserIgnoreLogicDelete(adminUser);
                    if (i == 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                    log.info("用户{}：账户已恢复", name);
                    adminUserLoginResponseVO.setResultCodeENum(LoginStatusEnum.CANCEL_UNREGISTER_LOGIN);
                }
                String token = generateToken(adminUser.getNickname(), adminUser.getUid());
                adminUserLoginResponseVO.setToken(token);
                return adminUserLoginResponseVO;
            }
            // 手机号登录
            case PHONE -> {
                //TODO
                throw new ForYourselfException(ResultCodeEnum.TODO, null);
            }
            // UID 登录
            case UID -> {
                AdminUser adminUser = adminUserMapper.selectOneByUidIgnoreLogicDelete(Long.valueOf(name));
                if (adminUser == null)
                    throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
                if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
                    throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
                adminUserLoginResponseVO.setResultCodeENum(LoginStatusEnum.NORMAL_LOGIN);
                if (adminUser.getIsDeleted() == 1) {
                    log.info("用户{}：账户已注销,正在恢复。。。", name);
                    int i = adminUserMapper.restoreUserIgnoreLogicDelete(adminUser);
                    if (i == 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                    log.info("用户{}：账户已恢复", name);
                    adminUserLoginResponseVO.setResultCodeENum(LoginStatusEnum.CANCEL_UNREGISTER_LOGIN);
                }
                String token = generateToken(adminUser.getNickname(), adminUser.getUid());
                adminUserLoginResponseVO.setToken(token);
                return adminUserLoginResponseVO;
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
        if (adminUser == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
        log.info("用户{}：正在换绑邮箱", adminUser.getNickname());
        // 检验邮箱
        if (!ValidateUtil.isValidEmail(request.getNewEmail()))
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        if (request.getNewEmail().equals(adminUser.getEmail()))
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        // 检查新邮箱是否已被其他管理员使用
        AdminUser existingAdmin = adminUserMapper.selectOneByEmailIgnoreLogicDelete(request.getNewEmail());
        if (existingAdmin != null && !existingAdmin.getUid().equals(adminUser.getUid()))
            throw new ForYourselfException(ResultCodeEnum.EMAIL_ALREADY_EXISTS, null);
        // 检验密码
        if (!ValidateUtil.isValidPassword(request.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        if (!bCryptPasswordEncoder.matches(request.getPassword(), adminUser.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        // 检验验证码
        String emailCodeKey = BusinessTypeEnum.BIND_EMAIL.getName() + AuthConstants.SEPARATOR + request.getNewEmail();
        String emailCodeValue = redisUtil.get(emailCodeKey);

        if (emailCodeValue == null) {
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, null);
        }

        String emailCode;
        int tryTimes;
        try {
            emailCode = emailCodeValue.split(AuthConstants.SEPARATOR)[0];
            tryTimes = Integer.parseInt(emailCodeValue.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误", adminUser.getNickname(), e);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        if (!checkCode(request.getVerificationCode(), request.getNewEmail(), emailCodeKey, emailCode, tryTimes))
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_ERROR, tryTimes - 1);
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
     *   <li>从上下文获取当前登录用户的 UID并查询用户信息</li>
     *   <li>检查是否有实际更新项（昵称、性别、生日至少有一项与数据库不同）</li>
     *   <li>校验生日是否合法（不能是未来日期）</li>
     *   <li>构建更新条件并执行数据库更新操作</li>
     * </ol>
     *
     * @param request 用户信息更新请求参数，包含昵称（必填）、性别（必填）、生日（可选）
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#USERNAME_FORMAT_ERROR} - 昵称格式不符合规范</li>
     *                                <li>{@link ResultCodeEnum#ACCOUNT_NOT_FOUND_OR_CANCELLED} - 用户不存在或已注销（防御性编程）</li>
     *                                <li>{@link ResultCodeEnum#PARAMETER_ERROR} - 没有任何字段发生变化</li>
     *                                <li>{@link ResultCodeEnum#DATE_FORMAT_ERROR} - 生日不能是未来日期</li>
     *                                <li>{@link ResultCodeEnum#DATABASE_SERVICE_ERROR} - 数据库更新失败</li>
     *                              </ul>
     */
    @Override
    public void updateUserInfo(UserUpdateInfoRequestDTO request, MultipartFile avatarFile) {
        // 检查昵称是否符合规范
        if (!ValidateUtil.isValidUsername(request.getNickname())) {
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
        }
        // 检查生日格式
        if (request.getBirthday() != null) {
            if (request.getBirthday().isAfter(LocalDate.now())) {
                throw new ForYourselfException(ResultCodeEnum.DATE_FORMAT_ERROR, null);
            }
        }
        // 获取当前登录用户
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        if (adminUser == null) {
            throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
        }

        // 获取用户头像
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, UserContextUtil.getUid()).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.ADMIN);
        AccountAvatar accountAvatar = adminUserFileServiceByMinIOImpl.getOne(eq);

        // 检查是否有更新项（不能一个都不更新和数据库一样）
        boolean hasChanges = false;

        // 检查昵称是否变化（必填字段，直接比较）
        if (!request.getNickname().equals(adminUser.getNickname())) {
            hasChanges = true;
        }

        // 检查性别是否变化（必填字段）
        if (!request.getGenderEnum().equals(adminUser.getGender())) {
            hasChanges = true;
        }

        // 检查生日是否变化（可选字段）
        if (request.getBirthday() == null) {
            // 前端没传生日，但数据库有值 → 用户想清空生日
            if (adminUser.getBirthday() != null) {
                hasChanges = true;
            }
        } else {
            // 前端传了生日，比较值是否不同
            if (!request.getBirthday().equals(adminUser.getBirthday())) {
                hasChanges = true;
            }
        }

        // 检查头像是否变化（可选字段）
        Boolean hasAvatarChanged = false;
        if (avatarFile == null || avatarFile.isEmpty()) {
            // 没有上传新头像，检查是否有旧头像需要删除
            if (accountAvatar != null && accountAvatar.getAvatarUrl() != null && !accountAvatar.getAvatarUrl().isEmpty()) {
                hasAvatarChanged = true;
                hasChanges = true;
            }
        } else {
            // 上传了新头像
            hasAvatarChanged = true;
            hasChanges = true;
        }


        // 如果没有任何变化，抛出异常
        if (!hasChanges) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        }

        // 构建更新条件（性别为必填字段，始终更新）
        LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, adminUser.getId())
                .set(AdminUser::getNickname, request.getNickname())
                .set(AdminUser::getGender, request.getGenderEnum().getCode())
                .set(AdminUser::getBirthday, request.getBirthday());
        log.info("用户{}：开始更新用户信息", adminUser.getNickname());
        // 执行更新
        final Boolean finalHasAvatarChanged = hasAvatarChanged;
        transactionTemplate.execute(status -> {
            try {
                // 更新用户信息
                this.update(updateWrapper);
                // 更新用户头像
                if (finalHasAvatarChanged) adminUserFileServiceByMinIOImpl.uploadAvatar(avatarFile);
                log.info("用户{}：更新用户信息成功", adminUser.getNickname());
                return true;
            } catch (Exception e) {
                log.error("用户{}：更新用户信息失败", adminUser.getNickname());
                throw new ForYourselfException(ResultCodeEnum.SYSTEM_EXECUTION_ERROR, null);
            }
        });
    }

    /**
     * 获取管理员用户信息
     * <p>根据当前登录管理员的 UID 查询并返回用户详细信息</p>
     *
     * @return 管理员用户信息响应VO，包含用户的基本信息
     * @throws ForYourselfException 当账户不存在或已注销时抛出 {@link ResultCodeEnum#ACCOUNT_NOT_FOUND_OR_CANCELLED}
     */
    @Override
    public AdminUserInfoResponseVO getAdminSelfInfo() {
        // 获取用户
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        if (adminUser == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
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
     *                                <li>{@link ResultCodeEnum#ACCOUNT_NOT_FOUND_OR_CANCELLED} - 账户不存在或已注销</li>
     *                                <li>{@link ResultCodeEnum#PASSWORD_ERROR} - 原密码错误</li>
     *                              </ul>
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
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUid, UserContextUtil.getUid()));

        if (adminUser == null) {
            throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
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

    /**
     * 管理员用户注销（账号逻辑删除）
     * <p>业务流程：</p>
     * <ol>
     *   <li>验证 Cloudflare Turnstile 人机验证令牌</li>
     *   <li>校验用户昵称、邮箱、密码格式合法性</li>
     *   <li>从Redis中获取该邮箱对应的验证码信息（验证码+剩余尝试次数）</li>
     *   <li>验证用户输入的验证码是否正确</li>
     *   <li>验证用户密码是否与数据库中存储的密码匹配</li>
     *   <li>执行逻辑删除操作，将管理员账号标记为已注销</li>
     * </ol>
     * <p>安全机制：</p>
     * <ul>
     *   <li>验证码错误会递减剩余尝试次数，达到0次后触发邮箱冻结机制</li>
     *   <li>使用BCrypt算法验证密码，确保密码安全性</li>
     *   <li>逻辑删除而非物理删除，保留数据完整性</li>
     *   <li>注意：管理员账户被逻辑删除后，不允许像普通用户那样靠登录来恢复</li>
     * </ul>
     *
     * @param request 用户注销请求参数，包含用户昵称、邮箱、密码、验证码、Cloudflare验证响应
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_VERIFICATION_FAILED} - 人机验证失败或验证码错误</li>
     *                                <li>{@link ResultCodeEnum#USERNAME_FORMAT_ERROR} - 用户名格式错误</li>
     *                                <li>{@link ResultCodeEnum#EMAIL_FORMAT_ERROR} - 邮箱格式错误</li>
     *                                <li>{@link ResultCodeEnum#PASSWORD_FORMAT_ERROR} - 密码格式错误</li>
     *                                <li>{@link ResultCodeEnum#CAPTCHA_EXPIRED} - 验证码不存在或已过期</li>
     *                                <li>{@link ResultCodeEnum#CACHE_SERVICE_ERROR} - Redis缓存数据格式错误或缓存服务异常</li>
     *                                <li>{@link ResultCodeEnum#ACCOUNT_NOT_FOUND_OR_CANCELLED} - 用户不存在或已被注销</li>
     *                                <li>{@link ResultCodeEnum#PASSWORD_ERROR} - 密码错误</li>
     *                                <li>{@link ResultCodeEnum#DATABASE_SERVICE_ERROR} - 数据库服务异常</li>
     *                              </ul>
     */
    @Override
    public void logout(AdminUserLogoutRequestDTO request) {
        String nickname = request.getNickname();
        // 验证人机
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.warn("用户{}：传来无效cloud flare令牌", nickname);
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "别攻击了，用爱发电，真的怕了！");
        }
        // 验证格式
        if (!ValidateUtil.isValidUsername(nickname))
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, "用户名格式错误");
        if (!ValidateUtil.isValidEmail(request.getEmail()))
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, "邮箱格式错误");
        if (!ValidateUtil.isValidPassword(request.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, "密码格式错误");
        // 验证验证码
        String emailCodeKey = BusinessTypeEnum.CANCEL_USER.getName() + AuthConstants.SEPARATOR + request.getEmail();
        String emailCodeValue = redisUtil.get(emailCodeKey);
        if (emailCodeValue == null) throw new ForYourselfException(ResultCodeEnum.CAPTCHA_EXPIRED, "验证码已过期");
        String rdEmailCode;
        Integer rdRemainTimes;
        try {
            rdEmailCode = emailCodeValue.split(AuthConstants.SEPARATOR)[0];
            rdRemainTimes = Integer.valueOf(emailCodeValue.split(AuthConstants.SEPARATOR)[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("用户{}：Redis缓存格式错误，请检查！", nickname);
            throw new ForYourselfException(ResultCodeEnum.CACHE_SERVICE_ERROR, null);
        }
        if (!checkCode(request.getCode(), request.getEmail(), emailCodeKey, rdEmailCode, rdRemainTimes))
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "验证码错误");
        // 验证密码
        AdminUser user = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getEmail, request.getEmail()));
        if (user == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_ERROR, null);
        // 逻辑删除
        try {
            this.remove(new LambdaUpdateWrapper<AdminUser>().eq(AdminUser::getEmail, user.getEmail()));
        } catch (Exception e) {
            log.error("用户{}：数据库服务异常，请检查！", nickname);
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
    }

    /**
     * 分页查询管理员用户列表
     * <p>支持条件：排除自己、时间范围、逻辑删除状态、账户权限、账户状态、关键词搜索</p>
     *
     * @param query 分页查询请求参数
     * @return 分页结果，包含管理员用户信息列表
     */
    @Override
    public IPage<AdminUserInfoResponseVO> pageUsers(AdminUserPageQueryDTO query) {
        // 1. 创建分页对象
        Page<AdminUser> page = new Page<>(query.getCurrent(), query.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();

        // 2.1 排除当前登录用户
        if (Boolean.TRUE.equals(query.getExcludeSelf())) {
            Long currentUid = UserContextUtil.getUid();
            wrapper.ne(AdminUser::getUid, currentUid);
        }

        // 2.2 时间范围查询（创建时间）
        if (query.getStartTime() != null && query.getEndTime() != null) {
            // 情况1：开始时间和截止时间都有 → 查询区间 [startTime 00:00:00, endTime 23:59:59]
            wrapper.between(
                    AdminUser::getCreateTime,  // 数据库字段：create_time
                    query.getStartTime().atStartOfDay(),  // LocalDate → LocalDateTime (当天 00:00:00)
                    query.getEndTime().atTime(23, 59, 59) // LocalDate → LocalDateTime (当天 23:59:59)
            );
            // 生成的 SQL: WHERE create_time BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59'
        } else if (query.getStartTime() != null) {
            // 情况2：只有开始时间 → 从开始时间到现在 [startTime 00:00:00, 现在]
            wrapper.ge(
                    AdminUser::getCreateTime,  // ge = Greater or Equal (大于等于)
                    query.getStartTime().atStartOfDay()  // 当天 00:00:00
            );
            // 生成的 SQL: WHERE create_time >= '2024-01-01 00:00:00'
        } else if (query.getEndTime() != null) {
            // 情况3：只有截止时间 → 从头到截止时间 [很久以前, endTime 23:59:59]
            wrapper.le(
                    AdminUser::getCreateTime,  // le = Less or Equal (小于等于)
                    query.getEndTime().atTime(23, 59, 59)  // 当天 23:59:59
            );
            // 生成的 SQL: WHERE create_time <= '2024-12-31 23:59:59'
        }

        // 2.3 逻辑删除状态筛选
        if (query.getIsDeleted() != null) {
            wrapper.eq(AdminUser::getIsDeleted, query.getIsDeleted());
        }

        // 2.4 账户权限筛选
        if (query.getAccountPermission() != null) {
            wrapper.eq(AdminUser::getAccountPermission, query.getAccountPermission());
        }

        // 2.5 账户状态筛选
        if (query.getAccountStatus() != null) {
            wrapper.eq(AdminUser::getAccountStatus, query.getAccountStatus());
        }

        // 2.6 关键词模糊查询（昵称或邮箱）
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(AdminUser::getNickname, query.getKeyword())
                    .or()
                    .like(AdminUser::getEmail, query.getKeyword()));
        }

        // 2.7 排序
        String orderBy = query.getOrderBy() != null ? query.getOrderBy() : "createTime";
        String orderDirection = query.getOrderDirection() != null ? query.getOrderDirection() : "desc";

        if ("asc".equalsIgnoreCase(orderDirection)) {
            if ("nickname".equals(orderBy)) {
                wrapper.orderByAsc(AdminUser::getNickname);
            } else {
                wrapper.orderByAsc(AdminUser::getCreateTime);
            }
        } else {
            if ("nickname".equals(orderBy)) {
                wrapper.orderByDesc(AdminUser::getNickname);
            } else {
                wrapper.orderByDesc(AdminUser::getCreateTime);
            }
        }

        // 3. 执行分页查询
        IPage<AdminUser> userPage = this.page(page, wrapper);

        // 4. 转换为 VO 并填充头像 URL
        return userPage.convert(user -> {
            AdminUserInfoResponseVO vo = BeanUtil.copyProperties(user, AdminUserInfoResponseVO.class);

            // 查询用户头像
            AccountAvatar accountAvatar = adminUserFileServiceByMinIOImpl.getOne(
                    new LambdaQueryWrapper<AccountAvatar>()
                            .eq(AccountAvatar::getUid, user.getUid())
                            .eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.ADMIN)
            );

            // 如果有头像，生成临时访问URL
            if (accountAvatar != null && accountAvatar.getAvatarUrl() != null) {
                try {
                    String avatarUrl = minioUtil.getPresignedUrl(accountAvatar.getAvatarUrl(), 1, TimeUnit.DAYS);
                    vo.setAvatar(avatarUrl);
                } catch (Exception e) {
                    log.error("管理员{}：获取头像URL失败", user.getNickname(), e);
                    // 头像获取失败不影响其他信息展示，设置为null
                    vo.setAvatar(null);
                }
            }

            return vo;
        });
    }


    /**
     * 创建管理员账户
     * <p>功能说明：</p>
     * <ul>
     *   <li>验证创建者身份和权限</li>
     *   <li>校验被创建者邮箱格式</li>
     *   <li>检查权限级别，确保创建者权限高于被创建者</li>
     *   <li>自动生成唯一昵称、随机密码和UID</li>
     *   <li>将新管理员信息存入数据库</li>
     * </ul>
     *
     * @param request 管理员创建请求参数，包含邮箱和权限级别
     * @return AdminUserCreateResponseVO 包含生成的昵称和初始密码
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>ACCOUNT_NOT_FOUND: 创建者不存在</li>
     *                                <li>EMAIL_FORMAT_ERROR: 邮箱格式错误</li>
     *                                <li>INSUFFICIENT_PERMISSIONS: 创建者权限不足</li>
     *                                <li>DATABASE_SERVICE_ERROR: 数据库服务异常</li>
     *                              </ul>
     */
    @Override
    public AdminUserCreateResponseVO createAdmin(AdminUserCreateRequestDTO request) {
        // 获取创建者对象
        AdminUser creator = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        if (creator == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND, "创捷者不存在");
        // 检验被创建者邮箱格式
        if (!ValidateUtil.isValidEmail(request.getEmail()))
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, "邮箱格式错误");
        // 检查邮箱是否已被其他管理员使用
        AdminUser existingAdmin = adminUserMapper.selectOneByEmailIgnoreLogicDelete(request.getEmail());
        if (existingAdmin != null)
            throw new ForYourselfException(ResultCodeEnum.EMAIL_ALREADY_EXISTS, "该邮箱已被使用");
        // 验证权限
        AdminPermissionsEnum createdByAuthLevel = request.getAccountPermission();
        if (creator.getAccountPermission().getCode() >= createdByAuthLevel.getCode())        // 这里root 权限只在数据库创建的时候创建所以这里大于等于不冲突
            throw new ForYourselfException(ResultCodeEnum.INSUFFICIENT_PERMISSIONS, "权限不足");
        // 生成被创建者对象 uid 邮箱 昵称 密码 权限
        String nickname = generateUniqueNickname();
        String password = generateRandomPassword(16);
        AdminUser createdBy = new AdminUser();
        createdBy.setUid(snowflake.nextId());
        createdBy.setEmail(request.getEmail());
        createdBy.setNickname(nickname);
        createdBy.setPassword(bCryptPasswordEncoder.encode(password));
        createdBy.setJoinDate(LocalDate.now());  // 设置入驻日期为当前日期
        createdBy.setAccountPermission(request.getAccountPermission());
        createdBy.setUpdateBy(creator.getUid());
        // 存入数据库
        try {
            this.save(createdBy);
        } catch (Exception e) {
            log.error("用户{}：数据库服务异常，请检查！", nickname);
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
        // 返回 vo
        return new AdminUserCreateResponseVO(nickname, password);
    }

    /**
     * 修改其他管理员的权限等级
     * <p>业务流程：</p>
     * <ol>
     *   <li>获取当前操作用户（修改者）信息</li>
     *   <li>获取被修改的管理员信息</li>
     *   <li>验证权限等级：修改者权限必须高于被修改者原权限和目标权限</li>
     *   <li>更新被修改者的权限等级和修改人 UID</li>
     * </ol>
     * <p>权限校验规则：修改者权限码必须小于被修改者原权限码且小于目标权限码</p>
     *
     * @param request 权限修改请求参数，包含被修改管理员UID和新的权限等级
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>INSUFFICIENT_PERMISSIONS: 修改者权限不足</li>
     *                                <li>DATABASE_SERVICE_ERROR: 数据库更新失败</li>
     *                              </ul>
     */
    @Override
    public void updateAdminPermission(AdminUserPermissionUpdateRequestDTO request) {
        // 获取创建者对象
        AdminUser upDater = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        // 获取被修改者对象
        AdminUser updatedBy = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, request.getUid()));
        // 验证权限
        int modifierPermission = upDater.getAccountPermission().getCode(); // 修改者权限
        int modifiedOriginPermission = updatedBy.getAccountPermission().getCode(); // 被修改者原权限
        int targetExpectPermission = request.getNewPermission().getCode(); // 目标权限
        if (modifierPermission >= modifiedOriginPermission || modifierPermission >= targetExpectPermission)
            throw new ForYourselfException(ResultCodeEnum.INSUFFICIENT_PERMISSIONS, "权限不足");
        // 执行修改
        LambdaUpdateWrapper<AdminUser> set = new LambdaUpdateWrapper<AdminUser>().eq(AdminUser::getId, updatedBy.getId())
                .set(AdminUser::getAccountPermission, request.getNewPermission())
                .set(AdminUser::getUpdateBy, upDater.getUid());
        boolean update = this.update(set);
        if (!update) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
    }

    /**
     * 修改其它管理员的账号状态
     * <p>权限校验：只有权限等级高于目标管理员的管理员才能修改其状态</p>
     *
     * @param request 状态修改请求，包含目标管理员UID和新状态
     * @throws ForYourselfException 当权限不足或数据库操作失败时抛出异常
     */
    @Override
    public void updateAdminStatus(AccountStatusUpdateRequestDTO request) {
        // ===获取修改者和被修改者对象
        AdminUser modifier = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, UserContextUtil.getUid()));
        AdminUser updated = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, request.getUid())); // 被修改者
        if (updated == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND, null);
        // ===判断是否有权限修改
        if (modifier.getAccountPermission().getCode() >= updated.getAccountPermission().getCode())
            throw new ForYourselfException(ResultCodeEnum.INSUFFICIENT_PERMISSIONS, null);
        // ===获取被修改者原来的状态和目标状态
        AccountStatusEnum originStatus = updated.getAccountStatus();
        AccountStatusEnum targetStatus = request.getTargetStatus();
        // ===判断是否需要修改
        if (originStatus == targetStatus) throw new ForYourselfException(ResultCodeEnum.NO_NEED_TO_UPDATE, null);
        // ====修改
        // ---获取封禁等级（如果目标状态不是正常，则必须有封禁等级）
        AccountBanLevel byLetter;
        if (request.getBanLevel() != null && !request.getBanLevel().isEmpty()) {
            byLetter = AccountBanLevel.getByLetter(request.getBanLevel());
        } else {
            byLetter = null;
        }
        transactionTemplate.executeWithoutResult(status -> {
            // ---先修改管理员账户状态
            LambdaUpdateWrapper<AdminUser> set = new LambdaUpdateWrapper<AdminUser>().eq(AdminUser::getUid, updated.getUid())
                    .set(AdminUser::getAccountStatus, targetStatus)
                    .set(AdminUser::getUpdateBy, modifier.getUid());
            boolean update = this.update(set);
            if (!update) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
            // ---根据情况处理封禁表
            // ···如果目标状态为正常,原始状态肯定为异常，删除
            if (targetStatus == AccountStatusEnum.ACCOUNT_STATUS_NORMAL) {
                LambdaQueryWrapper<AccountExceptionStatusTime> eq = new LambdaQueryWrapper<AccountExceptionStatusTime>().eq(AccountExceptionStatusTime::getUid, updated.getUid())
                        .eq(AccountExceptionStatusTime::getExceptionType, originStatus)
                        .eq(AccountExceptionStatusTime::getIdentityType, AccountIdentityTypeEnum.ADMIN);
                boolean delete = accountExceptionStatusTimeMapper.delete(eq) > 0;
                if (!delete) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
            }
            // ···如果目标状态为异常
            else {
                // ~~~先计算到期时间
                if (byLetter == null)
                    throw new ForYourselfException(ResultCodeEnum.INCOMPLETE_PARAMETERS, "封禁等级不能为空");
                LocalDateTime expireTime = LocalDateTime.now().plusHours(byLetter.getCode());
                // ~~~如果原始状态为正常，则插入
                if (originStatus == AccountStatusEnum.ACCOUNT_STATUS_NORMAL) {
                    AccountExceptionStatusTime insert = new AccountExceptionStatusTime();
                    insert.setUid(updated.getUid());
                    insert.setExceptionType(targetStatus);
                    insert.setIdentityType(AccountIdentityTypeEnum.ADMIN);
                    insert.setExpireTime(expireTime);
                    if (request.getBanReason() != null) insert.setReason(request.getBanReason());
                    insert.setUpdateBy(modifier.getUid());
                    int insertCount = accountExceptionStatusTimeMapper.insert(insert);
                    if (insertCount <= 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                }
                // ~~~如果原始状态为异常，则更新
                else {
                    LambdaUpdateWrapper<AccountExceptionStatusTime> updateWrapper = new LambdaUpdateWrapper<AccountExceptionStatusTime>()
                            .eq(AccountExceptionStatusTime::getUid, updated.getUid())
                            .eq(AccountExceptionStatusTime::getIdentityType, AccountIdentityTypeEnum.ADMIN)
                            .set(AccountExceptionStatusTime::getExceptionType, targetStatus)
                            .set(AccountExceptionStatusTime::getExpireTime, expireTime)
                            .set(AccountExceptionStatusTime::getUpdateBy, modifier.getUid());
                    if (request.getBanReason() != null)updateWrapper.set(AccountExceptionStatusTime::getReason, request.getBanReason());
                    int updateCount = accountExceptionStatusTimeMapper.update(null, updateWrapper);
                    if (updateCount <= 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
                }
            }
        });
    }

    /**
     * 获取特定管理员的详细信息
     *
     * @param uid 管理员 UID
     * @return 管理员信息 VO
     * @throws ForYourselfException 当管理员不存在时抛出 ACCOUNT_NOT_FOUND 异常
     */
    @Override
    public AdminUserInfoResponseVO getOtherAdminInfo(Long uid) {
        AdminUser one = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUid, uid));
        if (one == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND, null);
        return BeanUtil.copyProperties(one, AdminUserInfoResponseVO.class);
    }

    /**
     * 分页查询普通用户列表（通过 Feign 调用）
     * <p>业务流程：</p>
     * <ol>
     *   <li>生成 Link Token 用于跨服务身份验证</li>
     *   <li>将 Token 设置到 UserContext 中</li>
     *   <li>通过 Feign 调用 common-user-service 的分页接口</li>
     *   <li>返回分页结果</li>
     * </ol>
     * <p>安全机制：使用一次性短时防伪通行令牌进行内网服务间鉴权</p>
     *
     * @param query 分页查询参数，包含页码、每页条数、筛选条件等
     * @return 普通用户分页结果
     * @throws ForYourselfException {@link ResultCodeEnum#REMOTE_RESPONSE_ERROR} - 远程服务响应异常
     */
    @Override
    public IPage<CommonUserInfoResponseVO> getCommonPages(CommonUserPageQueryDTO query) {
        String token = InnerFlexibleTokenSecurityUtil.generateToken(linkProperties.getAdminCommon().getSecretKey(), linkProperties.getAdminCommon().getExpireMilliseconds());
        UserContextUtil.setLink(token);
        Result<Page<CommonUserInfoResponseVO>> pageResult = commonLinkAdminClient.pageUsers(query);
        IPage<CommonUserInfoResponseVO> data = pageResult.getData();
        if (data == null) throw new ForYourselfException(ResultCodeEnum.REMOTE_RESPONSE_ERROR, null);
        return data;
    }


    /**
     * 获取普通用户信息（通过Feign调用）
     * <p>生成内部灵活 Token并通过Feign客户端调用普通用户服务</p>
     *
     * @param uid 普通用户 UID
     * @return 普通用户信息 VO
     * @throws ForYourselfException 当用户不存在时抛出 ACCOUNT_NOT_FOUND 异常
     */
    @Override
    public CommonUserInfoResponseVO getOneCommonUserInfo(Long uid) {
        String token = InnerFlexibleTokenSecurityUtil.generateToken(linkProperties.getAdminCommon().getSecretKey(), linkProperties.getAdminCommon().getExpireMilliseconds());
        UserContextUtil.setLink(token);
        CommonUserInfoResponseVO data = commonLinkAdminClient.getById(uid).getData();
        if (data == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND, null);
        return data;
    }

    /**
     * 管理员修改普通用户账号状态（跨服务调用）
     * <p>业务流程：生成内部访问令牌 → 设置到ThreadLocal → Feign调用common-user-service → 验证响应</p>
     *
     * @param request 状态更新请求参数，包含普通用户UID和新的账户状态
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link ResultCodeEnum#REMOTE_RESPONSE_ERROR} - 远程服务返回数据为空</li>
     *                              </ul>
     */
    @Override
    public void updateCommonUserStatus(AccountStatusUpdateRequestDTO request) {
        String token = InnerFlexibleTokenSecurityUtil.generateToken(linkProperties.getAdminCommon().getSecretKey(), linkProperties.getAdminCommon().getExpireMilliseconds());
        UserContextUtil.setLink(token);
        String data = commonLinkAdminClient.changeStatus(request).getData();
        if (data == null) throw new ForYourselfException(ResultCodeEnum.REMOTE_RESPONSE_ERROR, null);
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
            long actualTime = CaptchaFreezeLevelEnum.LEVEL_S.getCode() - ttl;
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
        String S = CaptchaFreezeLevelEnum.LEVEL_S.getName();
        String A = CaptchaFreezeLevelEnum.LEVEL_A.getName();
        String B = CaptchaFreezeLevelEnum.LEVEL_B.getName();
        String C = CaptchaFreezeLevelEnum.LEVEL_C.getName();
        String D = CaptchaFreezeLevelEnum.LEVEL_D.getName();
        String E = CaptchaFreezeLevelEnum.LEVEL_E.getName();
        String F = CaptchaFreezeLevelEnum.LEVEL_F.getName();
        String G = CaptchaFreezeLevelEnum.LEVEL_G.getName();

        Integer timeS = CaptchaFreezeLevelEnum.LEVEL_S.getCode();
        Integer timeA = CaptchaFreezeLevelEnum.LEVEL_A.getCode();
        Integer timeB = CaptchaFreezeLevelEnum.LEVEL_B.getCode();
        Integer timeC = CaptchaFreezeLevelEnum.LEVEL_C.getCode();
        Integer timeD = CaptchaFreezeLevelEnum.LEVEL_D.getCode();
        Integer timeE = CaptchaFreezeLevelEnum.LEVEL_E.getCode();
        Integer timeF = CaptchaFreezeLevelEnum.LEVEL_F.getCode();
        Integer timeG = CaptchaFreezeLevelEnum.LEVEL_G.getCode();

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

    /**
     * 生成符合规则的随机不重复昵称（高唯一版）
     * <p>昵称规则（参考 ValidateUtil.isValidUsername）：</p>
     * <ul>
     *   <li>长度 2-20 位</li>
     *   <li>不能包含首尾空格</li>
     *   <li>不能是 admin、root、system 等系统保留用户名</li>
     *   <li>支持任意字符（包括中文、英文、数字、特殊符号等）</li>
     * </ul>
     * <p>生成策略：使用前缀 + 高强度随机字符组合，数据库校验唯一性</p>
     *
     * @return 符合规则且在数据库中不重复的随机昵称
     * @throws ForYourselfException 当多次尝试仍无法生成唯一昵称时抛出
     */
    private String generateUniqueNickname() {
        // 友好前缀池
        final String[] NICKNAME_PREFIXES = {
                "用户", "访客", "朋友", "伙伴", "星友",
                "User", "Guest", "Friend", "Buddy", "Star",
                "小", "阿", "老", "大"
        };
        // 【扩容】随机字符集：数字 + 大小写英文字母（组合量极大，极低重复率）
        final String RANDOM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        // 最大重试次数（增加兜底）
        final int MAX_RETRY_COUNT = 15;
        // 【加长】后缀长度范围：6-12位（长度足够，杜绝短字符重复）
        final int MIN_SUFFIX_LENGTH = 6;
        final int MAX_SUFFIX_LENGTH = 12;

        // 线程安全的随机工具
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            // 1. 随机选择前缀
            String prefix = NICKNAME_PREFIXES[random.nextInt(NICKNAME_PREFIXES.length)];

            // 2. 生成高强度随机后缀
            int suffixLength = random.nextInt(MIN_SUFFIX_LENGTH, MAX_SUFFIX_LENGTH + 1);
            StringBuilder suffix = new StringBuilder(suffixLength);
            for (int i = 0; i < suffixLength; i++) {
                suffix.append(RANDOM_CHARS.charAt(random.nextInt(RANDOM_CHARS.length())));
            }

            // 3. 拼接最终昵称
            String nickname = prefix + suffix;

            // 4. 校验昵称格式
            if (!ValidateUtil.isValidUsername(nickname)) {
                log.debug("昵称格式校验失败：{}，第{}次重试", nickname, retry + 1);
                continue;
            }

            // 5. 轻量级查询：判断昵称是否已存在
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<AdminUser>()
                    .eq(AdminUser::getNickname, nickname);
            boolean exists = this.exists(queryWrapper);

            // 6. 不存在则返回
            if (!exists) {
                log.debug("生成唯一昵称成功：{}", nickname);
                return nickname;
            }

            log.debug("昵称【{}】已存在，第{}次重试", nickname, retry + 1);
        }

        // 重试次数耗尽，抛出异常
        log.error("生成唯一昵称失败：已重试{}次仍未生成", MAX_RETRY_COUNT);
        throw new ForYourselfException(ResultCodeEnum.SYSTEM_EXECUTION_ERROR, "自动生成昵称失败，请稍后重试");
    }
}




