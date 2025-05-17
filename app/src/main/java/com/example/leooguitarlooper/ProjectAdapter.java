package com.example.leooguitarlooper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private final List<Project> projectList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onRenameClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProjectName;
        public Button btnDelete;
        public Button btnRename;

        /** @noinspection deprecation*/
        public ProjectViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnRename = itemView.findViewById(R.id.btn_rename);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });

            btnRename.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRenameClick(position);
                    }
                }
            });
        }
    }

    public ProjectAdapter(List<Project> projectList) {
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.tvProjectName.setText(project.getProjectName());
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }
}