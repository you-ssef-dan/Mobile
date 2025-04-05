package com.example.project2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Score extends AppCompatActivity {
    TextView tvScore, tvCorrect, tvWrong, tvGreating;
    int score,totalQuestions;

    Button btn_playAgain, btn_logout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);

        tvScore = findViewById(R.id.tv_score);
        tvCorrect = findViewById(R.id.tv_correct);
        tvWrong = findViewById(R.id.tv_wrong);
        tvGreating = findViewById(R.id.tv_greating);
        btn_playAgain = findViewById(R.id.playAgain);
        btn_logout = findViewById(R.id.logout);

        getIntentData();
        displayScore();

        // Restart quiz
        btn_playAgain.setOnClickListener(v -> {
            startActivity(new Intent(Score.this, MainActivity.class));
            finish();
        });

        // Logout user
        btn_logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Score.this, Login.class));
            finish();
        });

    }

    private void getIntentData(){
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 0);
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