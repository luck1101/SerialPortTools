package cn.yyongquan.serial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String toHexString(byte b) {
        String hex = Integer.toHexString(b);
        hex = hex.toUpperCase();
        if (hex.length() < 2) {
            return "0" + hex;
        } else {
            return hex;
        }
    }

    public static String toHexString(byte[] bs, int size) {
        StringBuffer sb = new StringBuffer();

        if (size > bs.length) size = bs.length;

        for (int i = 0; i < size; i++) {
            sb.append(Utils.toHexString(bs[i]));
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String getData() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return formatter.format(curDate);
    }
}
