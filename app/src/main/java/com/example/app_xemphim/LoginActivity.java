package com.example.app_xemphim;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvSwitchMode;
    private Button btnAuthAction;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        progressBar = findViewById(R.id.authProgress);

        btnAuthAction.setOnClickListener(v -> handleAuth());
        tvSwitchMode.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            btnAuthAction.setText(R.string.login);
            tvSwitchMode.setText(R.string.switch_to_register);
        } else {
            btnAuthAction.setText(R.string.register);
            tvSwitchMode.setText(R.string.switch_to_login);
        }
    }

    private void handleAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.enter_email_password, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (isLoginMode) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> onAuthSuccess(authResult.getUser()))
                    .addOnFailureListener(e -> onAuthFailed(e.getMessage()));
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> onAuthSuccess(authResult.getUser()))
                    .addOnFailureListener(e -> onAuthFailed(e.getMessage()));
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        if (user == null) {
            onAuthFailed(getString(R.string.auth_failed));
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.auth_success, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MovieListActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onAuthFailed(String message) {
        setLoading(false);
        Toast.makeText(this, message != null ? message : getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnAuthAction.setEnabled(!isLoading);
        tvSwitchMode.setEnabled(!isLoading);
    }
}
