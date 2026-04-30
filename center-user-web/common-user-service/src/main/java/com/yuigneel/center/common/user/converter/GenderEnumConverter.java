package com.yuigneel.center.common.user.converter;

import com.yuigneel.center.user.api.model.enums.GenderEnum;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.result.ResultCodeEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 性别枚举专属转换器
 * 核心能力：Spring MVC接收请求参数时，自动将字符串类型的性别code转为GenderEnum枚举实例
 * 适配场景：
 * 1. URL路径参数：/user/gender/2 → 自动转为GenderEnum.MALE
 * 2. 请求参数：?gender=-2 → 自动转为GenderEnum.FEMALE
 * 3. 表单提交的性别编码参数转换
 */
public class GenderEnumConverter implements Converter<String, GenderEnum> {

    /**
     * 转换核心逻辑（仅针对GenderEnum枚举设计）
     * @param source 前端传入的字符串类型性别编码（允许值：3/2/1/0/-1/-2/-3）
     * @return 匹配的 GenderEnum 枚举实例（不会返回 null）
     * @throws ForYourselfException 入参为空或非法时抛出明确异常提示
     */
    @Override
    public GenderEnum convert(String source) {
        // 1. 空值处理：性别为必填字段，不允许为空
        if (!StringUtils.hasText(source)) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "性别不能为空");
        }

        Integer code;
        try {
            // 2. 字符串转整数code（GenderEnum的核心匹配字段）
            code = Integer.parseInt(source);
        } catch (NumberFormatException e) {
            String messageData = "性别编码格式非法：" + source + "，仅支持整数（3=强男/2=男/1=弱男/0=未知/-1=弱女/-2=女/-3=强女）"+e;
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR,messageData);
        }

        // 3. 遍历GenderEnum所有枚举值，匹配code（仅针对该枚举的硬匹配逻辑）
        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }

        // 4. 无匹配code时抛出明确异常，便于问题定位
        throw  new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR,"性别编码值非法：" + code + "，合法值范围：3/2/1/0/-1/-2/-3");
    }
}