package com.yulgnier.center.common.user.model.dto;


import com.yulgnier.center.common.user.model.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;import lombok.Data;import java.time.LocalDate; /**
 * 用户注册请求 DTO
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequestDTO {
    
    @Schema(description = "用户邮箱", example = "example@email.com", required = true)
    private String email;
    
    @Schema(description = "用户昵称", example = "小明", required = true)
    private String nickname;
    
    @Schema(description = "登录密码", example = "123456", required = true)
    private String password;
    
    @Schema(description = "实际出生日期", example = "2000-01-01", required = false)
    private LocalDate birthday;
    
    @Schema(description = "性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女", example = "0", required = false)
    private GenderEnum genderEnum;
}
/*2026.3.31 yulgnier：哥，我焦虑。连个自己出去住的房租都交不起，我要反思这些年我努力了吗，我的工资有没有涨一涨？（不对，我没有工作[流泪][流泪][流泪]）
            瑞：你肯定租不起，租房一个月带生活成本指不定都1500左右了。组个蛋的组，在家啃老。
 */
