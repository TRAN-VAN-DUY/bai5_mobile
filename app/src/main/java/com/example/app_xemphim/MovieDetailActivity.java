package com.example.app_xemphim;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.app_xemphim.model.Movie;
import com.example.app_xemphim.util.ReminderScheduler;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieDetailActivity extends AppCompatActivity {
    private ImageView ivMovieDetail;
    private TextView tvMovieDetailTitle;
    private TextView tvMovieDetailInfo;
    private TextView tvMovieDetailDescription;
    private Spinner spinnerShowtime;
    private EditText etSeats;
    private Button btnBookTicket;
    private ProgressBar detailProgress;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ivMovieDetail = findViewById(R.id.ivMovieDetail);
        tvMovieDetailTitle = findViewById(R.id.tvMovieDetailTitle);
        tvMovieDetailInfo = findViewById(R.id.tvMovieDetailInfo);
        tvMovieDetailDescription = findViewById(R.id.tvMovieDetailDescription);
        spinnerShowtime = findViewById(R.id.spinnerShowtime);
        etSeats = findViewById(R.id.etSeats);
        btnBookTicket = findViewById(R.id.btnBookTicket);
        detailProgress = findViewById(R.id.detailProgress);

        String movieId = getIntent().getStringExtra("movieId");
        if (TextUtils.isEmpty(movieId)) {
            finish();
            return;
        }

        loadMovie(movieId);
        btnBookTicket.setOnClickListener(v -> bookTicket());
    }

    private void loadMovie(String movieId) {
        setLoading(true);
        db.collection("movies").document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    movie = documentSnapshot.toObject(Movie.class);
                    if (movie == null) {
                        setLoading(false);
                        finish();
                        return;
                    }

                    movie.setId(documentSnapshot.getId());
                    bindMovie(movie);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindMovie(Movie movie) {
        tvMovieDetailTitle.setText(movie.getTitle());
        String info = getString(R.string.movie_detail_info, movie.getGenre(), movie.getDuration(), movie.getTheater(),
                movie.getPrice());
        tvMovieDetailInfo.setText(info);
        tvMovieDetailDescription.setText(movie.getDescription());

        Glide.with(this)
                .load(movie.getPosterUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivMovieDetail);

        List<String> showtimes = movie.getShowtimes();
        if (showtimes == null || showtimes.isEmpty()) {
            showtimes = new ArrayList<>();
            showtimes.add("15/04/2026 19:30");
        }
        ArrayAdapter<String> showtimeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                showtimes);
        showtimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShowtime.setAdapter(showtimeAdapter);

        setLoading(false);
    }

    private void bookTicket() {
        if (movie == null) {
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String seatInput = etSeats.getText().toString().trim();
        if (TextUtils.isEmpty(seatInput)) {
            Toast.makeText(this, R.string.enter_seats, Toast.LENGTH_SHORT).show();
            return;
        }

        int seats;
        try {
            seats = Integer.parseInt(seatInput);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, R.string.invalid_seats, Toast.LENGTH_SHORT).show();
            return;
        }
        if (seats <= 0 || seats > 10) {
            Toast.makeText(this, R.string.invalid_seats, Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedShowtime = spinnerShowtime.getSelectedItem().toString();
        int totalPrice = seats * movie.getPrice();

        setLoading(true);

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("userId", user.getUid());
        ticketData.put("movieId", movie.getId());
        ticketData.put("movieTitle", movie.getTitle());
        ticketData.put("theater", movie.getTheater());
        ticketData.put("showtime", selectedShowtime);
        ticketData.put("seats", seats);
        ticketData.put("totalPrice", totalPrice);
        ticketData.put("bookedAt", Timestamp.now());

        db.collection("tickets").add(ticketData)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    scheduleReminder(documentReference.getId(), movie.getTitle(), movie.getTheater(), selectedShowtime);
                    Toast.makeText(this, getString(R.string.booking_success, totalPrice), Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleReminder(String ticketId, String movieTitle, String theater, String showtime) {
        long showtimeMillis = parseShowtimeToMillis(showtime);
        long triggerMillis = showtimeMillis - (30L * 60L * 1000L);
        long now = System.currentTimeMillis();
        if (triggerMillis <= now) {
            triggerMillis = now + 20_000L;
        }

        ReminderScheduler.scheduleShowReminder(
                this,
                ticketId.hashCode(),
                triggerMillis,
                movieTitle,
                theater,
                showtime);
    }

    private long parseShowtimeToMillis(String showtime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(showtime).getTime();
        } catch (ParseException | NullPointerException e) {
            return System.currentTimeMillis() + 20_000L;
        }
    }

    private void setLoading(boolean isLoading) {
        detailProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnBookTicket.setEnabled(!isLoading);
    }
}
