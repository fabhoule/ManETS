package ca.etsmtl.server.models;

public class Song {

    private String title;
    private String artist;
    private String album;
    private Integer duration;
    private String location;
    private String streamManifest;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStreamManifest() {
        return streamManifest;
    }

    public void setStreamManifest(String streamManifest) {
        this.streamManifest = streamManifest;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", title, artist);
    }
}
