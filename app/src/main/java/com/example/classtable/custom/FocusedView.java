package com.example.classtable.custom;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class FocusedView extends androidx.appcompat.widget.AppCompatTextView {
    public boolean isToday = false;
    private Paint paint = new Paint();

    public FocusedView(Context context) {
        super(context);
    }

    public FocusedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initControl();
    }

    public FocusedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initControl() {
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setColor(Color.parseColor("#00BCD4"));
        this.paint.setStrokeWidth(4);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isToday) {
            canvas.translate((float)(this.getWidth() / 2), (float)(this.getHeight() / 2));
            canvas.drawCircle(0.0F, 0.0F, (float)(this.getWidth() / 2-2), this.paint);
        }

    }
}
