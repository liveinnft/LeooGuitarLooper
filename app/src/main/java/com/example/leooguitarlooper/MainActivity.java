package com.example.leooguitarlooper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final List<Project> projectList = new ArrayList<>();
    private ProjectAdapter projectAdapter;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProjectPrefs";
    private static final String PROJECTS_KEY = "Projects";
    private TextView tvNoProjects;
    private ImageView ivArrow;

    /** @noinspection deprecation*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            v.setPadding(0, topInset, 0, 0);
            return insets.consumeSystemWindowInsets();
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadProjects();

        tvNoProjects = findViewById(R.id.tv_no_projects);
        ivArrow = findViewById(R.id.iv_arrow);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter(projectList);
        recyclerView.setAdapter(projectAdapter);

        projectAdapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position >= 0 && position < projectList.size()) {
                    Project project = projectList.get(position);
                    Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                    intent.putExtra("project_id", project.getProjectId());
                    intent.putExtra("project_name", project.getProjectName());
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                if (position >= 0 && position < projectList.size()) {
                    projectList.remove(position);
                    projectAdapter.notifyItemRemoved(position);
                    saveProjects();
                    updateNoProjectsView();
                } else {
                    Log.e("MainActivity", "Invalid position for deletion: " + position);
                }
            }

            @Override
            public void onRenameClick(int position) {
                if (position >= 0 && position < projectList.size()) {
                    showRenameProjectDialog(position);
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_project);
        fab.setOnClickListener(view -> showCreateProjectDialog());

        updateNoProjectsView();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadProjects() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PROJECTS_KEY, null);
        Type type = new TypeToken<ArrayList<Project>>() {}.getType();
        List<Project> loadedProjects = gson.fromJson(json, type);

        if (loadedProjects != null) {
            projectList.clear();
            projectList.addAll(loadedProjects);
            if (projectAdapter != null) {
                projectAdapter.notifyDataSetChanged();
            }
        }
    }

    private void saveProjects() {
        Gson gson = new Gson();
        String json = gson.toJson(projectList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PROJECTS_KEY, json);
        editor.apply();
    }

    private void showCreateProjectDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Создайте проект");

        final EditText input = new EditText(this);
        input.setHint("Project Name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String projectName = input.getText().toString();
            if (!projectName.isEmpty()) {
                String projectId = UUID.randomUUID().toString();
                Project newProject = new Project(projectId, projectName);
                projectList.add(newProject);
                if (projectAdapter != null) {
                    projectAdapter.notifyItemInserted(projectList.size() - 1);
                }
                saveProjects();
                updateNoProjectsView();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showRenameProjectDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Переименовать");

        final EditText input = new EditText(this);
        input.setText(projectList.get(position).getProjectName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newProjectName = input.getText().toString();
            if (!newProjectName.isEmpty()) {
                projectList.get(position).setProjectName(newProjectName);
                if (projectAdapter != null) {
                    projectAdapter.notifyItemChanged(position);
                }
                saveProjects();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateNoProjectsView() {
        if (projectList.isEmpty()) {
            tvNoProjects.setVisibility(View.VISIBLE);
            ivArrow.setVisibility(View.VISIBLE);
        } else {
            tvNoProjects.setVisibility(View.GONE);
            ivArrow.setVisibility(View.GONE);
        }
    }
}