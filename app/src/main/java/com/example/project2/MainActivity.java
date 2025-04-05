package com.example.project2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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
    String question;
    List<String> reponses;
    String repCorrect;

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

    MaterialButton logout, start;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView infos;
    FirebaseFirestore db;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.quit);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        infos = findViewById(R.id.text);
        db = FirebaseFirestore.getInstance();
        start = findViewById(R.id.action_button);


        //fetchQuizzesFromFirestore();


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

    private void addQuizToFirestore() {
        // Create quiz and questions
        String quizName = "Quiz1";
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("1 - 1 = ?", List.of("1", "0", "3"), "0"));
        questions.add(new Question("1 / 0 = ?", List.of("1", "2", "Impossible"), "Impossible"));
        questions.add(new Question("1 + 2 = ?", List.of("1", "2", "3"), "3"));
        questions.add(new Question("1 / 1 = ?", List.of("1", "2", "3"), "1"));
        questions.add(new Question("1 + 1 = ?", List.of("1", "2", "3"), "2"));

        Quiz quiz = new Quiz(quizName, questions);

        // First check if quiz with same name already exists
        db.collection("quiz")
                .whereEqualTo("name", quizName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Quiz does not exist, add it
                                db.collection("quiz")
                                        .add(quiz)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(MainActivity.this, "Quiz uploaded!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Quiz already exists
                                Toast.makeText(MainActivity.this, "Quiz already exists!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to check quiz", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
