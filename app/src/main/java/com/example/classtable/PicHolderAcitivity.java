package com.example.classtable;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.classtable.databinding.ActivityCropBinding;
import com.example.classtable.databinding.ActivityPicHolderAcitivityBinding;

import java.io.File;

public class PicHolderAcitivity extends AppCompatActivity {
    private ActivityPicHolderAcitivityBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消过渡动画
        overridePendingTransition(0, 0);
        EdgeToEdge.enable(this);
        binding = ActivityPicHolderAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
     /*   Intent intent=getIntent();
        String path =intent.getStringExtra("path");*/
        File imgfile = new File(this.getFilesDir(), "img.png");
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // 禁用磁盘缓存
                .skipMemoryCache(true);                     // 禁用内存缓存
        Glide.with(this)
                .load(imgfile)
                .apply(requestOptions)
                .centerInside()
                .into(binding.photoView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}