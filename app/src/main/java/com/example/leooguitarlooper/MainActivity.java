package com.example.leooguitarlooper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProjectPrefs";
    private static final String PROJECTS_KEY = "Projects";
    private TextView tvNoProjects;
    private ImageView ivArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadProjects();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        projectAdapter = new ProjectAdapter(projectList);
        recyclerView.setAdapter(projectAdapter);

        tvNoProjects = findViewById(R.id.tv_no_projects);
        ivArrow = findViewById(R.id.iv_arrow);
        updateNoProjectsVisibility();

        projectAdapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                intent.putExtra("project_id", projectList.get(position).getProjectId());
                intent.putExtra("project_name", projectList.get(position).getName()); // Передача имени проекта
                startActivity(intent);
            }

            @Override
            public void onRenameClick(int position) {
                showRenameProjectDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                projectList.remove(position);
                projectAdapter.notifyItemRemoved(position);
                saveProjects();
                updateNoProjectsVisibility();
            }
        });

        FloatingActionButton fabAddProject = findViewById(R.id.fab_add_project);
        fabAddProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProjectDialog();
            }
        });
    }

    private void loadProjects() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PROJECTS_KEY, null);
        Type type = new TypeToken<ArrayList<Project>>() {}.getType();
        projectList = gson.fromJson(json, type);

        if (projectList == null) {
            projectList = new ArrayList<>();
        }
    }

    private void saveProjects() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(projectList);
        editor.putString(PROJECTS_KEY, json);
        editor.apply();
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Project");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String projectName = input.getText().toString();
            if (!projectName.isEmpty()) {
                String projectId = UUID.randomUUID().toString();
                projectList.add(new Project(projectName, projectId));
                projectAdapter.notifyItemInserted(projectList.size() - 1);
                saveProjects();
                updateNoProjectsVisibility();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showRenameProjectDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Project");

        final EditText input = new EditText(this);
        input.setText(projectList.get(position).getName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String projectName = input.getText().toString();
            if (!projectName.isEmpty()) {
                projectList.get(position).setName(projectName);
                projectAdapter.notifyItemChanged(position);
                saveProjects();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateNoProjectsVisibility() {
        if (projectList.isEmpty()) {
            tvNoProjects.setVisibility(View.VISIBLE);
            ivArrow.setVisibility(View.VISIBLE);
        } else {
            tvNoProjects.setVisibility(View.GONE);
            ivArrow.setVisibility(View.GONE);
        }
    }
}