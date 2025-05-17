package com.example.leooguitarlooper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RecordActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder mediaRecorder;
    private final List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private String fileName;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;
    private SeekBar seekBar;
    private final Handler mainHandler = new Handler();
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Runnable updateProgressRunnable;
    private int progress = 0;
    private TrackAdapter trackAdapter;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProjectPrefs";
    private static final String PROJECTS_KEY = "Projects";
    private String projectId;
    private TextView tvCountdown;
    private ImageButton btnStartRecording;
    private CountDownTimer countDownTimer;
    private Project currentProject; // Добавлено для хранения текущего проекта
    private boolean isRecording = false; // Флаг для отслеживания состояния записи
    // Флаг для отслеживания состояния проигрывания

    /** @noinspection deprecation*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            v.setPadding(0, topInset, 0, 0);
            return insets.consumeSystemWindowInsets();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Назад к проекту"); // Установка заголовка Toolbar

        projectId = getIntent().getStringExtra("project_id");
        String projectName = getIntent().getStringExtra("project_name");

        TextView tvProjectName = findViewById(R.id.tv_project_name);
        tvProjectName.setText(projectName);

        tvCountdown = findViewById(R.id.tv_countdown);
        tvCountdown.setText("Начните запись");

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadProject();

        progressBar = findViewById(R.id.progress_bar);
        seekBar = findViewById(R.id.seek_bar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackAdapter = new TrackAdapter(trackList);
        recyclerView.setAdapter(trackAdapter);

        trackAdapter.setOnItemClickListener(new TrackAdapter.OnItemClickListener() {
            @Override
            public void onMuteClick(int position, boolean isMuted) {
                if (position >= 0 && position < trackList.size()) {
                    trackList.get(position).setMuted(isMuted);
                    saveProject();
                }
            }

            @Override
            public void onDeleteClick(int position) {
                if (position >= 0 && position < trackList.size()) {
                    trackList.remove(position);
                    trackAdapter.notifyItemRemoved(position);
                    saveProject();
                }
            }

            @Override
            public void onRenameClick(int position) {
                if (position >= 0 && position < trackList.size()) {
                    showRenameTrackDialog(position);
                }
            }
        });

        btnStartRecording = findViewById(R.id.btn_start_recording);
        btnStartRecording.setOnClickListener(v -> {
            if (permissionToRecordAccepted) {
                startCountdown();
            } else {
                ActivityCompat.requestPermissions(RecordActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        });

        ImageButton btnStopRecording = findViewById(R.id.btn_stop_recording);
        btnStopRecording.setOnClickListener(v -> stopRecording());

        ImageButton btnPlayAll = findViewById(R.id.btn_play_all);
        btnPlayAll.setOnClickListener(v -> playAllTracks());

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    for (MediaPlayer mediaPlayer : mediaPlayers) {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int duration = mediaPlayer.getDuration();
                            int newPosition = (duration * progress) / 100;
                            mediaPlayer.seekTo(newPosition);
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        backgroundThread = new HandlerThread("BackgroundThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void startCountdown() {
        btnStartRecording.setEnabled(false);
        countDownTimer = new CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("Начало через: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                tvCountdown.setText("Запись идет...");
                playAllTracksAndStartRecording();
            }
        }.start();
    }

    private void playAllTracksAndStartRecording() {
        stopAllTracks(); // Остановить все предыдущие проигрывания

        // Определение длины самой длинной дорожки
        final int longestTrackDuration = getLongestTrackDuration();

        // Запуск всех дорожек для проигрывания по порядку
        playNextTrack(0, true, longestTrackDuration);
    }

    /** @noinspection CallToPrintStackTrace*/
    private int getLongestTrackDuration() {
        int longestTrackDuration = 0;
        for (Track track : trackList) {
            if (!track.isMuted()) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(track.getFilePath());
                    mediaPlayer.prepare();
                    int duration = mediaPlayer.getDuration();
                    if (duration > longestTrackDuration) {
                        longestTrackDuration = duration;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("RecordActivity", "Error preparing track: " + track.getTrackName(), e);
                } finally {
                    mediaPlayer.release();
                }
            }
        }
        return longestTrackDuration;
    }

    /** @noinspection CallToPrintStackTrace*/
    private void playNextTrack(int index, boolean startRecordingAfter, int longestTrackDuration) {
        if (index >= trackList.size()) {
            if (startRecordingAfter) {
                startRecording();
            }
            return;
        }

        final Track track = trackList.get(index);
        if (!track.isMuted()) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(track.getFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayers.add(mediaPlayer);
                track.setPlaying(true);
                mainHandler.post(() -> trackAdapter.notifyItemChanged(index));
                Log.d("RecordActivity", "Playing track: " + track.getTrackName());

                // Обновление прогресса проигрывания
                mediaPlayer.setOnCompletionListener(mp -> {
                    track.setPlaying(false);
                    mainHandler.post(() -> trackAdapter.notifyItemChanged(index));
                    playNextTrack(index + 1, startRecordingAfter, longestTrackDuration); // Переход к следующей дорожке
                });

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer.isPlaying()) {
                            int progress = (int) ((mediaPlayer.getCurrentPosition() / (float) longestTrackDuration) * 100);
                            mainHandler.post(() -> seekBar.setProgress(progress));
                            mainHandler.postDelayed(this, 100);
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("RecordActivity", "Error playing track: " + track.getTrackName(), e);
            }
        } else {
            playNextTrack(index + 1, startRecordingAfter, longestTrackDuration); // Переход к следующей дорожке, если текущая замучена
        }
    }

    /** @noinspection CallToPrintStackTrace*/
    private void startRecording() {
        // Начало записи новой дорожки
        backgroundHandler.post(() -> {
            String trackId = UUID.randomUUID().toString();
            fileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath() + "/audiorecordtest_" + trackId + ".m4a";
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(192000);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true; // Устанавливаем флаг записи
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    progress = 0;
                    updateProgressRunnable = new Runnable() {
                        @Override
                        public void run() {
                            progress += 1;
                            if (progress > 100) {
                                progress = 0;
                            }
                            progressBar.setProgress(progress);
                            mainHandler.postDelayed(this, 100);
                        }
                    };
                    mainHandler.post(updateProgressRunnable);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private void stopRecording() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            tvCountdown.setText("Начните запись");
            btnStartRecording.setEnabled(true);
        }

        if (isRecording) {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                UUID.randomUUID().toString();
                String trackName = "Track " + currentProject.getTrackCounter(); // Установка имени дорожки
                currentProject.incrementTrackCounter(); // Увеличение счетчика дорожек
                trackList.add(new Track(fileName, trackName));
                mainHandler.post(() -> {
                    trackAdapter.notifyItemInserted(trackList.size() - 1);
                    progressBar.setVisibility(View.INVISIBLE);
                    mainHandler.removeCallbacks(updateProgressRunnable);
                });

                // Log the file path and trackList size
                Log.d("RecordActivity", "Recorded file: " + fileName);
                Log.d("RecordActivity", "Track list size: " + trackList.size());

                saveProject();
                tvCountdown.setText("Начните запись");
                btnStartRecording.setEnabled(true);
            }
            isRecording = false; // Сбрасываем флаг записи
        }

        stopAllTracks(); // Остановить все проигрывания
    }

    private void playAllTracks() {
        stopAllTracks(); // Остановить все предыдущие проигрывания

        // Определение длины самой длинной дорожки
        final int longestTrackDuration = getLongestTrackDuration();

        // Запуск всех дорожек для проигрывания по порядку
        playNextTrack(0, false, longestTrackDuration);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void stopAllTracks() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }
        }
        mediaPlayers.clear();
        for (Track track : trackList) {
            track.setPlaying(false);
        }
        mainHandler.post(() -> trackAdapter.notifyDataSetChanged());
        Log.d("RecordActivity", "All tracks stopped and released.");
    }

    private void loadProject() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PROJECTS_KEY, null);
        Type type = new TypeToken<ArrayList<Project>>() {}.getType();
        List<Project> projectList = gson.fromJson(json, type);

        if (projectList == null) {
            Log.d("RecordActivity", "No projects found in SharedPreferences.");
            return;
        }

        for (Project project : projectList) {
            if (project.getProjectId().equals(projectId)) {
                currentProject = project;
                trackList = project.getTracks();
                Log.d("RecordActivity", "Project loaded with " + trackList.size() + " tracks.");
                return;
            }
        }
        Log.d("RecordActivity", "Project with projectId " + projectId + " not found.");
    }

    private void saveProject() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PROJECTS_KEY, null);
        Type type = new TypeToken<ArrayList<Project>>() {}.getType();
        List<Project> projectList = gson.fromJson(json, type);

        if (projectList == null) {
            Log.d("RecordActivity", "No projects found in SharedPreferences to save.");
            return;
        }

        for (Project project : projectList) {
            if (project.getProjectId().equals(projectId)) {
                project.setTracks(trackList);
                project.setTrackCounter(currentProject.getTrackCounter()); // Сохранение текущего номера дорожки
                Log.d("RecordActivity", "Project saved with " + trackList.size() + " tracks.");
                break;
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        json = gson.toJson(projectList);
        editor.putString(PROJECTS_KEY, json);
        editor.apply();
    }

    private void showRenameTrackDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Track");

        final EditText input = new EditText(this);
        input.setText(trackList.get(position).getTrackName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTrackName = input.getText().toString();
            if (!newTrackName.isEmpty()) {
                trackList.get(position).setTrackName(newTrackName);
                trackAdapter.notifyItemChanged(position);
                saveProject();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAllTracks();
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        backgroundThread.quitSafely();
    }

    /** @noinspection deprecation*/
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}