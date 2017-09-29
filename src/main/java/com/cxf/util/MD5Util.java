package com.cxf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

	 /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    public static String file2MD5(File file) {
        if (file == null || !file.exists()) {
            return "";
        }
        try {
            InputStream is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while ((len = is.read(bytes)) > 0) {
                messagedigest.update(bytes, 0, len);
            }
            is.close();
            byte[] md5Bytes = messagedigest.digest();
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 鍔犲瘑瑙ｅ瘑绠楁硶 鎵ц涓�娆″姞瀵嗭紝涓ゆ瑙ｅ瘑
     */
    public static String convertMD5(String inStr) {

        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;

    }
    
    /**
     * 对byte类型的数组进行MD5加密
     * 
     * @author 高焕杰
     */
    public static byte[] getMD5String(byte[] bytes) {
    	 MessageDigest messagedigest;
		try {
			messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(bytes);  
            return messagedigest.digest(); 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
             
    }

    

    // 娴嬭瘯涓诲嚱鏁�
    public static void main(String args[]) {
    	String headStr="ex";
    	try {
			byte[] head = headStr.getBytes("utf8");
			
			byte[] md5=MD5Util.getMD5String(head);
			
			String md5Str = new String(head);
			
			String md5Str2=MD5Util.string2MD5(headStr);
			
			
			//ByteUtil.printHexString(head);
			System.out.println(md5Str2.getBytes("utf8").length);
			//ByteUtil.printHexString(md5);
			
			System.out.println(md5Str);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	
        String s = new String("AppID=GESRT94OJO3&AppSecret=twotfv89794ol&partner_sys_customer_key=credit_1461504531&timestamp=1460009866");
        //String s = new String("AppID=GESRT94OJO3&AppSecret=twotfv89794ol&partner_sys_customer_key=credit_1461504531&timestamp=1460009866");
        System.out.println("原始：" + s);
        System.out.println("MD5后：" + string2MD5(s));
        System.out.println("加密的：" + convertMD5(s));
        System.out.println("解密的：" + convertMD5(convertMD5(s)));

    }
}
