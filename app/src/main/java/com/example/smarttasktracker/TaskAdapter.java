package com.example.smarttasktracker;

import android.graphics.Color;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private final OnTaskActionListener listener;
    private boolean isHistoryMode; // True if this adapter is for the bottom "Completed" list

    public interface OnTaskActionListener {
        void onStart(int id);

        void onFinish(int id, long elapsedSeconds);

        void onDelete(int id);
    }

    public TaskAdapter(List<Task> tasks, boolean isHistoryMode, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.isHistoryMode = isHistoryMode;
        this.listener = listener;
    }

    public void updateList(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // 1. Set Name
        holder.tvName.setText(task.getName());

        // 2. Set Details (Cost + Expected Time) - This is the fix!
        holder.tvExpected.setText(String.format("Cost: $%.2f • Exp: %d min", task.getCost(), task.getExpectedMinutes()));

        // 3. Set Delete Button Listener
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(task.getId()));

        if (isHistoryMode) {
            // --- COMPLETED MODE ---
            // Grey out the card visual style
            holder.cardView.setCardBackgroundColor(Color.parseColor("#EEEEEE"));
            holder.tvName.setTextColor(Color.GRAY);
            holder.tvExpected.setTextColor(Color.GRAY);

            // Show final recorded time
            long minutes = task.getActualSeconds() / 60;
            long seconds = task.getActualSeconds() % 60;
            holder.chronometer.setText(String.format("Done in: %02d:%02d", minutes, seconds));
            holder.chronometer.setTextColor(Color.GRAY);

            // Hide Action Button (No actions allowed in history)
            holder.btnAction.setVisibility(View.GONE);

        } else {
            // --- ACTIVE MODE ---
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.tvName.setTextColor(Color.parseColor("#1D1B20"));
            holder.tvExpected.setTextColor(Color.parseColor("#666666"));
            holder.btnAction.setVisibility(View.VISIBLE);

            if (task.getStatus() == 1) { // STATUS: RUNNING
                holder.btnAction.setText("Finish");
                holder.btnAction.setBackgroundColor(Color.parseColor("#B3261E")); // Red color for Stop
                holder.btnAction.setTextColor(Color.WHITE);

                // Calculate the timer offset: Time now - Time started
                long elapsedRealtime = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - task.getStartTimestamp());
                holder.chronometer.setBase(elapsedRealtime);
                holder.chronometer.start();

                holder.btnAction.setOnClickListener(v -> {
                    holder.chronometer.stop();
                    long elapsedMillis = SystemClock.elapsedRealtime() - holder.chronometer.getBase();
                    // Convert to seconds and notify listener
                    listener.onFinish(task.getId(), elapsedMillis / 1000);
                });

            } else { // STATUS: PENDING
                holder.btnAction.setText("Start");
                holder.btnAction.setBackgroundColor(Color.parseColor("#6750A4")); // Purple color for Start
                holder.btnAction.setTextColor(Color.WHITE);

                holder.chronometer.stop();
                holder.chronometer.setText("00:00"); // Reset text

                holder.btnAction.setOnClickListener(v -> listener.onStart(task.getId()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvExpected;
        Chronometer chronometer;
        Button btnAction;
        ImageButton btnDelete;
        CardView cardView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvExpected = itemView.findViewById(R.id.tvExpected);
            chronometer = itemView.findViewById(R.id.chronometer);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardView = (CardView) itemView;
        }
    }
}