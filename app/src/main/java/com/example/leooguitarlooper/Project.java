package com.example.leooguitarlooper;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private String projectId;
    private List<Track> tracks;

    public Project(String name, String projectId) {
        this.name = name;
        this.projectId = projectId;
        this.tracks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
}