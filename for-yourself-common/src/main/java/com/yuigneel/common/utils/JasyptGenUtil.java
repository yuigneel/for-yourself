/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.yuigneel.common.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import java.util.Scanner;

public final class JasyptGenUtil {

    private static final String ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            printHeader();
            String mode = chooseMode(scanner);

            System.out.print("\n请输入根密钥（ROOT_KEY）：");
            String rootKey = scanner.nextLine().trim();

            System.out.print(mode.equals("1") ? "请输入待加密的明文：" : "请输入待解密的密文：");
            String content = scanner.nextLine().trim();

            System.out.println("\n正在处理...");
            String result = process(rootKey, content, mode);
            printResult(result, mode);

        } catch (Exception e) {
            System.err.println("\n❌ 失败：" + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void printHeader() {
        System.out.println("\n========================================");
        System.out.println("  Jasypt 加解密工具（Spring Boot3 专用）");
        System.out.println("========================================");
    }

    private static String chooseMode(Scanner scanner) {
        System.out.println("\n[1]加密  [2]解密");
        System.out.print("请输入选项：");
        String choice = scanner.nextLine().trim();
        if (!"1".equals(choice) && !"2".equals(choice)) {
            throw new IllegalArgumentException("请输入1或2！");
        }
        return choice;
    }

    private static String process(String rootKey, String content, String mode) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(ALGORITHM);
        encryptor.setPassword(rootKey);
        // ✅【修复】Spring Boot3 必须加这行！之前就是缺了它！
        encryptor.setIvGenerator(new RandomIvGenerator());

        return "1".equals(mode) ? encryptor.encrypt(content) : encryptor.decrypt(content);
    }

    private static void printResult(String result, String mode) {
        System.out.println("\n========================================");
        System.out.println("✅ 处理成功！");
        System.out.println("结果：\n" + result);
        System.out.println("========================================");
    }
}