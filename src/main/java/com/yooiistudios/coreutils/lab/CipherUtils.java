package com.yooiistudios.coreutils.lab;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Dongheyon Jeong in AndroidPlayground from Yooii Studios Co., LTD. on 15. 8. 24.
 *
 * CipherUtils
 *  Simple cipher helper to obfuscate String in Java code.
 *
 *
 *  1. Encrypt
 *      private static final String data = "www.google.com";
 *
 *      String swappedData = CipherUtils.swap(key.getBytes(), data, 5);
 *      byte[] encrypted = CipherUtils.encrypt(keyBytes, swappedData.getBytes());
 *      String arrRep = CipherUtils.toArrayRepresentation(encrypted);
 *
 *      => private static final byte[] ENCRYPTED = new byte[] { arrRep };
 *
 *  2. Decrypt
 *      byte[] decrypted = CipherUtils.decrypt(keyBytes, ENCRYPTED);
 *      String originalData = CipherUtils.unswap(key.getBytes(), new String(decrypted), 5);
 */
// FIXME: Must be refactored
public class CipherUtils {
    private static final int KEY_LENGTH_IN_BYTES = 16;
    private static final byte PADDING = "0".getBytes()[0];

    private CipherUtils() {
        throw new AssertionError("You MUST NOT create the instance of this class!!");
    }

    public static byte[] encrypt(byte[] key, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(toBytesKey(key), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(clear);
    }

    public static byte[] decrypt(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(toBytesKey(key), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    public static String swap(byte[] keyBytes, String data, int count) {
        String swapped = data;
        for (int i = 0; i < count; i++) {
            swapped = swap(keyBytes, swapped);
        }
        return swapped;
    }

    public static String swap(byte[] keyBytes, String data) {
        byte[] targetBytes = data.getBytes();
        byte tempByte;
        for (byte keyByte : keyBytes) {
            int fromIndex = byteToInt(keyByte) % data.length();
            tempByte = targetBytes[0];
            targetBytes[0] = targetBytes[fromIndex];
            targetBytes[fromIndex] = tempByte;
        }

        return new String(targetBytes);
    }

    public static String unswap(byte[] keyBytes, String data, int count) {
        String swapped = data;
        for (int i = 0; i < count; i++) {
            swapped = unswap(keyBytes, swapped);
        }
        return swapped;
    }

    public static String unswap(byte[] keyBytes, String data) {
        byte[] targetBytes = data.getBytes();
        byte tempByte;
        for (int i = keyBytes.length - 1; i >= 0; i--) {
            int fromIndex = byteToInt(keyBytes[i]) % data.length();
            tempByte = targetBytes[0];
            targetBytes[0] = targetBytes[fromIndex];
            targetBytes[fromIndex] = tempByte;
        }

        return new String(targetBytes);
    }

    public static String toArrayRepresentation(byte[] bytes) {
        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < bytes.length; i++) {
            byte aByte = bytes[i];
            boolean isLast = i == bytes.length - 1;
//            sb.append(Integer.toBinaryString(aByte & 0xff)).append(isLast ? " }" : ", ");
            sb.append((int) aByte).append(isLast ? " }" : ", ");
//            Byte.parseByte(, 2);
        }
        return sb.toString();
    }

    private static byte[] toBytesKey(byte[] originalKeyBytes) {
        int originalKeyByteLength = originalKeyBytes.length;
        if (originalKeyByteLength == KEY_LENGTH_IN_BYTES) {
            return originalKeyBytes;
        }

        byte[] bytes = new byte[KEY_LENGTH_IN_BYTES];

        for (int i = 0; i < KEY_LENGTH_IN_BYTES; i++) {
            if (i < originalKeyBytes.length) {
                bytes[i] = originalKeyBytes[i];
            } else {
                bytes[i] = PADDING;
            }
        }
        return bytes;
    }

    private static int byteToInt(byte value) {
        return value & 0xff;
    }
}
