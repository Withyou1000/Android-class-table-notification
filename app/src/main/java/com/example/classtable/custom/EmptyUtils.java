package com.example.classtable.custom;

import android.text.TextUtils;


public class EmptyUtils {
    public static boolean isEmpty(String target) {
        return !(target != null && !TextUtils.isEmpty(target));
    }

    public static boolean isNotEmpty(String target) {
        return target != null && !TextUtils.isEmpty(target);
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static String NullToEmpty(String obj) {
        if (isNull(obj))
            return "";
        else
            return obj;
    }


}
