package com.example.classtable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.baidu.ocr.ui.crop.CropView;
import com.baidu.ocr.ui.crop.FrameOverlayView;
import com.example.classtable.databinding.ActivityCropBinding;

import java.io.FileOutputStream;
import java.io.IOException;


public class CropActivity extends AppCompatActivity {
    private ActivityCropBinding binding;
    private CropView cropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crop);
        // 初始化 ViewBinding
        binding = ActivityCropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        cropView = binding.cropContainer.findViewById(com.baidu.ocr.R.id.crop_view);
        cropView.setFilePath(path);

        binding.cropContainer.findViewById(com.baidu.ocr.R.id.confirm_button).setOnClickListener(v -> {
            FrameOverlayView overlayView = binding.cropContainer.findViewById(com.baidu.ocr.R.id.overlay_view);
            Rect rect;
            rect = overlayView.getFrameRect();
            Bitmap cropped = cropView.crop(rect);
            try {
                FileOutputStream fos = this.openFileOutput("img.png", this.MODE_PRIVATE);
                // 将Bitmap压缩为PNG格式并写入输出流
                cropped.compress(Bitmap.CompressFormat.PNG, 100, fos);
                // 关闭输出流
                fos.close();
                finish();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("SaveImage", "图片保存失败", e);
            }
        });
        binding.cropContainer.findViewById(com.baidu.ocr.R.id.cancel_button).setOnClickListener(v -> {
            finish();
        });
        binding.cropContainer.findViewById(com.baidu.ocr.R.id.rotate_button).setOnClickListener(v->{
            cropView.rotate(90);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}