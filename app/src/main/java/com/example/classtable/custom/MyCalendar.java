package com.example.classtable.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;import android.widget.ListAdapter;
import android.widget.TextView;


import com.example.classtable.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyCalendar extends LinearLayout {
    private GridView grid;
    private Calendar curDate=Calendar.getInstance();

    public MyCalendar(Context context) {
        super(context);
    }

    public MyCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initControl(context);
    }

    public MyCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initControl(context);
    }

    private void initControl(Context context) {
        this.bindControl(context);
        this.renderCalender();
    }


    private void renderCalender() {
        ArrayList<Date> cells = new ArrayList();
        Calendar calendar = (Calendar)this.curDate.clone();
        int Days = calendar.get(7) - 1;
        if (Days == 0) {
            Days = 7;
        }

        calendar.add(7, -Days + 1);
        int maxCellCount = 7;

        while(cells.size() < maxCellCount) {
            cells.add(calendar.getTime());
            calendar.add(7, 1);
        }

        this.grid.setAdapter((ListAdapter) new CalendarAdapter(this.getContext(), cells));
    }


    private void bindControl(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.my_calender,this);
       /* this.txtDate = (TextView)this.findViewById(R.id.txtDate);*/
        this.grid = (GridView)this.findViewById(R.id.grid);
    }

    private class CalendarAdapter extends ArrayAdapter<Date> {
        LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days) {
            super(context, R.layout.item_day, days);
            this.inflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Date date = (Date)this.getItem(position);
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_day, parent, false);
            }

            int day = date.getDate();
            ((TextView)convertView).setText(String.valueOf(day));
            Date now = new Date();
            boolean isTheSameMouth = false;
            if (now.getMonth() == date.getMonth()) {
                isTheSameMouth = true;
            }

            if (isTheSameMouth) {
                ((TextView)convertView).setTextColor(Color.parseColor("#000000"));
            } else {
                ((TextView)convertView).setTextColor(Color.parseColor("#666666"));
            }

            if (now.getDate() == date.getDate() && now.getMonth() == date.getMonth() && now.getYear() == date.getYear()) {
                ((TextView)convertView).setTextColor(Color.parseColor("#00BCD4"));
                ((FocusedView)convertView).isToday = true;
            }

            return convertView;
        }
    }
}
