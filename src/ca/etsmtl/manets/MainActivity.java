package ca.etsmtl.manets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import models.ManETS_Player;

import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    private Server server;
    private String port;
    private String ip;
    private boolean isStreamMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        port = sharedPref.getString("port", "");
        ip = sharedPref.getString("ip", "");
        ip = sharedPref.getString("isStreamMode", "");
        server = new Server();

        try {
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setting(MenuItem item) {
        startActivity(new Intent(this, ManETSPreferenceFragment.class));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    public void play(View view) {

        if(isStreamMode) {

            Task playTask = new Task();
            playTask.execute("play", "0");
        } else {

            playStream();
        }
    }

    public void getPlaylist() {
        Task getTask = new Task();
        getTask.execute("getPlaylist", "0");
    }

    public void playStream() {

        final ManETS_Player manETSPlayer = new ManETS_Player();
        try {
            manETSPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            final String url = String.format("http://%s:%s/01_manifest.m3u8", ip, port);
            manETSPlayer.setDataSource(url);

            manETSPlayer.prepare(); // Opération qui prend beaucoup de temps.

        } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }

        manETSPlayer.start();
    }

}
