package com.example.app_xemphim;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_xemphim.adapter.MovieAdapter;
import com.example.app_xemphim.model.Movie;
import com.example.app_xemphim.util.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private RecyclerView recyclerMovies;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private final List<Movie> movies = new ArrayList<>();
    private MovieAdapter adapter;

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerMovies = findViewById(R.id.recyclerMovies);
        progressBar = findViewById(R.id.moviesProgress);
        tvEmpty = findViewById(R.id.tvMoviesEmpty);
        Button btnMyTickets = findViewById(R.id.btnMyTickets);
        Button btnLogout = findViewById(R.id.btnLogout);

        adapter = new MovieAdapter(movies, movie -> {
            Intent intent = new Intent(MovieListActivity.this, MovieDetailActivity.class);
            intent.putExtra("movieId", movie.getId());
            startActivity(intent);
        });

        recyclerMovies.setLayoutManager(new LinearLayoutManager(this));
        recyclerMovies.setAdapter(adapter);

        btnMyTickets.setOnClickListener(v -> startActivity(new Intent(this, MyTicketsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        requestNotificationPermissionIfNeeded();
        NotificationHelper.createChannel(this);
        saveUserProfileAndToken();
        loadMovies();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void saveUserProfileAndToken() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("fcmToken", token);
            userData.put("updatedAt", FieldValue.serverTimestamp());

            db.collection("users").document(user.getUid()).set(userData,
                    com.google.firebase.firestore.SetOptions.merge());
        });
    }

    private void loadMovies() {
        setLoading(true);
        db.collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    movies.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Movie movie = doc.toObject(Movie.class);
                        if (movie != null) {
                            movie.setId(doc.getId());
                            movies.add(movie);
                        }
                    }

                    if (movies.isEmpty()) {
                        seedSampleMovies();
                    } else {
                        updateMovieUi();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void seedSampleMovies() {
        List<Map<String, Object>> sampleMovies = new ArrayList<>();

        sampleMovies.add(createMovieMap(
                "Avengers: Endgame",
                "Action, Sci-Fi",
                "181 min",
                "https://images.unsplash.com/photo-1536440136628-849c177e76a1",
                "Sieu anh hung tap hop de cuu vu tru.",
                "CGV Vincom",
                90000,
                Arrays.asList("12/04/2026 14:00", "12/04/2026 19:30", "13/04/2026 21:00")));

        sampleMovies.add(createMovieMap(
                "Interstellar",
                "Adventure, Drama",
                "169 min",
                "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c",
                "Hanh trinh xuyen khong gian tim hanh tinh moi.",
                "Lotte Cinema",
                85000,
                Arrays.asList("12/04/2026 16:00", "13/04/2026 18:30", "13/04/2026 20:45")));

        sampleMovies.add(createMovieMap(
                "Dune: Part Two",
                "Sci-Fi, Epic",
                "166 min",
                "https://images.unsplash.com/photo-1478720568477-152d9b164e26",
                "Cuoc chien quyen luc tren hanh tinh cat.",
                "Galaxy Nguyen Du",
                95000,
                Arrays.asList("12/04/2026 15:30", "12/04/2026 20:00", "14/04/2026 21:15")));

        WriteBatch batch = db.batch();
        for (Map<String, Object> movie : sampleMovies) {
            batch.set(db.collection("movies").document(), movie);
        }

        batch.commit()
                .addOnSuccessListener(unused -> loadMovies())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @NonNull
    private Map<String, Object> createMovieMap(String title, String genre, String duration,
            String posterUrl, String description,
            String theater, int price, List<String> showtimes) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("genre", genre);
        map.put("duration", duration);
        map.put("posterUrl", posterUrl);
        map.put("description", description);
        map.put("theater", theater);
        map.put("price", price);
        map.put("showtimes", showtimes);
        return map;
    }

    private void updateMovieUi() {
        setLoading(false);
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
