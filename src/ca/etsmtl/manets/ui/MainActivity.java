package ca.etsmtl.manets.ui;

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
import ca.etsmtl.manets.R;
import ca.etsmtl.manets.task.HttpTask;
import ca.etsmtl.manets.task.StreamingTask;
import ca.etsmtl.server.Server;
import ca.etsmtl.server.models.ManETS_Player;
import ca.etsmtl.server.models.Song;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {

    private Server server;
    private String port;
    private String ip;
    private boolean isStreamMode = true;
    private List<Song> songs;
    private final ManETS_Player manETSPlayer = new ManETS_Player();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

//        port = sharedPref.getString("port", "");
//        ip = sharedPref.getString("ip", "");
        //TODO Dynamiser
        port = "8080";
        ip = "127.0.0.1";
//        isStreamMode = sharedPref.getBoolean("isStreamMode", false);
        server = new Server();

        try {
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        final Gson gson = new GsonBuilder().create();
        final HttpTask httpTask = new HttpTask(ip, port);
        final ListView songList = (ListView)findViewById(R.id.songList);
        StreamingTask.getInstance().setIp(ip);
        StreamingTask.getInstance().setPort(port);

        try {
            songs = gson.fromJson(httpTask.execute("getSongs").get(), new TypeToken<List<Song>>(){}.getType());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        final ArrayAdapter<Song> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songs);
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

            HttpTask playHttpTask = new HttpTask(ip, port);
            playHttpTask.execute("pause");
        } else {

            if (manETSPlayer.isPlaying()) {
                manETSPlayer.pause();
            } else {
                manETSPlayer.start();
            }
        }
    }

    public void getPlaylist() {
        HttpTask getHttpTask = new HttpTask(ip, port);
        getHttpTask.execute("getPlaylist", "0");
    }

    public void playStream(final int index) {

        try {
            manETSPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            final String url = String.format("http://%s:%s/%s", ip, port, URLEncoder.encode(songs.get(index).getStreamManifest(), "UTF-8"));
            manETSPlayer.setDataSource(url);

            manETSPlayer.prepare(); // Opération qui prend beaucoup de temps.

        } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }

        manETSPlayer.start();
    }

    private void sendPlay(final int index) {

        if (isStreamMode) {
            playStream(index);
        } else {
            HttpTask playHttpTask = new HttpTask(ip,port);
            playHttpTask.execute("play", String.valueOf(index));
        }
    }

}
