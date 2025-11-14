package com.example.haboob.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.Notification;
import com.example.haboob.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final ArrayList<Notification> data = new ArrayList<>();
    private final OnNotificationClickListener listener;
    private final EventsList eventsList;

    public interface OnNotificationClickListener {
        void onNotificationClick(com.example.haboob.Notification notification);
    }

    public NotificationsAdapter(EventsList eventsList, OnNotificationClickListener listener) {
        this.eventsList = eventsList;
        this.listener = listener;
    }

    public void setData(List<Notification> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Notification n = data.get(position);
        holder.tvMessage.setText(n.getMessage() == null ? "Empty Notification" : n.getMessage());

        Date time = n.getTimeCreated();
        String formatted = time == null ? "" :
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(time);
        holder.tvTime.setText(formatted);

        // event title via in-memory list
        String title = "—";
        if (eventsList != null && n.getEventId() != null) {
            Event e = eventsList.getEventByID(n.getEventId());
            if (e != null && e.getEventTitle() != null && !e.getEventTitle().trim().isEmpty()) {
                title = e.getEventTitle();
            }
        }
        holder.tvEvent.setText("Event: " + title);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvEvent;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvEvent = itemView.findViewById(R.id.tv_event);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
