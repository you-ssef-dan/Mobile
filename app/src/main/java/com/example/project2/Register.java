package com.example.project2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {
    TextInputEditText email, pass, passConf;
    MaterialButton register;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    MaterialTextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        passConf = findViewById(R.id.passConf);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progress);
        login = findViewById(R.id.log);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String tEmail, tPass;
                tEmail = email.getText().toString();
                tPass = pass.getText().toString();
                if(tEmail.isBlank()){
                    Toast.makeText(Register.this, "Enter Email",Toast.LENGTH_SHORT).show();
                }
                if(tPass.isBlank()){
                    Toast.makeText(Register.this, "Enter Email",Toast.LENGTH_SHORT).show();
                }
                if(!tPass.equals(passConf.getText().toString())){
                    Toast.makeText(Register.this, "Password confirmation is incorrect",Toast.LENGTH_SHORT).show();
                }

                mAuth.createUserWithEmailAndPassword(tEmail, tPass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account created",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Registration failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish(); // to be reconsidered
            }
        });



    }
}