package com.yuigneel.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * 微服务跨模块内网调用通用一次性短时防伪通行令牌鉴权工具类
 * 核心适配场景：管理员A模块Feign远程调用B模块专属受限私有接口，防伪造冒充、防本地私自调用越权拦截校验
 * 灵活定制规则：加密原始密钥、令牌有效过期时长全部外部传入自定义，无内置固定硬编码常量
 * 强制互通要求：同一条跨服务调用链路，两端传入的【原始加密明文密钥、过期毫秒时长】必须100%完全一致
 *
 * @author 自定义开发
 */
public class InnerFlexibleTokenSecurityUtil {

    /**
     * 动态自定义生成内网一次性加密通行令牌
     * 自动获取系统当前毫秒级时间戳，拼接约定明文密钥SHA256不可逆加密，封装组装完整合规令牌返回
     * 调用位置：上游A管理员模块Service业务层，发起Feign远程跨模块请求前调用生成
     *
     * @param secretRawKey 双方提前私密约定的原始加密明文密钥 自定义复杂度长度 严禁外泄公开
     * @param expireMillis 令牌自定义全局有效过期时长 单位：毫秒 推荐设置3-10分钟短时有效期防抓包重放攻击
     * @return 拼接【生成时间戳|SHA256加密签名】格式完整可直接传输使用的临时通行Token字符串
     */
    public static  String generateToken(String secretRawKey, long expireMillis) {
        // 获取令牌生成瞬间精准系统毫秒级时间戳
        long currentCreateTimestamp = System.currentTimeMillis();
        // 固定全局统一加密拼接顺序：私密原始密钥 + 实时生成时间戳 顺序严禁调换修改
        String encryptSourceText = secretRawKey + currentCreateTimestamp;
        // SHA256哈希不可逆加密生成防伪唯一签名串
        String signHex = new Digester(DigestAlgorithm.SHA256).digestHex(encryptSourceText);
        // 封装拼接完整令牌，自带原生生成时间戳，下游校验自动拆分无感处理
        return currentCreateTimestamp + "|" + signHex;
    }

    /**
     * 全自动化闭环校验上游传递的通行令牌合法性有效性
     * 入参自定义约定密钥+约定过期时长+接收完整Token，内部自动完成格式校验、篡改拦截、时效过期判断、签名真伪复核全流程
     * 调用位置：下游B模块接收跨模块内网请求后，业务逻辑执行前优先执行全校验拦截
     *
     * @param secretRawKey 与上游生成令牌完全一致的约定私密原始加密明文密钥
     * @param expireMillis 与上游生成令牌完全一致的约定令牌有效过期毫秒时长
     * @param receiveFullToken 上游请求头中携带传递过来的完整加密通行 Token字符串
     * @return true 令牌合规未过期签名匹配合法放行 | false 空值篡改过期伪造非法请求直接拦截拒绝访问
     */
    public static boolean verifyToken(String secretRawKey, long expireMillis, String receiveFullToken) {
        // 第一层基础兜底校验：令牌非空合规格式校验
        if (StrUtil.isBlank(receiveFullToken) || !receiveFullToken.contains("|")) {
            return false;
        }

        // 拆分令牌内置原生生成时间戳与加密防伪签名
        String[] tokenInfoArr = StrUtil.splitToArray(receiveFullToken, "|");
        // 校验令牌完整性，防止恶意篡改裁剪内容
        if (tokenInfoArr.length != 2) {
            return false;
        }

        long tokenCreateTimestamp;
        String receiveSignStr;
        try {
            // 解析提取令牌原生生成时间戳与附带签名内容
            tokenCreateTimestamp = Long.parseLong(tokenInfoArr[0]);
            receiveSignStr = tokenInfoArr[1];
        } catch (NumberFormatException e) {
            // 时间戳格式被非法篡改直接判定鉴权失败
            return false;
        }

        // 第二层时效防重放攻击校验：判断令牌是否超出约定有效时长已过期失效
        long nowCurrentTime = System.currentTimeMillis();
        if (nowCurrentTime - tokenCreateTimestamp > expireMillis) {
            return false;
        }

        // 第三层核心真伪签名匹配校验：本地同源规则重算标准签名全等比对防伪防伪造
        String standardSourceText = secretRawKey + tokenCreateTimestamp;
        String standardCorrectSign = new Digester(DigestAlgorithm.SHA256).digestHex(standardSourceText);
        return standardCorrectSign.equalsIgnoreCase(receiveSignStr);
    }

}