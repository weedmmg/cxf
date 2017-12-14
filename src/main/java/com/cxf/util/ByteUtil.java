package com.cxf.util;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteUtil {

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    public static String printHexString(byte[] b) {

        String h = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            h = h + " " + hex.toUpperCase();
        }
        return h;

    }

    /*
     * 16进制字符串转字节数组
     */
    public static byte[] hexString2Bytes(String hex) {

        if ((hex == null) || (hex.equals(""))) {
            return null;
        } else if (hex.length() % 2 != 0) {
            return null;
        } else {
            hex = hex.toUpperCase();
            int len = hex.length() / 2;
            byte[] b = new byte[len];
            char[] hc = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
            }
            return b;
        }

    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * 
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] byteSub(byte[] byte_1, int begin, int end) {
        byte[] byte_3 = new byte[end - begin];
        System.arraycopy(byte_1, begin, byte_3, 0, end - begin);
        return byte_3;
    }

    /**
     * 鏁村瀷杞崲涓�4浣嶅瓧鑺傛暟缁�
     * 
     * @param intValue
     * @return
     */
    public static byte[] intToByteArray(int number, int size) {

        byte[] result = new byte[size];

        for (int i = 0; i < size; i++) {
            result[i] = (byte) (number >> 8 * (size - 1 - i) & 0xFF);
        }

        return result;
    }

    /**
     * byte[]杞琲nt
     * 
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes, int size) {

        int intValue = 0;
        for (int i = 0; i < bytes.length; i++) {
            intValue += (bytes[i] & 0xFF) << (8 * (size - 1 - i));
        }
        return intValue;
    }

    /**
     * 鏁村瀷杞崲涓�8浣嶅瓧鑺傛暟缁�
     * 
     * @param intValue
     * @return
     */
    // public static byte[] longToByteArray(long number, int size) {
    //
    // byte[] result = new byte[size];
    //
    // for (int i = 0; i < size; i++) {
    // result[i] = (byte) (number >> 8 * (size - 1 - i) & 0xFF);
    // }
    //
    // return result;
    // }

    /**
     * byte[]杞琹ong
     * 
     * @param bytes
     * @return
     */
    // public static long byteArrayToLong(byte[] bytes, int size) {
    // long value = 0;
    // for (int i = 0; i < bytes.length; i++) {
    // value += (bytes[i] & 0xFF) << (8 * (size - 1 - i));
    // }
    //
    // return value;
    // }

    /**
     * byte[]杞琹ong
     * 
     * @param bytes
     * @return
     */
    public static long byteArrayToLong(byte[] bytes, int size) {
        long value = 0;

        for (int i = bytes.length - 1; i >= 0; i--) {
            value <<= 8;
            value |= (bytes[i] & 0xFF);
        }

        return value;
    }

    /**
     * 鏁村瀷杞崲涓�8浣嶅瓧鑺傛暟缁�
     * 
     * @param intValue
     * @return
     */
    public static byte[] longToByteArray(long number, int size) {

        byte[] result = new byte[size];

        for (int i = 0; i < size; i++) {
            result[i] = (byte) ((number >> (8 * i)) & 0xFF);
        }

        return result;
    }

    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    /**
     * 校验和
     * 
     * @param msg
     *            需要计算校验和的byte数组
     * @param length
     *            校验和位数
     * @return 计算出的校验和数组
     */
    public static byte[] sumCheck(byte[] msg, int length) {
        long mSum = 0;
        byte[] mByte = new byte[length];

        /** 逐Byte添加位数和 */
        for (byte byteMsg : msg) {
            long mNum = ((long) byteMsg >= 0) ? (long) byteMsg : ((long) byteMsg + 256);
            mSum += mNum;
        }
        /** end of for (byte byteMsg : msg) */

        /** 位数和转化为Byte数组 */
        for (int liv_Count = 0; liv_Count < length; liv_Count++) {
            mByte[length - liv_Count - 1] = (byte) (mSum >> (liv_Count * 8) & 0xff);
        }
        /** end of for (int liv_Count = 0; liv_Count < length; liv_Count++) */

        return mByte;
    }

    public static void main(String[] args) throws IOException {

        String errorInfo = // "ecbc19011411a1b101600501100019461411a1b101f02568".toUpperCase();
        "bc02ae01160114110b150a2e27007801f41000000000ad7914110b111039151000000000ae7214110b1110391810000001c0017014110b111039181000000000af8014110b1110391a1000000000b07214110b1110391c100000003a756c14110b1110391d1000000000b16b14110b1110391e1000000000b27114110b1110390310000001c0047614110b111039031000000000b38114110b111039051000000108b97814110b111039061000000000b49214110b111039071000000000b58214110b1110390a10000001c0017d14110b1110390b1000000000b67d14110b1110390c100000003a757e14110b1110390c1000000000b76c14110b1110390e10000001c0047f14110b111039101000000000b87b14110b111039101000000000b97614110b111039131000000000ba8014110b111039151000000000bb8314110b11103917100000003a758014110b111039191000000000bc6e14110b111039191000000000bd8114110b1110391c10000001c0048414110b1110391c1000000000be7414110b1110391e1000000000bf7414110b111039201000000000c07414110b111039221000000000c17a14110b111039251000000108b98314110b11103925100000003a759814110b111039251000000000c27014110b1110392710000001c0048014110b111039291000000000c38614110b111039291000000000c47d14110b1110392c1000000000c57e14110b1110392e1000000000c67414110b11103930100000003a757414110b111039321000000000c76d14110b1110393210000001c0046414110b1110393510000001c0017714110b111039351000000000657d14110b111039371000000000677c14110b1110393b1000000000687c14110b11103a021000000000697c14110b11103a0410000001c0047c14110b11103a0610000000006a7e14110b11103a06"
                .toUpperCase();

        // "ECBE0D010101026E770160AA1F012AE5EF68";
        // "ECBE0D010101026E770160AA1F012AE5EF68EC05000568ECBE0D010101026E770160AA1F012AE5EF68EC05000568ECBE0D010101026E770160AA1F012AE5EF68";
        // "770160AA1F012AE5EF68ECBE0D010101026E770160AA1F012AE5EF68EC05000568ECBE0D010101026E770160AA1F012AE5EF68ECBE0D0101EC0500BE68ECBE0D010101026E770160AA1F012AE5EF68EC05000568ECBE0D010101026E770160AA1F012AE5EF";
        byte[] array = ByteUtil.hexString2Bytes(errorInfo);
        byte[] msgSign = ByteUtil.sumCheck(array, 1);
        System.out.println(ByteUtil.printHexString(msgSign).trim());
        // 鍒濆鍖栨祴璇曟暟鎹�
        // byte[] msg = new byte[429], b = new byte[1];
        // for (int i = 0; i < msg.length; i++) {
        // msg[i] = 0x30;
        // }
        //
        // b[0] = 0x01;
        // System.out.println(b[0]);
        // int length = msg.length;
        // byte[] intByte = intToByteArray(length, 3);
        // System.out.println(byteArrayToInt(intByte, intByte.length));
        // // 缁勮鎶ユ枃澶撮儴
        // byte[] b2;
        // if (length < 256) {
        // b[0] = 0x10;
        // byte[] l = new byte[1];
        // l[0] = (byte) (length & 0xFF);
        // b2 = ByteUtil.byteMerger(b, l);
        // } else if (length < 64 * 1024) {
        // b[0] = 0x20;
        // byte[] l = new byte[2];
        // l[0] = (byte) (length & 0xFF);
        // l[1] = (byte) ((length >> 8) & 0xFF);
        // b2 = ByteUtil.byteMerger(b, l);
        // } else {
        // b[0] = 0x30;
        // byte[] l = new byte[3];
        // l[0] = (byte) (length & 0xFF);
        // l[1] = (byte) ((length >> 8) & 0xFF);
        // l[2] = (byte) ((length >> 16) & 0xFF);
        // b2 = ByteUtil.byteMerger(b, l);
        // }
        //
        // byte[] b3 = ByteUtil.byteMerger(b2, msg);
        //
        // // 瑙ｆ瀽PB鐨勯暱搴�
        // int dataLength = 0;
        // int i = 0, i2 = 0, i3 = 0;
        // if (b3[0] >= 16) {
        // i = b3[1];
        // }
        // if (b3[0] >= 32) {
        // i2 = b3[2] << 8;
        // }
        // if (b3[0] == 48) {
        // i3 = b3[3] << 16;
        // }
        // dataLength = i + i2 + i3;
        //
        // // 鎶奝B鏁版嵁鏀惧埌DATA涓�
        // byte[] data = new byte[dataLength];
        // System.arraycopy(data, 0, b3, b3.length - dataLength, dataLength);
        // System.out.println(data.length);
        byte[] result = longToByteArray(-10002l, 4);
        long cha = byteArrayToLong(result, 4);
        System.out.println(cha);
    }
}
