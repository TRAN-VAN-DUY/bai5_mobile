package com.example.app_xemphim;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_xemphim.adapter.TicketAdapter;
import com.example.app_xemphim.model.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {
    private RecyclerView recyclerTickets;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<Ticket> ticketList = new ArrayList<>();
    private TicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerTickets = findViewById(R.id.recyclerTickets);
        tvEmpty = findViewById(R.id.tvTicketsEmpty);
        progressBar = findViewById(R.id.ticketsProgress);

        adapter = new TicketAdapter(ticketList);
        recyclerTickets.setLayoutManager(new LinearLayoutManager(this));
        recyclerTickets.setAdapter(adapter);

        loadTickets();
    }

    private void loadTickets() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        setLoading(true);
        db.collection("tickets")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ticketList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Ticket ticket = snapshot.toObject(Ticket.class);
                        if (ticket != null) {
                            ticket.setId(snapshot.getId());
                            ticketList.add(ticket);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(ticketList.isEmpty() ? View.VISIBLE : View.GONE);
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
