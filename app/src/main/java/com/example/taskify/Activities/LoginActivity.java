package com.example.taskify.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskify.VolleyNetworking.NetworkManager;
import com.example.taskify.R;
import com.example.taskify.VolleyNetworking.VolleyCallback;

public class LoginActivity extends AppCompatActivity {

    // UI references
    private EditText usernameLoginText, passwordLoginText;
    private NetworkManager networkManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize shared preferences to save and retrieve user-specific settings.
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize UI components.
        usernameLoginText = findViewById(R.id.usernameLoginText);
        passwordLoginText = findViewById(R.id.passwordLoginText);

        // Setup action bar with back button and title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back (up) button
            getSupportActionBar().setTitle("Login to Taskify"); // Set the title for the action bar
        }

        // Setup login button click listener
        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameLoginText.getText().toString().trim();
                String password = passwordLoginText.getText().toString().trim();

                if (validateLogin(username, password)) {
                    loginUser(username, password);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup sign up button click listener
        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
        // Initialize the network manager for network operations
        networkManager = NetworkManager.getInstance(this);
    }

    // Validate username and password input
    private boolean validateLogin(String username, String password) {
        // Add more sophisticated validation logic here if needed
        return !username.isEmpty() && !password.isEmpty();
    }

    // Attempt to login user with provided credentials
    private void loginUser(String username, String password) {
        // Call the registerUser method from the NetworkManager class
        networkManager.loginUser(username, password, new VolleyCallback<String>() {
            @Override
            public void onSuccess(String message) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.apply();

                // Registration successful
                Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                Log.d("LOGIN", "Login successfully");
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish(); // Close LoginActivity after successful login
            }

            @Override
            public void onError(String message) {
                // Registration failed
                Toast.makeText(LoginActivity.this, "Login failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle navigation up/back action
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // This would navigate back to parent activity from where it is called.
        return true;
    }
}