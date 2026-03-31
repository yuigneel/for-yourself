package com.yulgnier.center.common.user.controller;

import com.yulgnier.center.common.user.model.dto.EmailCodeRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
@Tag(name = "邮箱验证码测试", description = "用于调试的邮箱验证码发送接口")
@RequestMapping("/test")
@RestController
@Slf4j  // 日志注解，替换 System.out，更规范
public class EmailTestController {

    // 注入Spring Boot自动配置的邮件发送器（你已经配了yml，直接用）
    @Autowired
    private JavaMailSender javaMailSender;

    // 从yml读取你的发件邮箱（和yml里mail.username一致）
    @Value("${spring.mail.username}")
    private String sendEmail;

    @Operation(summary = "测试发送邮件验证码", description = "接收真实邮箱地址，生成并发送 6 位随机验证码")
    @PostMapping("/test-email-code")
    public String testEmailCode(
            @Parameter(description = "邮箱验证码请求参数", required = true)
            @RequestBody EmailCodeRequestDTO request) {
        // 1. 获取前端传入的真实收件邮箱
        String receiveEmail = request.getEmail();
        log.info("开始向真实邮箱发送验证码：{}", receiveEmail);

        // 2. 生成6位随机验证码（比固定123456真实）
        String code = generateCode();

        try {
            // 3. 核心：构建并发送邮件（真实发送！）
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sendEmail);       // 发件人（你的163邮箱）
            message.setTo(receiveEmail);      // 收件人（前端传的真实邮箱）
            message.setSubject("验证码通知"); // 邮件标题
            message.setText("您的验证码是：" + code + "，5分钟内有效！"); // 邮件内容

            // 执行发送！！！
            javaMailSender.send(message);

            log.info("验证码发送成功！邮箱：{}，验证码：{}", receiveEmail, code);
            return "✅ 发送成功！验证码已发送至邮箱：" + receiveEmail + "，验证码：" + code;
        } catch (Exception e) {
            log.error("邮件发送失败！", e);
            return "❌ 发送失败：" + e.getMessage();
        }
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }
}