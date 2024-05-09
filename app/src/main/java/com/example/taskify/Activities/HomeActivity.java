package com.example.taskify.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskify.Model.TaskModel;
import com.example.taskify.VolleyNetworking.NetworkManager;
import com.example.taskify.R;
import com.example.taskify.Adapter.TaskListAdapter;
import com.example.taskify.VolleyNetworking.VolleyCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;



public class HomeActivity extends AppCompatActivity {

    // View references
    RecyclerView taskRV;
    ArrayList<TaskModel> dataList;
    TaskListAdapter taskListAdapter;
    Button logoutButton;
    FloatingActionButton addTaskFAB;
    TextView userName;

    // Constants and utilities
    private static final int EDIT_TASK_REQUEST_CODE = 1001;
    private NetworkManager networkManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide(); // Hide the action bar

        // Initialize network manager and shared preferences
        networkManager = NetworkManager.getInstance(this);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);


        // Initialize UI components
        taskRV = findViewById(R.id.taskListRV);
        dataList = new ArrayList<>();
        userName = findViewById(R.id.userName);
        logoutButton = findViewById(R.id.logoutButton);


        // Initialize and set up RecyclerView and adapter
        taskListAdapter = new TaskListAdapter(dataList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        taskRV.setLayoutManager(layoutManager);
        taskRV.setAdapter(taskListAdapter);

        // Set the user's name retrieved from SharedPreferences
        userName.setText(sharedPreferences.getString("username", "User"));

        // Set up add task floating action button
        addTaskFAB = findViewById(R.id.addTaskFAB);
        addTaskFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AddTaskActivity.class));
            }
        });

        // Logout button setup
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTasksFromVolley(); // Fetch tasks from the server when activity resumes
    }

    // Fetch tasks from the server using Volley
    private void fetchTasksFromVolley() {
        networkManager.getTaskList(new VolleyCallback<ArrayList<TaskModel>>() {
            @Override
            public void onSuccess(ArrayList<TaskModel> taskList) {
                // Clear the existing data list
                dataList.clear();
                // Add the tasks retrieved from Volley to the data list
                dataList.addAll(taskList);
                // Sort dataList based on task deadline
                Collections.sort(dataList, new Comparator<TaskModel>() {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    @Override
                    public int compare(TaskModel task1, TaskModel task2) {
                        try {
                            Date date1 = dateFormat.parse(task1.getTaskDeadline());
                            Date date2 = dateFormat.parse(task2.getTaskDeadline());
                            return date1.compareTo(date2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });
                taskListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Log.e("FETCH", "Error getting tasks: " + message);
                // Handle the error scenario
            }
        });
    }

    // Log out the user
    private void logoutUser() {
        networkManager.logoutUser(new VolleyCallback<String>() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); // Clear activity stack

                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, "Logout failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}