/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.ocr.ui.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.baidu.ocr.ui.util.ImageUtil;

import java.io.IOException;

public class CropView extends View {

    public CropView(Context context) {
        super(context);
        init();
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setFilePath(String path) {
        if (this.bitmap != null && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
        }

        if (path == null) {
            return;
        }
        try {
            // 1. 读取原始图片（无缩放、无压缩）
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888; // 高精度颜色格式
            options.inScaled = false;                            // 禁用缩放
            this.bitmap = BitmapFactory.decodeFile(path, options);

            // 2. 校正图片方向（Exif 旋转）
            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            int rotationInDegrees = ImageUtil.exifToDegrees(rotation);
            if (rotationInDegrees != 0) {
                matrix.postRotate(rotationInDegrees);
                // 旋转后的 Bitmap（保持原图质量）
                this.bitmap = Bitmap.createBitmap(this.bitmap, 0, 0,
                        this.bitmap.getWidth(), this.bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            // 处理大图内存溢出问题（可选）
            e.printStackTrace();
        }

        setBitmap(this.bitmap);
    }

    private void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        matrix.reset();
        centerImage(getWidth(), getHeight());
        rotation = 0;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerImage(w, h);
        invalidate();
    }

    public Bitmap crop(Rect frame) {
        float scale = getScale();

        float[] src = new float[] {frame.left, frame.top};
        float[] desc = new float[] {0, 0};

        Matrix invertedMatrix = new Matrix();
        this.matrix.invert(invertedMatrix);
        invertedMatrix.mapPoints(desc, src);

        Matrix matrix = new Matrix();

        int width = (int) (frame.width() / scale);
        int height = (int) (frame.height() / scale);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        Bitmap originalBitmap = this.bitmap;
        matrix.postTranslate(-desc[0], -desc[1]);
        canvas.drawBitmap(originalBitmap, matrix, null);
        return bitmap;
    }

    public void setMinimumScale(float setMinimumScale) {
        this.setMinimumScale = setMinimumScale;
    }

    public void setMaximumScale(float maximumScale) {
        this.maximumScale = maximumScale;
    }

    private float setMinimumScale = 0.2f;
    private float maximumScale = 4.0f;

    private float[] matrixArray = new float[9];
    private Matrix matrix = new Matrix();
    private Bitmap bitmap;

    private GestureDetector gestureDetector;

    private ScaleGestureDetector scaleGestureDetector;
    private ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener =
            new ScaleGestureDetector.OnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scale(detector);
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    float scale = detector.getScaleFactor();
                    matrix.postScale(scale, scale);
                    invalidate();
                }
            };

    private void init() {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), onScaleGestureListener);
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                translate(distanceX, distanceY);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    int rotation = 0;

    public void rotate(int degrees) {
        Matrix matrix = new Matrix();

        int dx = this.bitmap.getWidth() / 2;
        int dy = this.bitmap.getHeight() / 2;

        matrix.postTranslate(-dx, -dy);
        matrix.postRotate(degrees);
        matrix.postTranslate(dy, dx);
        Bitmap scaledBitmap = this.bitmap;
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap.getHeight(), scaledBitmap.getWidth(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.drawBitmap(this.bitmap, matrix, null);
        this.bitmap.recycle();
        this.bitmap = rotatedBitmap;
        centerImage(getWidth(), getHeight());
        invalidate();
    }

    private void translate(float distanceX, float distanceY) {
        matrix.getValues(matrixArray);
        float left = matrixArray[Matrix.MTRANS_X];
        float top = matrixArray[Matrix.MTRANS_Y];

        Rect bound = getRestrictedBound();
        if (bound != null) {
            float scale = getScale();
            float right = left + (int) (bitmap.getWidth() / scale);
            float bottom = top + (int) (bitmap.getHeight() / scale);

            if (left - distanceX > bound.left) {
                distanceX = left - bound.left;
            }
            if (top - distanceY > bound.top) {
                distanceY = top - bound.top;
            }

            if (distanceX > 0) {
                if (right - distanceX < bound.right) {
                    distanceX = right - bound.right;
                }
            }
            if (distanceY > 0) {
                if (bottom - distanceY < bound.bottom) {
                    distanceY = bottom - bound.bottom;
                }
            }
        }
        matrix.postTranslate(-distanceX, -distanceY);
        invalidate();
    }

    private void scale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor();
        float currentScale = getScale();
        if (currentScale * scale < setMinimumScale) {
            scale = setMinimumScale / currentScale;
        }
        if (currentScale * scale > maximumScale) {
            scale = maximumScale / currentScale;
        }
        matrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
        invalidate();
    }

    private void centerImage(int width, int height) {
        if (width <= 0 || height <= 0 || bitmap == null) {
            return;
        }
        float widthRatio = 1.0f * height / this.bitmap.getHeight();
        float heightRatio = 1.0f * width / this.bitmap.getWidth();

        float ratio = Math.min(widthRatio, heightRatio);

        float dx = (width - this.bitmap.getWidth()) / 2;
        float dy = (height - this.bitmap.getHeight()) / 2;
        matrix.setTranslate(0, 0);
        matrix.setScale(ratio, ratio, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        matrix.postTranslate(dx, dy);
        invalidate();
    }

    private float getScale() {
        matrix.getValues(matrixArray);
        float scale = matrixArray[Matrix.MSCALE_X];
        if (Math.abs(scale) <= 0.1) {
            scale = matrixArray[Matrix.MSKEW_X];
        }
        return Math.abs(scale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, matrix, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = scaleGestureDetector.onTouchEvent(event);
        result = gestureDetector.onTouchEvent(event) || result;
        return result || super.onTouchEvent(event);
    }

    private Rect restrictBound;

    private Rect getRestrictedBound() {
        return restrictBound;
    }

    public void setRestrictBound(Rect rect) {
        this.restrictBound = rect;
    }
}
