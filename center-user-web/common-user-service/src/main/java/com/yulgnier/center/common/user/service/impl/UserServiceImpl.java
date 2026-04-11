package com.yulgnier.center.common.user.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yulgnier.center.common.user.config.properties.CloudflareProperties;
import com.yulgnier.center.common.user.config.properties.MailProperties;
import com.yulgnier.center.common.user.config.properties.MiscellaneousProperties;
import com.yulgnier.center.common.user.mapper.UserMapper;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.common.user.model.dto.UserLoginRequestDTO;
import com.yulgnier.center.common.user.model.dto.UserRegisterRequestDTO;
import com.yulgnier.center.common.user.model.enums.BanLevelEnum;
import com.yulgnier.center.common.user.model.enums.BusinessTypeEnum;
import com.yulgnier.center.common.user.model.enums.GenderEnum;
import com.yulgnier.center.common.user.service.UserService;
import com.yulgnier.common.config.properties.JwtProperties;
import com.yulgnier.common.utils.JwtUtil;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.CloudflareTurnstileUtil;
import com.yulgnier.common.utils.RedisUtil;
import com.yulgnier.common.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java.util.Random;

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
            log.info("邮箱{}：传来无效cloud flare令牌", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.ILLEGAL_REQUEST, "别攻击了，用爱发电，真的怕了！");
        }
        //  检验邮箱格式是否正确
        if (!ValidateUtil.isValidEmail(receiveEmail)) {
            log.info("邮箱{}：邮箱格式错误", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        //  检验业务是否符合参数
        if (request.getBusinessType() == null) {
            log.warn("邮箱{}：业务类型为空", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.REQUEST_INCOMPLETE, null);
        }
        String emailCodeKey = request.getBusinessType().getName() + ":" + receiveEmail; //eg: 注册:example@163.com
        String code = generateCode();   // 生成6位随机验证码    あなたのことが大好きです。付き合ってください-愚人节快乐
        String emailCodeValue = code + ":" + miscellaneousProperties.getEmailTryTimes(); //eg: 123456:6
        String emailSendCountKey = BusinessTypeEnum.SEND_EMAIL.getName() + ":" + receiveEmail;
        log.info("用户申请获取邮箱验证码：email={}, businessType={}", receiveEmail, request.getBusinessType());
        //  检验该邮箱是否允许发送验证码
        Integer needWaitTime = getNeedWaitTime(emailSendCountKey);
        if (needWaitTime > 0) {
            log.info("邮箱{}：已超过发送限制，请稍后再试", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_BANNED_SEND, needWaitTime);
        }
        //  检验验证码是否已经发送
        if (RedisUtil.hasKey(emailCodeKey)) {
            log.info("邮箱{}：已发送验证码，请勿重复发送", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_CODE_SEND_LIMIT, null);
        }

        //  发送验证码
        //      将邮箱和业务作为 key，验证码:剩余试错次数 作为 value 保存到缓存中
        try {
            RedisUtil.set(emailCodeKey, emailCodeValue, miscellaneousProperties.getEmailExpireMinutes(), TimeUnit.MINUTES); // 保存验证码到缓存，有效期 5 分钟
        } catch (Exception e) {
            log.error("缓存保存失败！", e);
            throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
        }
        log.info("开始向邮箱{}发送验证码", receiveEmail);
        try {
            //  核心：构建并发送邮件（真实发送！）
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());       // 发件人（你的 163 邮箱）
            message.setTo(receiveEmail);      // 收件人（前端传的真实邮箱）
            message.setSubject("验证码通知"); // 邮件标题
            message.setText("您的验证码是：" + code + "，5分钟内有效！"); // 邮件内容
            //  执行发送！！！
            javaMailSender.send(message);
            log.debug("验证码发送成功！邮箱：{}，验证码：{}", receiveEmail, code);
            log.info("验证码发送成功!");
            return "✅ 发送成功！验证码已发送至邮箱：" + receiveEmail;
        } catch (Exception e) {
            log.error("邮件发送失败！", e);
            // 5.3 删除 key
            try {
                RedisUtil.delete(emailCodeKey);
            } catch (Exception ex) {
                log.error("缓存删除失败！", ex);
                throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
            }
            throw new ForYourselfException(ResultCodeEnum.VERIFICATION_CODE_NOT_SEND, "❌ 发送失败!!!");
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
            log.info("用户{}：传来无效cloud flare令牌", nickname);
            throw new ForYourselfException(ResultCodeEnum.TOKEN_INVALID, "别攻击了，用爱发电，真的怕了！");
        }
        //  检验邮箱格式是否正确
        String userEmail = request.getEmail();
        if (!ValidateUtil.isValidEmail(userEmail)) {
            log.info("用户{}：邮箱格式错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR, null);
        }
        //  检查昵称
        if (!ValidateUtil.isValidUsername(nickname)) {
            log.info("用户{}：昵称格式错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.USERNAME_FORMAT_ERROR, null);
        }
        //      检查昵称是否已存在
        if (this.getOneOpt(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getNickname, nickname)).isPresent()) {
            log.info("用户{}：昵称已存在", nickname);
            throw new ForYourselfException(ResultCodeEnum.USER_ALREADY_EXISTS, null);
        }
        //  检查密码
        if (!ValidateUtil.isValidPassword(request.getPassword())) {
            log.info("用户{}：密码格式错误", nickname);
            throw new ForYourselfException(ResultCodeEnum.PASSWORD_FORMAT_ERROR, null);
        }
        //  是否有日期，有检查，无，跳过
        if (request.getBirthday() != null) {
            if (request.getBirthday().isAfter(LocalDate.now())) {
                log.info("用户{}：生日格式错误", nickname);
                throw new ForYourselfException(ResultCodeEnum.DATE_FORMAT_ERROR, null);
            }
        }
        //  检查是否有性别输入，有跳过，无设为默认未知
        if (request.getGenderEnum() == null) {
            request.setGenderEnum(GenderEnum.UNKNOWN);
        }
        //  检查邮箱和验证码是否匹配
        String emailCodeKey = BusinessTypeEnum.REGISTER.getName() + ":" + userEmail;
        String emailCodeValue = RedisUtil.get(emailCodeKey);
        if (emailCodeValue == null) {
            log.info("用户{}：验证码已过期，请重新获取", nickname);
            throw new ForYourselfException(ResultCodeEnum.VERIFICATION_CODE_EXPIRED, null);
        }
        String cacheCode = emailCodeValue.substring(0, 6);
        Integer tryTimes = Integer.valueOf(emailCodeValue.split(":")[1]);
        if (cacheCode == null || !cacheCode.equals(request.getCode())) {
            log.info("用户{}：验证码错误", nickname);
            //  记录一次到缓存，下次登录时检查
            tryTimes -= 1;
            Long ttl = RedisUtil.getTtl(emailCodeKey, TimeUnit.MICROSECONDS);
            RedisUtil.set(emailCodeKey, cacheCode + ":" + tryTimes, ttl, TimeUnit.MICROSECONDS);
            log.info("用户{}：验证码还剩{}次验证机会", nickname, tryTimes);
            //  顺便检查验证码是否没有次数了
            if (tryTimes <= 0) {
                log.info("用户{}：尝试次数为零，删除缓存的验证码,将用户邮箱录入缓存标记冻结", nickname);
                //      删除缓存中的验证码
                try {
                    RedisUtil.delete(emailCodeKey);
                } catch (Exception e) {
                    log.error("缓存删除失败！", e);
                    throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
                }
                //  将用户邮箱录入缓存标记冻结
                String emailSendCountKey = BusinessTypeEnum.SEND_EMAIL.getName() + ":" + userEmail;
                try {
                    markEmailAsCaptchaFreeze(emailSendCountKey);
                } catch (Exception e) {
                    log.error("缓存保存失败！", e);
                    throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
                }
                throw new ForYourselfException(ResultCodeEnum.VERIFICATION_CODE_EXPIRED, "❌ 尝试次数为零，请重新获取验证码");
            }
            throw new ForYourselfException(ResultCodeEnum.VERIFICATION_CODE_ERROR, tryTimes);
        }
        //  检查邮箱是否已注册
        if (this.getOneOpt(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getEmail, request.getEmail())).isPresent()) {
            log.info("用户{}：邮箱已注册", nickname);
            throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_EXIST, "邮箱已注册");
        }
        //  录入数据库
        //      生成密码密文
        String encryptedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        //      创建用户
        CommonUser user = new CommonUser();
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
        } catch (Exception e) {
            log.error("用户{}：保存用户失败", nickname, e);
            throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
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
    public String login(UserLoginRequestDTO request) {
        String name = request.getName();
        log.info("用户{}：开始登录", name);
        // 人机检测
        if (!CloudflareTurnstileUtil.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.info("用户{}：传来无效cloud flare令牌", name);
            throw new ForYourselfException(ResultCodeEnum.TOKEN_INVALID, "别攻击了，用爱发电，真的怕了！");
        }
        // switch 到不同的登录方式
        switch (request.getLoginType().getCode()) {
            // 用户名登录
            case 1 -> {
                CommonUser user = this.getOne(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getNickname, name));
                if (user == null) {
                    log.info("用户{}：用户名不存在", name);
                    throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST, null);
                }
                if (!bCryptPasswordEncoder.matches(request.getPw(), user.getPassword())) {
                    log.info("用户{}：账户或密码错误", name);
                    throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_PASSWORD_ERROR, null);
                }
                return generateToken(name, user.getUid());
            }
            // 邮箱登录
            case 2 -> {
                CommonUser user = this.getOne(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getEmail, name));
                if (user == null) {
                    log.info("用户{}：邮箱不存在", name);
                    throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST, null);
                }
                if (!bCryptPasswordEncoder.matches(request.getPw(), user.getPassword())) {
                    log.info("用户{}：账户或密码错误", name);
                    throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_PASSWORD_ERROR, null);
                }
                return generateToken(name, user.getUid());
            }
            // 手机号登录
            case 3 -> {
                // TODO
                log.warn("用户{}：手机号登录未实现", name);
                throw new ForYourselfException(ResultCodeEnum.FEATURE_NOT_IMPLEMENTED, null);
            }
            // UID 登录
            case 4 -> {
                CommonUser user = this.getOne(new LambdaQueryWrapper<CommonUser>().eq(CommonUser::getUid, name));
                if (user == null) {
                    log.info("用户{}：UID不存在", name);
                    throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST, null);
                }
                if (!bCryptPasswordEncoder.matches(request.getPw(), user.getPassword())) {
                    log.info("用户{}：账户或密码错误", name);
                    throw new ForYourselfException(ResultCodeEnum.ADMIN_ACCOUNT_PASSWORD_ERROR, null);
                }
                return generateToken(name, user.getUid());
            }
            default -> {
                log.warn("用户{}：登录方式错误", name);
                throw new ForYourselfException(ResultCodeEnum.ILLEGAL_REQUEST, null);
            }
        }
    }


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
            Integer needTime = value == null ? 0 : Integer.valueOf(value.split(":")[1]);
            //  获得实际剩余时间
            Long ttl = RedisUtil.getTtl(key, TimeUnit.MINUTES); //不存在则返回0
            //  计算实际冻结的时间（最大时间-实际剩余时间）
            long actualTime = BanLevelEnum.LEVEL_S.getCode() - ttl;
            //  计算并返回还剩多长冻结时间（计划冻结时间-实际冻结时间）
            return needTime - (int) actualTime;
        } catch (NumberFormatException e) {
            log.error("获取剩余冻结时间失败", e);
            throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
        }
    }

    /**
     * 标记该邮箱因验证码错误被冻结（等级自动升级）
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
        String currentBanLevel = (value == null) ? "x" : value.split(":")[0];

        // 2. switch 必须用【字符串字面量】（Java语法强制要求，解决报错）
        // 内部直接用上面提取的变量，无任何冗余调用
        switch (currentBanLevel) {
            case "S" -> RedisUtil.set(key, S + ":" + timeS, timeS, TimeUnit.MINUTES);
            case "A" -> RedisUtil.set(key, S + ":" + timeS, timeS, TimeUnit.MINUTES);
            case "B" -> RedisUtil.set(key, A + ":" + timeA, timeS, TimeUnit.MINUTES);
            case "C" -> RedisUtil.set(key, B + ":" + timeB, timeS, TimeUnit.MINUTES);
            case "D" -> RedisUtil.set(key, C + ":" + timeC, timeS, TimeUnit.MINUTES);
            case "E" -> RedisUtil.set(key, D + ":" + timeD, timeS, TimeUnit.MINUTES);
            case "F" -> RedisUtil.set(key, E + ":" + timeE, timeS, TimeUnit.MINUTES);
            case "G" -> RedisUtil.set(key, F + ":" + timeF, timeS, TimeUnit.MINUTES);
            default -> RedisUtil.set(key, G + ":" + timeG, timeS, TimeUnit.MINUTES);
        }
    }

    /**
     * 生成jwt 令牌
     */
    private String generateToken(String nickname, Long uid) {
        HashMap<String, Object> loadHashMap = new HashMap<>(Map.of("UID", uid));
        try {
            return JwtUtil.generateToken(loadHashMap, jwtProperties.getExpireHour(), TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("用户{}：生成jwt令牌失败", nickname, e);
            throw new ForYourselfException(ResultCodeEnum.SERVICE_ERROR, null);
        }
    }
}
