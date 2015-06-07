package ca.etsmtl.manets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.etsmtl.models.ManETS_Player;
import ca.etsmtl.models.Playlist;
import ca.etsmtl.models.Song;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {

    private Server server;
    private String port;
    private String ip;
    private boolean isStreamMode;
    private final ManETS_Player manETSPlayer = new ManETS_Player();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        port = sharedPref.getString("port", "");
        ip = sharedPref.getString("ip", "");
//        isStreamMode = sharedPref.getBoolean("isStreamMode", false);
        server = new Server();

        try {
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        final Gson gson = new GsonBuilder().create();
        final Task task = new Task();
        final ListView songList = (ListView)findViewById(R.id.songList);
        Playlist playlist;

        try {
            playlist = gson.fromJson(task.execute("getPlaylist", "0").get(), Playlist.class);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        final ArrayAdapter<Song> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlist.getSongs());
        songList.setAdapter(adapter);




        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
        }


        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                sendPlay(position);
            }
        });
        
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
    
    public void pause(View view) {

        if(isStreamMode) {

            Task playTask = new Task();
            playTask.execute("pause");
        } else {

            manETSPlayer.pause();
        }
    }

    public void getPlaylist() {
        Task getTask = new Task();
        getTask.execute("getPlaylist", "0");
    }

    public void playStream() {

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

    private void sendPlay(final int index) {

        if(isStreamMode) {
            Task playTask = new Task();
            playTask.execute("play", String.valueOf(index));
        } else {

            playStream();
        }
    }

}
