package com.example.project2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public List<Question> getQuestions() {
        return questions;
    }
}

class Question {
    private String question;
    private List<String> reponses;
    private String repCorrect;

    public Question() {}

    public Question(String question, List<String> reponses, String repCorrect) {
        this.question = question;
        this.reponses = reponses;
        this.repCorrect = repCorrect;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getReponses() {
        return reponses;
    }

    public String getRepCorrect() {
        return repCorrect;
    }
}

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 1;
    private static final int PERMISSION_LOCATION = 2;
    private static final int PERMISSION_ALL = 100;

    private boolean isBackCamera = true; // To keep track
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    MaterialButton logout, start, enableCamera,switchCamera;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView infos;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.quit);
        start = findViewById(R.id.action_button);
        enableCamera = findViewById(R.id.enableCamera);
        previewView = findViewById(R.id.previewView);
        infos = findViewById(R.id.text);
        switchCamera = findViewById(R.id.switchCamera);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

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
            List<String> permissionsToRequest = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.CAMERA);
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_ALL);
            } else {
                proceedIfPermissionsGranted();
            }
        });
    }

    private void checkAndRequestGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            Toast.makeText(this, "Please enable GPS to continue", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            Toast.makeText(this, "GPS is already enabled", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), Questions.class));
            finish();
        }
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

        if (requestCode == PERMISSION_ALL) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                proceedIfPermissionsGranted();
            } else {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        String permission = permissions[i];
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            showPermissionRationale(permission, PERMISSION_ALL,
                                    permission.equals(Manifest.permission.CAMERA)
                                            ? "Camera access is required to use this feature."
                                            : "Location is needed to start the quiz.");
                        } else {
                            Toast.makeText(this, permission + " permission denied permanently. Enable it from settings.", Toast.LENGTH_LONG).show();
                            openAppSettings();
                        }
                    }
                }
            }
        }

        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                startCamera(isBackCamera);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    showPermissionRationale(Manifest.permission.CAMERA, PERMISSION_CAMERA, "Camera access is required to use this feature.");
                } else {
                    Toast.makeText(this, "Camera permission denied permanently. Enable it from settings.", Toast.LENGTH_LONG).show();
                    openAppSettings();
                }
            }
        }
    }

    private void proceedIfPermissionsGranted() {
        checkAndRequestGPS();
        startCamera(isBackCamera);
    }

    private void showPermissionRationale(String permission, int requestCode, String message) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Grant", (dialog, which) ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
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
