package com.example.leooguitarlooper;

public class Track {
    private final String filePath;
    private final String trackId;
    private String trackName;
    private boolean isMuted;
    private boolean isPlaying;

    public Track(String filePath, String trackId, String trackName) {
        this.filePath = filePath;
        this.trackId = trackId;
        this.trackName = trackName;
        this.isMuted = false;
        this.isPlaying = false;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}