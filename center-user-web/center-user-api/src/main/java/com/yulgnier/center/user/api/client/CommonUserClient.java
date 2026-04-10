package com.yulgnier.center.user.api.client;

import com.yulgnier.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yulgnier.common.model.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "center-common-user-service")
public interface CommonUserClient {
    @PostMapping("/center-common/user/getEmailCode")
     Result<String> getEmailCode(@RequestBody EmailCodeRequestDTO request);
}
