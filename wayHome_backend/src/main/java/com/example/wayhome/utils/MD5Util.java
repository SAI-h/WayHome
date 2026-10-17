package com.example.wayhome.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 摘要工具类。
 *
 * @author WayHome
 */
public final class MD5Util {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private MD5Util() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 对字符串进行 MD5 摘要计算，返回 32 位小写十六进制字符串。
     *
     * @param source 原始字符串
     * @return MD5 摘要
     */
    public static String encrypt(String source) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(source.getBytes(StandardCharsets.UTF_8));
            return toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持 MD5 算法", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String toHexString(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            chars[index++] = HEX_CHARS[b >>> 4 & 0x0F];
            chars[index++] = HEX_CHARS[b & 0x0F];
        }
        return new String(chars);
    }
}
