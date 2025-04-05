package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Score extends AppCompatActivity {
    TextView tvScore, tvCorrect, tvWrong, tvGreating;
    int score,totalQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);

        tvScore = findViewById(R.id.tv_score);
        tvCorrect = findViewById(R.id.tv_correct);
        tvWrong = findViewById(R.id.tv_wrong);
        tvGreating = findViewById(R.id.tv_greating);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 0);

        displayScore();

    }
    private void displayScore() {
        int percentage = (score * 100) / totalQuestions;
        tvScore.setText(String.valueOf(percentage + "%"));
        tvCorrect.setText(String.valueOf(score));
        tvWrong.setText(String.valueOf(totalQuestions - score));

        if (percentage >= 80) {
            tvGreating.setText("Great work!");
        } else if (percentage >= 60) {
            tvGreating.setText("Good job!");
        } else {
            tvGreating.setText("Keep practicing!");
        }
    }
}