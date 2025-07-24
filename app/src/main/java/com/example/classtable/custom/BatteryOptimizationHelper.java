package com.example.classtable.custom;// BatteryOptimizationHelper.java

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import com.example.classtable.R;

public class BatteryOptimizationHelper {

    // 检测是否启用了电池优化（需要关闭才能保活）
    public static boolean isBatteryOptimizationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return !powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return false;
    }

    // 显示引导用户关闭电池优化的对话框
    public static void showBatteryOptimizationDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("后台运行权限申请")
                .setMessage("为了确保消息及时推送，请允许应用在后台运行")
                .setPositiveButton("去设置", (dialog, which) -> openBatteryOptimizationSettings(activity))
                .setNegativeButton("取消", null)
                .show();
    }

    // 打开系统电池优化设置页面
    private static void openBatteryOptimizationSettings(Context context) {
            openManufacturerSettings(context); // 适配国产系统
    }

    // 适配国产手机厂商的后台设置
    private static void openManufacturerSettings(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        try {
            if (manufacturer.contains("xiaomi")) {
                openXiaomiSettings(context);
            } else if (manufacturer.contains("huawei")) {
                openHuaweiSettings(context);
            } else if (manufacturer.contains("oppo")) {
                openOppoSettings(context);
            } else if (manufacturer.contains("vivo")) {
                openVivoSettings(context);
            } else {
                openDefaultSettings(context);
            }
        } catch (Exception e) {
            openDefaultSettings(context);
        }
    }

    // 小米的后台设置
    private static void openXiaomiSettings(Context context) throws Exception {
        Intent intent = new Intent();
        intent.setClassName(
                "com.miui.powerkeeper",
                "com.miui.powerkeeper.ui.HiddenAppsConfigActivity");
        intent.putExtra("package_name", context.getPackageName());
        intent.putExtra("package_label", context.getString(R.string.app_name));
        context.startActivity(intent);
    }

    // 华为的后台设置
    private static void openHuaweiSettings(Context context) throws Exception {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        intent.putExtra("packageName", context.getPackageName());
        context.startActivity(intent);
    }

    // OPPO的后台设置
    private static void openOppoSettings(Context context) throws Exception {
        Intent intent = new Intent();
        intent.setClassName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity");
        context.startActivity(intent);
    }

    // Vivo的后台设置
    private static void openVivoSettings(Context context) throws Exception {
        try {
            // 方案1：直接跳转后台高耗电管理页
            Intent intent = new Intent();
            intent.setClassName(
                    "com.vivo.abe", // 包名
                    "com.vivo.abe.ui.power.PwManControlActivity" // Activity路径
            );
            // 关键参数：直接定位到本应用的后台耗电开关
            intent.putExtra("packageName", context.getPackageName());
            context.startActivity(intent);
        } catch (Exception e) {
            // 方案2：旧版本备用路径
            Intent fallbackIntent = new Intent();
            fallbackIntent.setClassName(
                    "com.vivo.pem",
                    "com.vivo.pem.ui.activity.PowerConsumptionActivity"
            );
            context.startActivity(fallbackIntent);
        }
    }


    // 默认打开应用详情页
    private static void openDefaultSettings(Context context) {
        // 弹出悬浮窗或Toast提示手动路径
        Toast.makeText(context,
                "跳转未果，还望君移步至设置，细细查探一二",
                Toast.LENGTH_LONG).show();
    }
}