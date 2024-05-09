package com.example.taskify.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.taskify.Model.TaskModel;
import com.example.taskify.VolleyNetworking.NetworkManager;
import com.example.taskify.R;
import com.example.taskify.VolleyNetworking.VolleyCallback;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddTaskActivity extends AppCompatActivity {

    // UI components
    Button saveButton, pickColorButton;
    EditText inputTask, inputDeadline;
    CardView taskContainer;

    // Data model
    TaskModel taskModel;
    int defaultColor = Color.parseColor("#FFFF88"); // Default color for the task
    private boolean isEditMode = false; // Flag to check if it's edit mode
    private NetworkManager networkManager; // Network manager for server communication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        // Setup the title based on the mode (Add/Edit)
        getSupportActionBar().setTitle(isEditMode ? "Modifier la Note" : "Créer la Note");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize network and UI components
        networkManager = NetworkManager.getInstance(this);
        initializeViews();
        checkIfEditMode();
    }

    private void initializeViews() {
        // Bind UI components and set listeners
        saveButton = findViewById(R.id.saveButton);
        pickColorButton = findViewById(R.id.pickColorButton);
        inputTask = findViewById(R.id.inputTask);
        inputDeadline = findViewById(R.id.inputDeadline);
        taskContainer = findViewById(R.id.taskContainer);

        saveButton.setOnClickListener(v -> saveTask());
        pickColorButton.setOnClickListener(v -> openColorPicker());
    }

    private void checkIfEditMode() {
        // Check if an existing task is passed to edit
        TaskModel existingTask = (TaskModel) getIntent().getSerializableExtra("selectedTask");
        if (existingTask != null) {
            isEditMode = true;
            taskModel = existingTask;
            inputTask.setText(taskModel.getTaskName());
            inputDeadline.setText(taskModel.getTaskDeadline());
            defaultColor = Color.parseColor(taskModel.getTaskColor());
            pickColorButton.setBackgroundColor(defaultColor);
        } else {
            taskModel = new TaskModel();
        }
    }

    private void saveTask() {
        // Save or update the task
        String taskName = inputTask.getText().toString().trim();
        String taskDeadline = inputDeadline.getText().toString().trim();
        String taskColor = String.format("#%06X", (0xFFFFFF & defaultColor));

        if (!taskName.isEmpty() && !taskDeadline.isEmpty()) {
            if (isEditMode) {
                updateTaskUsingVolley(taskModel.getTaskId(), taskName, taskDeadline, taskColor);
            } else {
                addTaskUsingVolley(taskName, taskDeadline, taskColor);
            }
        } else {
            Toast.makeText(this, "Remplir les champs", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTaskUsingVolley(String taskId, String taskName, String taskDeadline, String taskColor) {
        // Send updated task details to the server
        taskModel = new TaskModel(taskId, taskName, taskDeadline, taskColor, "");
        networkManager.updateTask(taskId, taskModel, new VolleyCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(AddTaskActivity.this, "Bien Enregistré!", Toast.LENGTH_SHORT).show();
                finishActivityWithResult();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddTaskActivity.this, "Échec: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addTaskUsingVolley(String taskName, String taskDeadline, String taskColor) {
        // Send new task details to the server
        taskModel = new TaskModel("", taskName, taskDeadline, taskColor, "");
        networkManager.addTask(taskModel, new VolleyCallback<String>() {
            @Override
            public void onSuccess(String taskId) {
                taskModel.setTaskId(taskId);
                Toast.makeText(AddTaskActivity.this, "Bien Ajouté la note!", Toast.LENGTH_SHORT).show();
                finishActivityWithResult();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddTaskActivity.this, "Échec d'ajouté : " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishActivityWithResult() {
        // Finish activity and return result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("taskModel", taskModel);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void openColorPicker() {
        // Open color picker dialog
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                pickColorButton.setBackgroundColor(defaultColor);

                // Inflate item_task.xml to access the CardView
                View taskLayout = LayoutInflater.from(AddTaskActivity.this).inflate(R.layout.item_task, null);
                CardView taskContainer = taskLayout.findViewById(R.id.taskContainer);
                taskContainer.setCardBackgroundColor(defaultColor);

                // Convert the color integer to a hexadecimal string representation
                String hexColor = String.format("#%06X", (0xFFFFFF & defaultColor));
                taskModel.setTaskColor(hexColor);

                // Pass taskModel to HomeActivity
                Intent intent = new Intent(AddTaskActivity.this, HomeActivity.class);
                intent.putExtra("taskModel", taskModel);

            }
        });
        ambilWarnaDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle the back button in the action bar
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}