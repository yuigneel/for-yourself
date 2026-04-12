package com.yulgnier.common.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Scanner;

/**
 * Jasypt 加解密交互工具
 * <p>
 * 使用方式：直接运行 main 方法，根据控制台提示操作
 * </p>
 *
 * @author Yu·Lgnier
 */
public final class JasyptGenUtil {

    // 定义加密算法（必须与 application.yml 中的 jasypt.encryptor.algorithm 保持一致）
    private static final String ALGORITHM = "PBEWithMD5AndDES";

    /**
     * 主入口：交互式加解密工具
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            printHeader();

            // 1. 选择操作模式
            String mode = chooseMode(scanner);

            // 2. 输入根密钥
            System.out.print("\n请输入根密钥（ROOT_KEY）：");
            String rootKey = scanner.nextLine().trim();

            if (rootKey.isEmpty()) {
                throw new IllegalArgumentException("错误：根密钥不能为空！");
            }

            // 3. 输入待处理内容
            System.out.print(mode.equals("1") ? "请输入待加密的明文：" : "请输入待解密的密文：");
            String content = scanner.nextLine().trim();

            if (content.isEmpty()) {
                throw new IllegalArgumentException("错误：待处理内容不能为空！");
            }

            // 4. 执行加解密
            System.out.println("\n正在处理...");
            String result = process(rootKey, content, mode);

            // 5. 输出结果
            printResult(result, mode);

        } catch (IllegalArgumentException e) {
            System.err.println("\n❌ 参数错误：" + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n❌ 处理失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            System.out.println("\n感谢使用，再见！");
        }
    }

    /**
     * 打印欢迎界面
     */
    private static void printHeader() {
        System.out.println("\n========================================");
        System.out.println("  Jasypt 加解密交互工具 v1.0");
        System.out.println("  Author: Yu·Lgnier");
        System.out.println("========================================");
    }

    /**
     * 选择操作模式（加密/解密）
     *
     * @param scanner 扫描器
     * @return "1" 表示加密，"2" 表示解密
     */
    private static String chooseMode(Scanner scanner) {
        System.out.println("\n请选择操作模式：");
        System.out.println("  [1] 加密（明文 → 密文）");
        System.out.println("  [2] 解密（密文 → 明文）");
        System.out.print("\n请输入选项（1 或 2）：");

        String choice = scanner.nextLine().trim();

        if (!"1".equals(choice) && !"2".equals(choice)) {
            throw new IllegalArgumentException("无效选项，请输入 1（加密）或 2（解密）！");
        }

        return choice;
    }

    /**
     * 执行加解密操作
     *
     * @param rootKey 根密钥
     * @param content 待处理内容
     * @param mode    模式："1"=加密，"2"=解密
     * @return 处理结果
     */
    private static String process(String rootKey, String content, String mode) {
        try {
            // 创建加密器
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setAlgorithm(ALGORITHM);
            encryptor.setPassword(rootKey);

            // 根据模式执行对应操作
            if ("1".equals(mode)) {
                return encryptor.encrypt(content);
            } else {
                return encryptor.decrypt(content);
            }
        } catch (Exception e) {
            throw new RuntimeException("加解密失败，请检查密钥和内容是否正确！原因：" + e.getMessage(), e);
        }
    }

    /**
     * 打印处理结果
     *
     * @param result 处理结果
     * @param mode   模式："1"=加密，"2"=解密
     */
    private static void printResult(String result, String mode) {
        System.out.println("\n========================================");

        if ("1".equals(mode)) {
            System.out.println("✅ 加密成功");
            System.out.println("----------------------------------------");
            System.out.println("密文：");
            System.out.println(result);
        } else {
            System.out.println("✅ 解密成功");
            System.out.println("----------------------------------------");
            System.out.println("明文：");
            System.out.println(result);
        }

        System.out.println("========================================");
    }
}
