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

        double ll = 121.552298, la = 29.820054;

        long ll1 = 121552298, la1 = 29820054;

        byte[] lls = ByteUtil.toByteArray(ll), las = ByteUtil.toByteArray(la);

        byte[] ll1s = ByteUtil.longToByteArray(ll1, 4), las1 = ByteUtil.longToByteArray(la1, 4);

        System.out.println(ByteUtil.byteArrayToInt(ByteUtil.intToByteArray(-1, 1), 1) + "::" + las1.length);
        System.out.println(ByteUtil.byteArrayToLong(ll1s, 4) + "::" + ByteUtil.byteArrayToLong(las1, 4));

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
