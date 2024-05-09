package com.example.taskify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskify.VolleyNetworking.NetworkManager;
import com.example.taskify.R;
import com.example.taskify.VolleyNetworking.VolleyCallback;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // This line enables the back (up) button
            getSupportActionBar().setTitle("Register new user"); // Set the title for the action bar
        }

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (validateRegistration(username, password)) {
                    registerUser(username, password);
                } else {
                    Toast.makeText(RegisterActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                }
            }

        });
        networkManager = NetworkManager.getInstance(this);
    }

    private boolean validateRegistration(String username, String password) {
        return !username.isEmpty() && !password.isEmpty();
    }

    private void registerUser(String username, String password) {
        networkManager.registerUser(username, password, "emiage2023-2", new VolleyCallback<String>() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                Log.d("REGISTER:", "Register new user successfully");
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                // Registration failed
                Toast.makeText(RegisterActivity.this, "Registration failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // This would navigate back to parent activity from where it is called.
        return true;
    }
}