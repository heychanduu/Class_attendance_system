package com.cms.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTeacher = findViewById(R.id.btnTeacherMode);
        Button btnStudent = findViewById(R.id.btnStudentMode);

        btnTeacher.setOnClickListener(v -> startActivity(new Intent(this, TeacherActivity.class)));
        btnStudent.setOnClickListener(v -> startActivity(new Intent(this, StudentActivity.class)));
    }
}