package com.example.taskify.Activities;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskify.Model.TaskModel;
import com.example.taskify.R;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView taskDescriptionTextView;
    private TextView taskDeadlineTextView;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Set the status bar title and enable the home/up button
        getSupportActionBar().setTitle("Task Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        taskDescriptionTextView = findViewById(R.id.detai_taskDescription);
        taskDeadlineTextView = findViewById(R.id.detail_taskDeadline);
        rootLayout = findViewById(R.id.taskDetails);

        // Retrieve the task from the intent
        TaskModel task = (TaskModel) getIntent().getSerializableExtra("taskDetails");

        if (task != null) {
            taskDescriptionTextView.setText(task.getTaskName());
            taskDeadlineTextView.setText(task.getTaskDeadline());
            if (task.getTaskColor() != null && !task.getTaskColor().isEmpty()) {
                rootLayout.setBackgroundColor(Color.parseColor(task.getTaskColor()));
            }
        } else {
            Log.e(TAG, "Task data is null");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the home/up button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}