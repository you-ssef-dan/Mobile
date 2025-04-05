package com.example.project2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Questions extends AppCompatActivity {
    TextView question, rep1, rep2, rep3;
    FirebaseFirestore db;
    int currentQuestionIndex = 0;
    Button btnNext;

    List<Quiz> quizzes = new ArrayList<>();
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_questions);

        rep1 = findViewById(R.id.rep1);
        rep2 = findViewById(R.id.rep2);
        rep3 = findViewById(R.id.rep3);
        question = findViewById(R.id.question);
        db = FirebaseFirestore.getInstance();
        btnNext = findViewById(R.id.btnNext);

        fetchQuizzesFromFirestore();

        btnNext.setOnClickListener(v -> nextQuestion());
    }

    private void fetchQuizzesFromFirestore() {
        db.collection("quiz")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quiz quiz = document.toObject(Quiz.class);
                            quizzes.add(quiz);
                        }
                        // Now you have all quizzes, do something with them
                        Toast.makeText(Questions.this, "Quizzes fetched successfully!", Toast.LENGTH_SHORT).show();
                        displayQuizzes(quizzes);
                    } else {
                        Toast.makeText(Questions.this, "Error getting quizzes: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayQuizzes(List<Quiz> quizzes) {
        if (quizzes.isEmpty()) {
            Toast.makeText(this, "No quizzes found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Question> questionList = quizzes.get(0).questions;

        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);

            question.setText(currentQuestion.question);
            rep1.setText(currentQuestion.reponses.get(0));
            rep2.setText(currentQuestion.reponses.get(1));
            rep3.setText(currentQuestion.reponses.get(2));
        } else {
            Toast.makeText(this, "You've reached the end of the quiz!", Toast.LENGTH_SHORT).show();
            btnNext.setEnabled(false); // Optional: disable button
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++; // Initialize with the first question index
        displayQuizzes(quizzes);
    }

}