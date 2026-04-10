package com.example.app_xemphim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_xemphim.R;
import com.example.app_xemphim.model.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private final List<Ticket> tickets;

    public TicketAdapter(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.tvMovie.setText(ticket.getMovieTitle());
        holder.tvTheater.setText(holder.itemView.getContext().getString(R.string.ticket_theater, ticket.getTheater()));
        holder.tvShowtime
                .setText(holder.itemView.getContext().getString(R.string.ticket_showtime, ticket.getShowtime()));
        holder.tvSeats.setText(holder.itemView.getContext().getString(R.string.ticket_seats, ticket.getSeats()));
        holder.tvTotal.setText(holder.itemView.getContext().getString(R.string.ticket_total, ticket.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovie;
        TextView tvTheater;
        TextView tvShowtime;
        TextView tvSeats;
        TextView tvTotal;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovie = itemView.findViewById(R.id.tvMovieTicketTitle);
            tvTheater = itemView.findViewById(R.id.tvMovieTicketTheater);
            tvShowtime = itemView.findViewById(R.id.tvMovieTicketShowtime);
            tvSeats = itemView.findViewById(R.id.tvMovieTicketSeats);
            tvTotal = itemView.findViewById(R.id.tvMovieTicketTotal);
        }
    }
}
