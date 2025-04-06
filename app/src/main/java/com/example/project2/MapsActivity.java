package com.example.project2;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project2.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    TextView tvScore, tvCorrect, tvWrong, tvGreating;
    int score, totalQuestions;
    Button btn_playAgain, btn_logout;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int GPS_REQUEST_CODE = 1001;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        getIntentData();
        displayScore();

        btn_playAgain.setOnClickListener(v -> {
            startActivity(new Intent(MapsActivity.this, MainActivity.class));
            finish();
        });

        btn_logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MapsActivity.this, Login.class));
            finish();
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 0);
    }

    private void displayScore() {
        int percentage = (score * 100) / totalQuestions;
        tvScore.setText(percentage + "%");
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            checkIfLocationEnabled();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkIfLocationEnabled() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            getCurrentLocation();
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, GPS_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            } else {
                Toast.makeText(this, "GPS must be enabled to continue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(current).title("You are here"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

                        Geocoder geocoder = new Geocoder(this);
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String city = addresses.get(0).getLocality();
                                Toast.makeText(this, "City: " + city, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    checkIfLocationEnabled();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "GPS is required to get your location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
