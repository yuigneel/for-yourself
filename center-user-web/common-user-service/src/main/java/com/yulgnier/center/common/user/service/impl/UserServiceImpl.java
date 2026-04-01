package com.yulgnier.center.common.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yulgnier.center.common.user.config.properties.CloudflareProperties;
import com.yulgnier.center.common.user.mapper.CommonUserMapper;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.common.user.service.UserService;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.CloudflareTurnstileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<CommonUserMapper, CommonUser>
        implements UserService {    // 👈 必须加这一行！实现接口  行知道了！！窝是废物

    private final CloudflareProperties cloudflareProperties;

    /**
     * 获取邮箱验证码
     *
     * @param request 邮箱验证码请求参数
     * @return 邮箱验证码
     */
    @Override
    public String getEmailCode(EmailCodeRequestDTO request) {
        // 1.检验邮箱格式是否正确
        // 2.检验业务是否符合参数
        // 3. 检验是否人机
        log.debug("前端传来cloud flare的token{}",request.getCfTurnstileResponse());
        if (!CloudflareTurnstileUtils.verify(request.getCfTurnstileResponse(), cloudflareProperties.getSecret())){
            log.info("邮箱{}：传来无效cloud flare令牌", request.getEmail());
            throw new ForYourselfException(ResultCodeEnum.TOKEN_INVALID);
        }
        // 4.生成并发送验证码
        // 5.将邮箱和业务作为key，验证码作为value保存到缓存中
        return "";
    }
}
