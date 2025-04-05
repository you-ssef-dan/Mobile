package com.example.project2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Questions extends AppCompatActivity {
    TextView question, rep1, rep2, rep3,tv_time;
    FirebaseFirestore db;
    int totalQuestions = 0, currentQuestionIndex = 0, score,time = 10000;
    Button btnNext, btnQuit;
    private CountDownTimer countDownTimer;

    ProgressBar progressBarTime;


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
        progressBarTime = findViewById(R.id.progressBarTime);
        tv_time = findViewById(R.id.tv_time);
        btnQuit = findViewById(R.id.btn_quit);

        btnNext.setOnClickListener(v -> nextQuestion());
        btnQuit.setOnClickListener(v -> quitQuiz());

        rep1.setOnClickListener(v -> checkAnswer(rep1.getText().toString()));
        rep2.setOnClickListener(v -> checkAnswer(rep2.getText().toString()));
        rep3.setOnClickListener(v -> checkAnswer(rep3.getText().toString()));

        fetchQuizzesFromFirestore();
        startCountdown();
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
                        totalQuestions = quizzes.get(0).questions.size();
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
        startCountdown();

        List<Question> questionList = quizzes.get(0).questions;

        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);

            question.setText(currentQuestion.getQuestion());
            rep1.setText(currentQuestion.getReponses().get(0));
            rep2.setText(currentQuestion.getReponses().get(1));
            rep3.setText(currentQuestion.getReponses().get(2));
        } else {
            Toast.makeText(this, "You've reached the end of the quiz!", Toast.LENGTH_SHORT).show();
            btnNext.setEnabled(false); // Optional: disable button
            Toast.makeText(this, "Your score is: " + score, Toast.LENGTH_SHORT).show();
            navigateToScore();
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++; // Initialize with the first question index
        displayQuizzes(quizzes);
    }

    private void checkAnswer(String selectedAnswer) {
        Question currentQuestion = quizzes.get(0).questions.get(currentQuestionIndex);
        if (selectedAnswer.equals(currentQuestion.getRepCorrect())) {
            score++;
            Toast.makeText(this, "Correct!" , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong!" , Toast.LENGTH_SHORT).show();
        }
        nextQuestion(); // Move to the next question after checking the answer
    }

    private void startCountdown() {
        // Cancel any existing timer if it exists
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Countdown timer for 20 seconds (20,000 milliseconds)
        countDownTimer = new CountDownTimer(time, 1000) { // 20000 ms = 20 seconds, 1000 ms = update interval
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate progress based on time left
                int progress = (int) ((time - millisUntilFinished) * 100 / time);
                progressBarTime.setProgress(progress);

                // Update the time remaining
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                int minutes = secondsRemaining / 60;
                int seconds = secondsRemaining % 60;
                tv_time.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // Call function when countdown is complete
                nextQuestion();  // Move to the next question when the time is up
            }
        }.start();
    }

    private void quitQuiz() {
        navigateToScore();
    }

    private void navigateToScore() {
        Intent intent = new Intent(this, Score.class);
        intent.putExtra("score", score);  // Pass the score
        intent.putExtra("totalQuestions", totalQuestions);  // Pass the total number of questions
        startActivity(intent);
        finish();  // Close MainActivity // Close MainActivity so the user can't go back to it
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();  // Cancel the countdown when activity is destroyed
        }
        super.onDestroy();
    }


}