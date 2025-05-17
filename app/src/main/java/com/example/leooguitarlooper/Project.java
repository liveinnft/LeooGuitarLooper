package com.example.leooguitarlooper;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private final String projectId;
    private String projectName;
    private List<Track> tracks;
    private int trackCounter; // Добавлено для хранения текущего номера дорожки

    public Project(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.tracks = new ArrayList<>();
        this.trackCounter = 1; // Инициализация счетчика дорожек с 1
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public int getTrackCounter() {
        return trackCounter;
    }

    public void setTrackCounter(int trackCounter) {
        this.trackCounter = trackCounter;
    }

    public void incrementTrackCounter() {
        this.trackCounter++;
    }
}