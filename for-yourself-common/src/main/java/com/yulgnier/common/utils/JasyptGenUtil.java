package com.yulgnier.common.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public final class JasyptGenUtil {
    private static final String rootKey = "";  // 根密钥,用完即删！！！
    private static final String plainText = ""; // 待加密内容，用完即删！！！

    public static void main(String[] args) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        // 算法默认即可
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        // 这里填你的【根密钥】（之后存在环境变量里）
        encryptor.setPassword(rootKey);

        // 要加密的内容：你的JWT密钥
        String encSecret = encryptor.encrypt(plainText);

        System.out.println("密文：" + encSecret);
    }
}
