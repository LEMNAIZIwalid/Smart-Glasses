package com.example.lado.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lado.Models.NotificationModel;
import com.example.lado.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationModel> notifications = new ArrayList<>();

    public void setNotifications(List<NotificationModel> newNotifications) {
        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(new NotificationDiffCallback(this.notifications, newNotifications));

        this.notifications.clear();
        this.notifications.addAll(newNotifications);

        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notif = notifications.get(position);

        // MESSAGE
        holder.textMessage.setText(notif.getMessage());

        // TIMESTAMP FORMATÉ
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date(notif.getTimestamp()));
        holder.textTimestamp.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // ---------------------- ViewHolder ----------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }

    // ---------------------- DiffUtil Callback ----------------------
    private static class NotificationDiffCallback extends DiffUtil.Callback {

        private final List<NotificationModel> oldList;
        private final List<NotificationModel> newList;

        public NotificationDiffCallback(List<NotificationModel> oldList, List<NotificationModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            NotificationModel oldItem = oldList.get(oldItemPosition);
            NotificationModel newItem = newList.get(newItemPosition);

            // Identifiant unique = message + timestamp
            return oldItem.getMessage().equals(newItem.getMessage())
                    && oldItem.getTimestamp() == newItem.getTimestamp();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            NotificationModel oldItem = oldList.get(oldItemPosition);
            NotificationModel newItem = newList.get(newItemPosition);

            return oldItem.getMessage().equals(newItem.getMessage())
                    && oldItem.getTimestamp() == newItem.getTimestamp();
        }
    }
}
