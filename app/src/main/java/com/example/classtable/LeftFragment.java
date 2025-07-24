package com.example.classtable;

import static android.app.Activity.RESULT_OK;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.classtable.custom.GlideEngine;
import com.example.classtable.databinding.FragmentLeftBinding;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.io.File;
import java.util.ArrayList;


public class LeftFragment extends Fragment {

    private FragmentLeftBinding binding;


    private SharedPreferences SavePath;
    private String path;
    private int currentRotation = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLeftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SavePath = requireContext().getSharedPreferences("SavePath", Context.MODE_PRIVATE);
        binding.photoView.setMaximumScale(5.0f); // 设置最大缩放比例为 5 倍
        binding.ivInfoPic.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 没有权限，请求权限
                requestPermissions(
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE
                        },
                        1
                );
            } else {
                openImageChooser();
            }
        });
        // 获取文件对象
        File file = new File(getActivity().getFilesDir(), "img.png");
        // 检查文件是否存在
        if (file.exists()) {
            // 更新 UI
            binding.rotateBt.setVisibility(View.VISIBLE);
            binding.rl.setVisibility(View.GONE);
            binding.photoView.setVisibility(View.VISIBLE);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            binding.photoView.setImageBitmap(bitmap);
        }

       /* path = SavePath.getString("path", "6");
        if (!path.equals("6")) {
            binding.rotateBt.setVisibility(View.VISIBLE);
            binding.rl.setVisibility(View.GONE);
            binding.photoView.setVisibility(View.VISIBLE);
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  // 禁用磁盘缓存
                    .skipMemoryCache(true);                     // 禁用内存缓存
            Glide.with(LeftFragment.this)
                    .load(path)
                    .apply(requestOptions)
                    .centerInside()
                    .format(DecodeFormat.PREFER_ARGB_8888) // 设置解码格式为 ARGB_8888
                    .into(binding.photoView);
        }*/
        binding.photoView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PicHolderAcitivity.class);
          /*  intent.putExtra("path", path);*/
            startActivity(intent);
        });
        binding.rotateBt.setOnClickListener(v -> {
            // 每次增加 90 度旋转
            currentRotation += 90;
            // 确保旋转角度在 0 - 360 度之间
            currentRotation = currentRotation % 360;
            // 设置旋转角度
            binding.photoView.setRotation(currentRotation);
          /*  adjustScaleToFit();*/
        });
        binding.photoView.setOnLongClickListener(v -> {
            binding.rotateBt.setVisibility(View.GONE);
            binding.photoView.setVisibility(View.GONE);
            binding.rl.setVisibility(View.VISIBLE);
            binding.photoView.setImageDrawable(null);
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            File imgfile = new File(getActivity().getFilesDir(), "img.png");
            if (!imgfile.exists()) {
                // 文件不存在，进行相应处理
                return;
            }
            binding.rotateBt.setVisibility(View.VISIBLE);
            binding.rl.setVisibility(View.GONE);
            binding.photoView.setVisibility(View.VISIBLE);
            // 更新 UI
            binding.rl.setVisibility(View.GONE);
            binding.photoView.setVisibility(View.VISIBLE);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(imgfile.getAbsolutePath(), options);
            binding.photoView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   private void adjustScaleToFit() {
       // 获取 PhotoView 的宽度和高度
       int viewWidth = binding.photoView.getWidth();
       int viewHeight = binding.photoView.getHeight();

       // 获取图片的宽度和高度
       int drawableWidth = binding.photoView.getDrawable().getIntrinsicWidth();
       int drawableHeight = binding.photoView.getDrawable().getIntrinsicHeight();

       // 计算旋转后的图片尺寸
       double rotatedWidth;
       double rotatedHeight;
       if (currentRotation % 180 == 90) {
           rotatedWidth = drawableHeight;
           rotatedHeight = drawableWidth;
       } else {
           rotatedWidth = drawableWidth;
           rotatedHeight = drawableHeight;
       }

       // 计算缩放比例
       float scaleX = (float) viewWidth / (float) rotatedWidth;
       float scaleY = (float) viewHeight / (float) rotatedHeight;
       float scale = Math.min(scaleX, scaleY);
       // 获取 PhotoView 的最小和最大缩放比例
       float minScale = binding.photoView.getMinimumScale();
       float maxScale = binding.photoView.getMaximumScale();
       // 确保缩放比例在允许的范围内
       scale = Math.max(minScale, Math.min(scale, maxScale));
       // 设置缩放级别
       binding.photoView.setScale(scale, true);
   }
    private void openImageChooser() {
        PictureSelector.create(getActivity())
                .openGallery(SelectMimeType.ofImage())
                .setMaxSelectNum(1)
                .setImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        String path = result.get(0).getRealPath();
                        Intent intent = new Intent(getActivity(), CropActivity.class);
                        // 添加参数
                        intent.putExtra("path", path);
                        // 启动目标Activity，并传递请求码
                        startActivityForResult(intent, 666);
                       /* binding.rl.setVisibility(View.GONE);
                        binding.photoView.setVisibility(View.VISIBLE);
                        binding.rotateBt.setVisibility(View.VISIBLE);
                        path = result.get(0).getPath();
                        RequestOptions requestOptions = new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)  // 禁用磁盘缓存
                                .skipMemoryCache(true);                     // 禁用内存缓存
                        Glide.with(LeftFragment.this)
                                .load(path)
                                .apply(requestOptions)
                                .centerInside()
                                .format(DecodeFormat.PREFER_ARGB_8888) // 设置解码格式为 ARGB_8888
                                .into(binding.photoView);
                        SharedPreferences.Editor editor = SavePath.edit();
                        editor.putString("path", path);
                        editor.apply();*/
                    }

                    @Override
                    public void onCancel() {
                        binding.rotateBt.setVisibility(View.GONE);
                        binding.photoView.setVisibility(View.GONE);
                        binding.rl.setVisibility(View.VISIBLE);
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 已经有权限，执行相关操作
                openImageChooser();
            } else {
                // 用户拒绝了权限，可以给用户一些提示
                Toast.makeText(getActivity(), "需要读取存储权限才能选择图片呢", Toast.LENGTH_SHORT).show();
            }
        }
    }
}