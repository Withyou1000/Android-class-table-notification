package com.example.classtable;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.classtable.custom.BatteryOptimizationHelper;
import com.example.classtable.databinding.ActivityCropBinding;
import com.example.classtable.databinding.ActivityExplainBinding;

public class ExplainActivity extends AppCompatActivity {
    private ActivityExplainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityExplainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String fullText = binding.tvNotice.getText().toString();
        SpannableString spannableString = new SpannableString(fullText);

// 1. 找到 "请点这里" 的起始和结束位置
        int start1 = fullText.indexOf("请点这里");
        int end1 = start1 + "请点这里".length();

// 2. 找到 "点击这里" 的起始和结束位置
        int start2 = fullText.indexOf("点击这里");
        int end2 = start2 + "点击这里".length();

// 3. 为 "请点这里" 设置下划线和颜色
        spannableString.setSpan(new UnderlineSpan(), start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 4. 为 "点击这里" 设置下划线和颜色
        spannableString.setSpan(new UnderlineSpan(), start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 5. 为 "请点这里" 设置点击事件
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onNoticeLinkClicked(); // 执行第一个点击事件
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // 保持下划线
                ds.setColor(Color.parseColor("#FF0000")); // 设置颜色
            }
        }, start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 6. 为 "点击这里" 设置点击事件
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onNoticeLinkClicked2(); // 执行第二个点击事件
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // 保持下划线
                ds.setColor(Color.parseColor("#FF0000")); // 设置颜色
            }
        }, start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 7. 设置 TextView 可点击
        binding.tvNotice.setText(spannableString);
        binding.tvNotice.setMovementMethod(LinkMovementMethod.getInstance()); // 允许点击
        binding.tvNotice.setHighlightColor(Color.TRANSPARENT); // 移除点击时的背景色

// 应用 SpannableString
        binding.tvNotice.setText(spannableString);
        binding.tvNotice.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvNotice.setHighlightColor(Color.TRANSPARENT); // 去除点击背景高亮

    }

    private void onNoticeLinkClicked2() {
        BatteryOptimizationHelper.showBatteryOptimizationDialog(this);
    }

    private void onNoticeLinkClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE,this.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, "jjc");
            this.startActivity(intent);
        }
    }

}