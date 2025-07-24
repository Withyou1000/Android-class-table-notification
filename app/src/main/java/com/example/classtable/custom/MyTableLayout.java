package com.example.classtable.custom;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;


import com.example.classtable.AddCourseActivity;
import com.example.classtable.R;
import com.example.classtable.bean.TableCell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class MyTableLayout extends ViewGroup {

    /**
     * 列数
     */
    private int mColumnCount = 8;
    /**
     * 行数
     */
    private int mRowCount = 5;
    /**
     * 边框大小
     */
    private int mBorderWidth = 4;
    private int nextWidth = 6;
    /**
     * 边框画笔
     */
    private final Paint mBorderPaint;
    private final Paint mBorderPaintClear;
    /**
     * 颜色
     */
    private int mColor = Color.BLACK;
    /**
     * 默认列宽
     */
    private int mDefaultColumnWidth = 0;
    /**
     * 默认行高
     */
    private int mDefaultRowHeight = 0;
    /**
     * 当前获取焦点的单元格
     */
    private TableCell mFocusedCell = null;
    private TableCell nextCourse = null;
    private boolean Isdraw = true;
    /**
     * 当前焦点单元格背景色
     */
    private int mFocusedCellBackgroundColor = Color.parseColor("#80b0bec5");

    private final Paint mFocusedCellBackgroundPaint;
    private final Paint nextBorderPaint;
    private final Paint tableItemPanint;
    /**
     * 是否处于多选模式
     */
    private boolean mConsumeTouchEvent = true;

    private final Map<String, TableCell> cellData = new ConcurrentHashMap<>();
    private final Map<TableCell, Integer> cells_color = new ConcurrentHashMap<>();
    private final GestureDetectorCompat gestureDetectorCompat;
    private OnMyClickListener mOnItemClickListener;
    private LinearGradient mVerticalGradient;
    private int[] colorIds = new int[]{
            R.color.course_color_orange_red,
            R.color.course_color_bright_green,
            R.color.course_color_blue_purple,
            R.color.course_color_golden_yellow,
            R.color.course_color_sky_blue,
            R.color.course_color_pink,
            R.color.course_color_light_green,
            R.color.course_color_beige,
            R.color.course_color_deep_blue_purple,
            R.color.course_color_steel_blue
    };


    private static final String TAG = "TableLayout";

    public MyTableLayout(Context context) {
        this(context, null);
    }

    public MyTableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyTableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //边框线
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setColor(Color.BLACK);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        //下节课
        nextBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nextBorderPaint.setColor(Color.RED);
        nextBorderPaint.setStyle(Paint.Style.STROKE);
        nextBorderPaint.setStrokeWidth(nextWidth);
        //橡皮擦
        mBorderPaintClear = new Paint();
        mBorderPaintClear.setAlpha(0xFF);
        mBorderPaintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //聚焦
        mFocusedCellBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFocusedCellBackgroundPaint.setColor(mFocusedCellBackgroundColor);
        mFocusedCellBackgroundPaint.setStyle(Paint.Style.FILL);
        //课表背景
        tableItemPanint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableItemPanint.setStyle(Paint.Style.FILL);
        TableGestureListener gestureListener = new TableGestureListener() {

            private TableCell calTableCell(MotionEvent e) {
                int column = (int) (e.getX() / (mDefaultColumnWidth + mBorderWidth));
                int row = (int) (e.getY() / (mDefaultRowHeight + mBorderWidth));
                return new TableCell(row, column);
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                TableCell cell = calTableCell(e);
                if (cell.getCol() > 0) {
                    removeViewAt(cell.getRow(), cell.getCol());
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(cell, 2);
                    }
                }
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //通过单击的位置判断出哪个cell
                TableCell cell = calTableCell(e);
                int row = cell.getRow();
                int column = cell.getCol();
                mFocusedCell = cell;
                invalidate();
//第一列不触发点击事件
                if (mOnItemClickListener != null) {
                    return mOnItemClickListener.onItemClick(mFocusedCell, 1);
                }
                return super.onSingleTapUp(e);
            }
        };
        new TableGestureDetector(context, gestureListener);
        gestureDetectorCompat = new GestureDetectorCompat(context, gestureListener);
        setWillNotDraw(false);
        initAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnMyClickListener {
        boolean onItemClick(TableCell cell, int type);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        private int gravity = Gravity.CENTER;
        private int row = 0;
        private int column = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.TableLayout_Layout);
            row = a.getInteger(R.styleable.TableLayout_Layout_android_layout_row, 0);
            column = a.getInteger(R.styleable.TableLayout_Layout_android_layout_column, 0);
            gravity = a.getInteger(R.styleable.TableLayout_Layout_android_layout_gravity, Gravity.CENTER);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TableLayout, defStyleAttr, defStyleRes);
        mColumnCount = a.getInteger(R.styleable.TableLayout_android_columnCount, mColumnCount);
        mRowCount = a.getInteger(R.styleable.TableLayout_android_rowCount, mRowCount);
        mBorderWidth = (int) a.getDimension(R.styleable.TableLayout_android_strokeWidth, mBorderWidth);
        mColor = a.getColor(R.styleable.TableLayout_android_color, mColor);
        mBorderPaint.setColor(mColor);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED};
        mVerticalGradient = new LinearGradient(
                0, 0,          // 渐变起点：左上角
                0, h,          // 渐变终点：左下角
                colors,
                null,          // 颜色均匀分布
                Shader.TileMode.CLAMP
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //加4，因为红色框右边 上下都会突出
        int allBorderWidth = mColumnCount == 0 ? 0 : (mColumnCount + 1) * mBorderWidth + 4;
        int allBorderHeight = mRowCount == 0 ? 0 : (mRowCount + 1) * mBorderWidth + 4;
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        boolean changedMeasuredSize = false;
        if (measuredWidth < allBorderWidth) {
            measuredWidth = allBorderWidth;
            changedMeasuredSize = true;
        }
        if (measuredHeight < allBorderHeight) {
            measuredHeight = allBorderHeight;
            changedMeasuredSize = true;
        }
        if (changedMeasuredSize) {
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
        if (mColumnCount == 0) {
            mDefaultColumnWidth = 0;
        } else {
            mDefaultColumnWidth = (int) Math.ceil((getMeasuredWidth() - allBorderWidth) / (float) mColumnCount);
        }
        if (mRowCount == 0) {
            mDefaultRowHeight = 0;
        } else {
            mDefaultRowHeight = (int) Math.ceil((getMeasuredHeight() - allBorderHeight) / (float) mRowCount);
        }

        for (int i = 0, N = getChildCount(); i < N; i++) {
            View c = getChildAt(i);
            if (c.getVisibility() == GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) c.getLayoutParams();
            int childMaxWidth = /*lp.columnSpan **/ mDefaultColumnWidth;
            int childMaxHeight = /*lp.rowSpan **/ mDefaultRowHeight;
            int childWidthSpec;
            int childHeightSpec;
            switch (lp.width) {
                case LayoutParams.MATCH_PARENT:
                    childWidthSpec = MeasureSpec.makeMeasureSpec(childMaxWidth, MeasureSpec.EXACTLY);
                    break;
                case LayoutParams.WRAP_CONTENT:
                    childWidthSpec = MeasureSpec.makeMeasureSpec(childMaxWidth, MeasureSpec.AT_MOST);
                    break;
                default:
                    childWidthSpec = MeasureSpec.makeMeasureSpec(Math.min(lp.width, childMaxWidth), MeasureSpec.EXACTLY);
            }
            switch (lp.height) {
                case LayoutParams.MATCH_PARENT:
                    childHeightSpec = MeasureSpec.makeMeasureSpec(childMaxHeight, MeasureSpec.EXACTLY);
                    break;
                case LayoutParams.WRAP_CONTENT:
                    childHeightSpec = MeasureSpec.makeMeasureSpec(childMaxHeight, MeasureSpec.AT_MOST);
                    break;
                default:
                    childHeightSpec = MeasureSpec.makeMeasureSpec(Math.min(lp.height, childMaxHeight), MeasureSpec.EXACTLY);
            }
            c.measure(childWidthSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (TableCell cell : cellData.values()) {
            View c = cell.getView();
            if (c == null || c.getVisibility() == View.GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) c.getLayoutParams();
            int gravity = lp.gravity;
            int x = lp.column * (mBorderWidth + mDefaultColumnWidth) + mBorderWidth;
            int y = lp.row * (mBorderWidth + mDefaultRowHeight) + mBorderWidth;
            int cellWidth = /*lp.columnSpan **/ mDefaultColumnWidth;
            int cellHeight = /*lp.rowSpan **/ mDefaultRowHeight;
            int measuredWidth = c.getMeasuredWidth();
            int measuredHeight = c.getMeasuredHeight();
            c.setLeft(x);
            c.setTop(y);
            c.setRight(x + cellWidth);
            c.setBottom(y + cellHeight);

            final int layoutDirection = getLayoutDirection();
            final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    x = x + cellWidth / 2 - measuredWidth / 2;
                    break;
                case Gravity.RIGHT:
                    x = x + cellWidth - measuredWidth;
            }
            switch (verticalGravity) {
                case Gravity.CENTER_VERTICAL:
                    y = y + cellHeight / 2 - measuredHeight / 2;
                    break;
                case Gravity.BOTTOM:
                    y = y + cellHeight - measuredHeight;
                    break;
            }
            c.layout(x, y, x + measuredWidth, y + measuredHeight);
        }
    }


    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private LayoutParams generateLayoutParams(TableCell cell) {
        if (cell == null) {
            return (LayoutParams) generateDefaultLayoutParams();
        }
        LayoutParams lp = (LayoutParams) generateDefaultLayoutParams();
        lp.row = cell.getRow();
        lp.column = cell.getCol();
        return lp;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int colW = mBorderWidth + mDefaultColumnWidth;
        int rowH = mBorderWidth + mDefaultRowHeight;
        float offset = mBorderWidth / 2f;
        //绘制每个课表背景颜色
        for (TableCell key : cells_color.keySet()) {
            if (key.getCol() > 0) {
                tableItemPanint.setColor(getResources().getColor(colorIds[cells_color.get(key)]));
                int left = key.getCol() * colW + mBorderWidth + 2;
                int top = key.getRow() * rowH + mBorderWidth + 2;
                if (left < width - mBorderWidth && top < height - mBorderWidth) {
                    canvas.drawRect(left, top, left + colW - mBorderWidth, top + rowH - mBorderWidth, tableItemPanint);
                }
            }
        }
        //取消渐变色了，不好look
//        mBorderPaint.setShader(mVerticalGradient);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        // 绘制横线（统一垂直渐变）
        for (int i = 1; i < mRowCount; i++) {
            float y = (mDefaultRowHeight + mBorderWidth) * i + offset;
            canvas.drawLine(2, y, width - 2, y, mBorderPaint);
        }

        // 绘制竖线（同样垂直渐变）
        for (int i = 1; i < mColumnCount; i++) {
            float x = (mDefaultColumnWidth + mBorderWidth) * i + offset;
            canvas.drawLine(x, 2, x, height - 2, mBorderPaint);
        }

        // 绘制外边框（垂直渐变）
        canvas.drawRoundRect(
                offset, offset,
                width - offset, height - offset,
                0f, 0f,
                mBorderPaint
        );
       /* //绘制横线边框
        for (int i = 1; i < mRowCount; i++) {
            float y = (mDefaultRowHeight + mBorderWidth) * i + offset;
            canvas.drawLine(2, y+2, width-2, y+2, mBorderPaint);
        }
        //绘制竖线边框
        for (int i = 1; i < mColumnCount; i++) {
            float x = (mDefaultColumnWidth + mBorderWidth) * i + offset;
            canvas.drawLine(x+2, 2, x+2, height-2, mBorderPaint);
        }
        //绘制边缘边框
        canvas.drawRoundRect(offset+2, offset+2, width - offset-2, height - offset-2, 0f, 0f, mBorderPaint);*/
        //绘制当前焦点的单元格颜色
        if (mFocusedCell != null && mFocusedCell.getRow() >= 0 && mFocusedCell.getCol() >= 0) {
            int left = mFocusedCell.getCol() * colW + mBorderWidth + 2;
            int top = mFocusedCell.getRow() * rowH + mBorderWidth + 2;
            if (left < width - mBorderWidth && top < height - mBorderWidth) {
                canvas.drawRect(left, top, left + colW - mBorderWidth, top + rowH - mBorderWidth, mFocusedCellBackgroundPaint);
            }
        }
        //绘制下节课边框
        if (nextCourse != null && Isdraw) {
            int left = nextCourse.getCol() * colW + 2;
            int top = nextCourse.getRow() * rowH + 2;
            if (left < width - mBorderWidth && top < height - mBorderWidth) {
                canvas.drawRect(left, top, left + colW, top + rowH, nextBorderPaint);
            }
        }
    }

    public void addView(@NonNull View child, @NonNull TableCell cell) {
        LayoutParams lp = generateLayoutParams(cell);
        addView(child, -1, lp);
    }

    @Override
    public void addView(@NonNull View child, int index, @Nullable ViewGroup.LayoutParams params) {
        LayoutParams lp = (LayoutParams) params;
        if (!checkLayoutParams(params)) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        }
        String key = genCellMapKey(lp.row, lp.column);
        TableCell cell = cellData.get(key);
        if (cell != null) {
            View v = cell.getView();
            if (v != null) {
                removeView(v);
                cell.setView(child);
            }
        }
        super.addView(child, index, params);
    }

    public void removeViewAt(@Nullable TableCell cell) {
        if (cell != null) {
            View v = cell.getView();
            if (v != null) {
                removeView(v);
            }
        }
    }

    public void removeViewAt(int row, int column) {
        View v = getChildAt(row, column);
        if (v != null) {
            removeView(v);
        }
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        String key = genCellMapKey(lp.row, lp.column);
        TableCell cell = new TableCell(lp.row, lp.column);
        cell.setView(child);
        cellData.put(key, cell);
        Random random = new Random();
        cells_color.put(cell, random.nextInt(colorIds.length));
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        String key = genCellMapKey(lp.row, lp.column);
        TableCell cell = cellData.get(key);
        if (cell != null) {
            cell.setView(null);
            if (isDefaultCellLayoutParam(cell)) {
                cellData.remove(key);
                cells_color.remove(cell);
            }
        }
    }


    @Nullable
    public View getChildAt(int row, int column) {
        if (row < 0 || column < 0) {
            return null;
        }
        TableCell cell = cellData.get(genCellMapKey(row, column));
        if (cell != null) {
            return cell.getView();
        }
        return null;
    }

    @Nullable
    public View getChildAt(@NonNull TableCell cell) {
        return getChildAt(cell.getRow(), cell.getCol());
    }

    public void setTableCellData(Collection<TableCell> cells) {
        cellData.clear();
        removeAllViews();
        if (cells != null && cells.size() > 0) {
            for (TableCell cell : cells) {
                if (!isDefaultCellLayoutParam(cell)) {
                    cellData.put(genCellMapKey(cell.getRow(), cell.getCol()), cell);
                }
                if (cell.getView() != null) {
                    addView(cell.getView(), cell);
                }
            }
        }
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return mConsumeTouchEvent;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (getLayerType() == LAYER_TYPE_SOFTWARE) {
            invalidate();
        }
    }


    private boolean isDefaultCellLayoutParam(TableCell cell) {
        return cell != null && cell.getView() == null && !cell.isSelected();
    }

    public void setConsumeTouchEvent(boolean consume) {
        this.mConsumeTouchEvent = consume;
    }

    /**
     * 设置列数
     */
    public void setColumnCount(int columnCount) {
        if (this.mColumnCount != columnCount) {
            this.mColumnCount = Math.max(columnCount, 1);

            requestLayout();
        }
    }

    /**
     * 获取列数
     */
    public int getColumnCount() {
        return mColumnCount;
    }

    /**
     * 设置行数
     */
    public void setRowCount(int rowCount) {
        if (this.mRowCount != rowCount) {
            this.mRowCount = Math.max(rowCount, 1);
            requestLayout();
        }
    }

    /**
     * 获取行数
     */
    public int getRowCount() {
        return mRowCount;
    }

    /**
     * 设置边框颜色
     *
     * @param color 颜色
     */
    public void setColor(int color) {
        if (this.mColor != color) {
            this.mColor = color;
            mBorderPaint.setColor(color);
            invalidate();
        }
    }

    /**
     * 设置选中格子后的背景色
     *
     * @param color 颜色值
     */
    public void setFocusedCellBackgroundColor(int color) {
        if (this.mFocusedCellBackgroundColor != color) {
            this.mFocusedCellBackgroundColor = color;
            mFocusedCellBackgroundPaint.setColor(color);
            invalidate();
        }
    }

    /**
     * 设置边框大小
     *
     * @param mBorderWidth 边框大小（px）
     */
    public void setBorderWidth(int mBorderWidth) {
        if (this.mBorderWidth != mBorderWidth) {
            this.mBorderWidth = Math.max(2, mBorderWidth);
            mBorderPaint.setStrokeWidth(mBorderWidth);
            mBorderPaintClear.setStrokeWidth(mBorderWidth);
            requestLayout();
        }
    }

    /**
     * 获取边框大小
     *
     * @return 边框大小（px）
     */
    public int getBorderWidth() {
        return mBorderWidth;
    }

    /**
     * 设置边框虚线格式
     */
    public void setBorderDashPathEffect(float[] intervals) {
        if (intervals != null && intervals.length > 0) {
            mBorderPaint.setPathEffect(new DashPathEffect(intervals, 0));
        } else {
            mBorderPaint.setPathEffect(null);
        }
        invalidate();
    }

    /**
     * 设置单击格子监听
     */
    public void setOnItemClickListener(OnMyClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    public void clearFocusedCell() {
        mFocusedCell = null;
        Set<String> keySet = cellData.keySet();
        for (String key : keySet) {
            TableCell cell = cellData.get(key);
            cell.setSelected(false);
            if (isDefaultCellLayoutParam(cell)) {
                cellData.remove(cell);
            }
        }
        invalidate();
    }


    public List<TableCell> getSelectedCells() {
        List<TableCell> result = new ArrayList<>();
        for (TableCell cell : cellData.values()) {
            if (cell.isSelected()) {
                result.add(cell);
            }
        }
        return result;
    }

    public List<TableCell> getTableCellData() {
        return new ArrayList<>(cellData.values());
    }


    @Nullable
    public TableCell getFocusedCell() {
        return mFocusedCell;
    }


    private String genCellMapKey(int row, int col) {
        return row + "," + col;
    }

    private String genCellMapKey(TableCell cell) {
        return cell.getRow() + "," + cell.getCol();
    }

    public void genCell(int row, int col) {
        nextCourse = new TableCell(row, col);
        invalidate();
    }

    public void isDrawNext(boolean isdraw) {
        Isdraw = isdraw;
    }
}