package com.example.project2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import androidx.camera.lifecycle.ProcessCameraProvider;



class Quiz {
    String name;
    List<Question> questions;

    public Quiz() {}

    public Quiz(String name, List<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
class Question {
    private String question;
    private List<String> reponses;
    private String repCorrect;

    public Question() {} // Firestore needs empty constructor

    public Question(String question, List<String> reponses, String repCorrect) {
        this.question = question;
        this.reponses = reponses;
        this.repCorrect = repCorrect;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getReponses() {
        return reponses;
    }

    public void setReponses(List<String> reponses) {
        this.reponses = reponses;
    }

    public String getRepCorrect() {
        return repCorrect;
    }

    public void setRepCorrect(String repCorrect) {
        this.repCorrect = repCorrect;
    }
}


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_CAMERA = 1;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    MaterialButton logout, start, enableCamera, switchCamera;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView infos;
    FirebaseFirestore db;
    private boolean isBackCamera = true; // To keep track of which camera is currently active

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        logout = findViewById(R.id.quit);
        start = findViewById(R.id.action_button);
        enableCamera = findViewById(R.id.enableCamera);
        previewView = findViewById(R.id.previewView);
        infos = findViewById(R.id.text);
        switchCamera = findViewById(R.id.switchCamera);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Apply edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        enableCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera(isBackCamera);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            }
        });

        switchCamera.setOnClickListener(v -> {
            isBackCamera = !isBackCamera; // Toggle camera
            startCamera(isBackCamera);
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });

        start.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Questions.class));
            finish();
        });
    }

    private void startCamera(boolean useBackCamera) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll();

                bindPreview(cameraProvider, useBackCamera);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider, boolean useBackCamera) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(useBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            startCamera(isBackCamera);
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        } else {
            infos.setText(user.getEmail());
        }
    }

    private void addQuizToFirestore() {
        String quizName = "Quiz1";
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("1 - 1 = ?", List.of("1", "0", "3"), "0"));
        questions.add(new Question("1 / 0 = ?", List.of("1", "2", "Impossible"), "Impossible"));
        questions.add(new Question("1 + 2 = ?", List.of("1", "2", "3"), "3"));
        questions.add(new Question("1 / 1 = ?", List.of("1", "2", "3"), "1"));
        questions.add(new Question("1 + 1 = ?", List.of("1", "2", "3"), "2"));

        Quiz quiz = new Quiz(quizName, questions);

        db.collection("quiz")
                .whereEqualTo("name", quizName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        db.collection("quiz")
                                .add(quiz)
                                .addOnSuccessListener(documentReference ->
                                        Toast.makeText(MainActivity.this, "Quiz uploaded!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(MainActivity.this, "Quiz already exists or failed to check", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
