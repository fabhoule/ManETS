package ca.etsmtl.manets.task;

import android.media.AudioManager;
import android.os.AsyncTask;
import ca.etsmtl.server.models.ManETS_Player;

import java.io.IOException;

public class StreamingTask extends AsyncTask<String, Void, String> {

    private static StreamingTask instance = null;
    private String ip;
    private String port;
    private final ManETS_Player manETSPlayer = new ManETS_Player();

    public static StreamingTask getInstance() {
        if (instance == null) {
            instance = new StreamingTask();
        }
        return instance;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    protected StreamingTask() {}

    @Override
    protected String doInBackground(String... params) {

        if(params == null || params.length < 1 || ip == null || ip.equals("") || port == null || port.equals("")) {
            return "Missing params";
        }

        switch (params[0]) {
            case "play":
                try {
                    manETSPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    final String url = String.format("http://%s:%s/%s", ip, port, params[1]);
                    manETSPlayer.setDataSource(url);

                    manETSPlayer.prepare();

                } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
                    e.printStackTrace();
                }

                manETSPlayer.start();
                return "OK";
            case "pause":
                if (manETSPlayer.isPlaying()) {
                    manETSPlayer.pause();
                    return "OK";
                } else {
                    manETSPlayer.start();
                    return "OK";
                }
            default:
                return "Invalid action";
        }
    }
}
