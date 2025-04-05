package com.example.project2;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.project2.databinding.ActivityMapsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    TextView tvScore, tvCorrect, tvWrong, tvGreating;
    int score,totalQuestions;

    Button btn_playAgain, btn_logout;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps);

        tvScore = findViewById(R.id.tv_score);
        tvCorrect = findViewById(R.id.tv_correct);
        tvWrong = findViewById(R.id.tv_wrong);
        tvGreating = findViewById(R.id.tv_greating);
        btn_playAgain = findViewById(R.id.playAgain);
        btn_logout = findViewById(R.id.logout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        getIntentData();

        getIntentData();
        displayScore();


        // Restart quiz
        btn_playAgain.setOnClickListener(v -> {
            startActivity(new Intent(MapsActivity.this, MainActivity.class));
            finish();
        });

        // Logout user
        btn_logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MapsActivity.this, Login.class));
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng CasaBlanca = new LatLng(33.5736, -7.5898);
        mMap.addMarker(new MarkerOptions().position(CasaBlanca).title("CasaBlanca"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CasaBlanca,10));
    }
}