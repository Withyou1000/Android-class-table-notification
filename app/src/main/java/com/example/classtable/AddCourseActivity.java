package com.example.classtable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classtable.bean.TableCell;
import com.example.classtable.sqlite.DatabaseHelper;


public class AddCourseActivity extends AppCompatActivity {
    private int row;
    private int col;
    private DatabaseHelper databaseHelper;
    private EditText inputCourseName;
    private EditText inputTeacher;
    private EditText inputLocation;
    private EditText inputWeek;
    private EditText inputNote;
    private Button backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_course);
        setFinishOnTouchOutside(false);
        Intent intent = getIntent();
        if (intent != null) {
            row = intent.getIntExtra("row", -1);
            col = intent.getIntExtra("col", -1);
        }
        inputCourseName = (EditText) findViewById(R.id.course_name);
        inputTeacher = (EditText) findViewById(R.id.teacher);
        inputLocation = (EditText) findViewById(R.id.location);
        inputWeek = (EditText) findViewById(R.id.week);
        inputNote = (EditText) findViewById(R.id.note);
        backButton = (Button) findViewById(R.id.back_add);
        databaseHelper = DatabaseHelper.getInstance(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button okButton = (Button) findViewById(R.id.button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseName = inputCourseName.getText().toString();
                String teacher = inputTeacher.getText().toString();
                String location = inputLocation.getText().toString();
                String week = inputWeek.getText().toString();
                String note = inputNote.getText().toString();
                if (courseName.equals("")) {
                    Toast.makeText(AddCourseActivity.this, "起码填个课程名哦，小鬼", Toast.LENGTH_SHORT).show();
                } else {
                    TableCell course = new TableCell(row, col, courseName, week, teacher,
                            location, note);
                    Intent intent = new Intent();
                    intent.putExtra("course",course);
                    intent.putExtra("string",course.getName());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        show_detail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        show_detail();
    }



    private void show_detail() {
        TableCell course = databaseHelper.queryOne(row, col);
        if (course != null) {
            inputCourseName.setText(course.getName());
            inputWeek.setText(course.getTime());
            inputLocation.setText(course.getLocation());
            inputTeacher.setText(course.getTeacher());
            inputNote.setText(course.getNote());
        }
    }
}
