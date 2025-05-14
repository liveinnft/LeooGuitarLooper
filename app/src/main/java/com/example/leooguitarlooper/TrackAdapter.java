package com.example.leooguitarlooper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> trackList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onMuteClick(int position, boolean isMuted);
        void onDeleteClick(int position);
        void onRenameClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TrackAdapter(List<Track> trackList) {
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.tvTrackName.setText(track.getTrackName());
        holder.cbMute.setChecked(track.isMuted());

        holder.cbMute.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onMuteClick(position, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });

        holder.btnRename.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRenameClick(position);
            }
        });

        if (track.isPlaying()) {
            holder.ivPlaying.setVisibility(View.VISIBLE);
        } else {
            holder.ivPlaying.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrackName;
        CheckBox cbMute;
        ImageButton btnRename;
        ImageButton btnDelete;
        ImageView ivPlaying;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrackName = itemView.findViewById(R.id.tv_track_name);
            cbMute = itemView.findViewById(R.id.cb_mute);
            btnRename = itemView.findViewById(R.id.btn_rename);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivPlaying = itemView.findViewById(R.id.iv_playing);
        }
    }
}