package com.example.classtable.custom;

import android.content.Context;
import android.content.SharedPreferences;

public class ShareUtil {
    private static ShareUtil mUtil;
    private SharedPreferences preferences;
    public static ShareUtil getInstance(Context context)
    {
        if(mUtil==null)
        {
            mUtil=new ShareUtil();
            mUtil.preferences=context.getSharedPreferences("shop",Context.MODE_PRIVATE);
        }
        return mUtil;
    }
    public void write(String key,Boolean value)
    {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }
    public Boolean read(String key,Boolean defaultvalue)
    {
        return preferences.getBoolean(key,defaultvalue);
    }
}
