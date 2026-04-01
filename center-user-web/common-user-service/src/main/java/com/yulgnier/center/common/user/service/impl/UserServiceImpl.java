package com.yulgnier.center.common.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yulgnier.center.common.user.config.properties.CloudflareProperties;
import com.yulgnier.center.common.user.config.properties.MailProperties;
import com.yulgnier.center.common.user.config.properties.MiscellaneousProperties;
import com.yulgnier.center.common.user.mapper.CommonUserMapper;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.common.user.service.UserService;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.CloudflareTurnstileUtils;
import com.yulgnier.common.utils.RedisUtil;
import com.yulgnier.common.utils.ValidateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<CommonUserMapper, CommonUser>
        implements UserService {    // 👈 必须加这一行！实现接口  行知道了！！窝是废物

    private final CloudflareProperties cloudflareProperties;
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final MiscellaneousProperties miscellaneousProperties;

    /**
     * 获取邮箱验证码
     *
     * @param request 邮箱验证码请求参数
     * @return 邮箱验证码
     */
    @Override
    public String getEmailCode(EmailCodeRequestDTO request) {
        String receiveEmail = request.getEmail();
        // 1.检验邮箱格式是否正确
        if (!ValidateUtils.isValidEmail(receiveEmail)) {
            log.info("邮箱{}：邮箱格式错误", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.EMAIL_FORMAT_ERROR);
        }
        // 2.检验业务是否符合参数
        if (request.getBusinessType() == null) {
            log.warn("邮箱{}：业务类型为空", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.REQUEST_INCOMPLETE);
        }
        // 3. 检验是否人机
        log.debug("前端传来cloud flare的token{}", request.getCfTurnstileResponse());
        if (!CloudflareTurnstileUtils.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())) {
            log.info("邮箱{}：传来无效cloud flare令牌", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.TOKEN_INVALID);
        }
        // 4.防止重复发送
        //4.1 生成6位随机验证码 あなたのことが大好きです。付き合ってください-愚人节快乐
        String code = generateCode();
        //4.2 拼接key
        String key = request.getBusinessType().getCode() + ":" + receiveEmail;
        //4.3 判断 key 是否存在
        if (RedisUtil.hasKey(key)) {
            log.info("邮箱{}：已发送验证码，请勿重复发送", receiveEmail);
            throw new ForYourselfException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
        }
        // 5. 发送验证码
        // 5.1 将邮箱和业务作为 key，验证码作为 value 保存到缓存中
        try {
            RedisUtil.set(key, code, miscellaneousProperties.getEmailExpireMinutes(), TimeUnit.MINUTES); // 保存验证码到缓存，有效期 5 分钟
        } catch (Exception e) {
            log.warn("缓存保存失败！", e);
            throw new ForYourselfException(ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
        log.info("开始向邮箱{}发送验证码：{}", receiveEmail, code);
        try {
            // 5.2 核心：构建并发送邮件（真实发送！）
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());       // 发件人（你的163邮箱）
            message.setTo(receiveEmail);      // 收件人（前端传的真实邮箱）
            message.setSubject("验证码通知"); // 邮件标题
            message.setText("您的验证码是：" + code + "，5分钟内有效！"); // 邮件内容
            // 执行发送！！！
            javaMailSender.send(message);
            log.debug("验证码发送成功！邮箱：{}，验证码：{}", receiveEmail, code);
            log.info("验证码发送成功!");
            return "✅ 发送成功！验证码已发送至邮箱：" + receiveEmail;
        } catch (Exception e) {
            log.warn("邮件发送失败！", e);
            // 5.3 删除 key
            RedisUtil.delete(key);
            throw new ForYourselfException(ResultCodeEnum.FAIL.getCode(), "❌ 发送失败：" + e.getMessage());
        }
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }
}
