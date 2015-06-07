package ca.etsmtl.models;

import android.media.MediaPlayer;

import java.util.List;

public class ManETS_Player extends MediaPlayer{

    private List<Playlist> playlists;
    private Integer currentPlaylistIdx;
    private Integer currentSongIdx;
    private boolean repeatOne = false;
    private boolean random = false;

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public Integer getCurrentPlaylistIdx() {
        return currentPlaylistIdx;
    }

    public void setCurrentPlaylistIdx(Integer currentPlaylist) {
        this.currentPlaylistIdx = currentPlaylist;
    }

    public Integer getCurrentSongIdx() {
        return currentSongIdx;
    }

    public void setCurrentSongIdx(Integer currentSongIdx) {
        this.currentSongIdx = currentSongIdx;
    }

    public boolean isRepeatOne() {
        return repeatOne;
    }

    public void setRepeatOne(boolean repeatOne) {
        this.repeatOne = repeatOne;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }
}
