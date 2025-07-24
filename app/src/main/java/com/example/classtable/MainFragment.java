package com.example.classtable;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.classtable.bean.TableCell;
import com.example.classtable.custom.MyTableLayout;
import com.example.classtable.custom.TimeRangeSelectorDialog;

import com.example.classtable.databinding.FragmentMainBinding;
import com.example.classtable.sqlite.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {

    private MyTableLayout tableLayout;
    private DatabaseHelper databaseHelper;
    private FragmentMainBinding binding; // 使用 ViewBinding
    private Calendar curDate = Calendar.getInstance();
    private SharedPreferences time;
    Map<Integer, String> timeMap = new HashMap<>();
    Map<String, Integer> todayMap = new HashMap<>();
    private int row = -1;
    private int col = -1;
    private int TEXT_SIZE=16;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy  MM");
        binding.date.setText(sdf.format(this.curDate.getTime()));

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        databaseHelper.openRead();
        databaseHelper.openWrite();
        show_course();

        time = requireContext().getSharedPreferences("time", Context.MODE_PRIVATE);
        tableLayout = binding.table;
        tableLayout.setOnItemClickListener(new MyTableLayout.OnMyClickListener() {
            @Override
            public boolean onItemClick(TableCell cell, int type) {
                // 单击事件
                if (type == 1) {
                    if (cell.getCol() > 0) {
                        Intent intent = new Intent(requireContext(), AddCourseActivity.class);
                        intent.putExtra("row", cell.getRow());
                        intent.putExtra("col", cell.getCol());
                        startActivityForResult(intent, 0);
                    } else {
                        TextView textView = (TextView) binding.table.getChildAt(cell);
                        TimeRangeSelectorDialog dialog = new TimeRangeSelectorDialog(requireContext(), textView.getText().toString(), "00:00", "23:59", new TimeRangeSelectorDialog.ConfirmAction() {
                            @Override
                            public void onLeftClick() {
                            }

                            @Override
                            public void onRightClick(String startTime, String endTime) {
                                textView.setText(String.format("%s\n - \n%s", startTime, endTime));
                                binding.table.addView(textView, cell);
                            }
                        });
                        dialog.show();
                    }
                }
                // 长按事件
                else if (type == 2) {
                    databaseHelper.delete_course(cell.getRow(), cell.getCol());
                    onResume();
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
        if (!databaseHelper.isEmpty()) {
            //通知table突显下一节课程，因为数据库不为空，那肯定要凸显
            binding.table.isDrawNext(true);
            //col是多少，今天就是星期几(并且每次都重置col，因为它不像row，没人给他赋值，还保留上一次的结果)
            col = curDate.get(Calendar.DAY_OF_WEEK) - 1;
            if (col == 0) {
                col = 7;
            }
            //找到今天所有课程，keys是所有课程的行号
            List<Integer> keys = databaseHelper.query_Day(col);
            boolean flag = false;
            //计算今天的下一节课，如果当前时间晚于今天课程的所有开始时间，就返回false
            try {
                flag = cal_next_Course(keys);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            //计算离今天最近的课（今天之后的课）
            if (!flag) {
                List<Integer> nextKeys = null;
                do {
                    col++;
                    if (col == 8) {
                        col = 1;
                    }
                    nextKeys = databaseHelper.query_Day(col);
                } while (nextKeys.size() == 0);
                //collection得到最小nextKeys中最小值，即为那天的第一节课，不能用nextKeys[0],因为课程加入的顺序可能不一样
                binding.table.genCell(Collections.min(nextKeys), col);
            }
        } else {
            binding.table.isDrawNext(false);
        }
    }

    private boolean cal_next_Course(List<Integer> keys) throws ParseException {
        if (keys.size() == 0) {
            // 如果 keys 列表为空，说明没有课程信息，返回 false
            return false;
        }
        todayMap.clear();
        for (int key : keys) {
            //键是今天所有课程的时间，值是每个课程在第几行
            todayMap.put(timeMap.get(key), key);
            Log.e("key", String.valueOf(key));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date currentDate;
        currentDate = sdf.parse(sdf.format(new Date()));
        boolean today_if_have = false;
        Integer minValue = null;
        for (String key : todayMap.keySet()) {
            //得到今天每个课程开始的时间
            String startTime = key.split("\n - ")[0];
            Date startItem;
            //将每个课程开始时间格式化，好与现在的时间比较
            startItem = sdf.parse(startTime);
            //开头时间晚于现在时间，并且找出value最小的，即最靠近现在的时间，minValue == null是让它开始能进入
            if (startItem.after(currentDate) && (minValue == null || todayMap.get(key) < minValue)) {
                minValue = todayMap.get(key);
                today_if_have = true;
            }
        }
        if (minValue != null) {
            row = minValue;
            binding.table.genCell(row, col);
        }
        return today_if_have;
    }

    private void show_course() {
        List<TableCell> courses = databaseHelper.queryall();
        for (TableCell course : courses) {
            TextView textView = new TextView(requireContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText(course.getName());
            textView.setTextSize(TEXT_SIZE);
            binding.table.addView(textView, course);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            TableCell course = (TableCell) data.getSerializableExtra("course");
            createCourseView(course);
            databaseHelper.insert_course(course);
        }
    }

    private void createCourseView(TableCell course) {
        TextView textView = new TextView(requireContext());
        textView.setText(course.getName());
        textView.setTextSize(TEXT_SIZE);
        textView.setGravity(Gravity.CENTER);
        binding.table.addView(textView, course);
    }

    private void reload() {
        timeMap.put(0, time.getString("tm1", "08:20\n - \n10:00"));
        timeMap.put(1, time.getString("tm2", "10:20\n - \n12:00"));
        timeMap.put(2, time.getString("tm3", "14:30\n - \n16:10"));
        timeMap.put(3, time.getString("tm4", "16:30\n - \n18:10"));
        timeMap.put(4, time.getString("tm5", "19:30\n - \n21:10"));

        binding.one.setText(time.getString("tm1", "08:20\n - \n10:00"));
        binding.two.setText(time.getString("tm2", "10:20\n - \n12:00"));
        binding.three.setText(time.getString("tm3", "14:30\n - \n16:10"));
        binding.four.setText(time.getString("tm4", "16:30\n - \n18:10"));
        binding.five.setText(time.getString("tm5", "19:30\n - \n21:10"));
    }

    @Override
    public void onPause() {
        //退出保存课程表的时间
        SharedPreferences.Editor editor = time.edit();
        editor.putString("tm1", binding.one.getText().toString());
        editor.putString("tm2", binding.two.getText().toString());
        editor.putString("tm3", binding.three.getText().toString());
        editor.putString("tm4", binding.four.getText().toString());
        editor.putString("tm5", binding.five.getText().toString());
        editor.apply();
        super.onPause();
    }
}