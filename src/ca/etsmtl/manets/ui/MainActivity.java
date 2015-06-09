package ca.etsmtl.manets.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import ca.etsmtl.server.Server;
import ca.etsmtl.server.models.ManETS_Player;
import ca.etsmtl.server.models.Song;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private final String PORT_KEY = "port";
    private final String IP_KEY = "ip";
    private final String IS_STREAM_MODE_KEY = "isStreamMode";

    private Server server;
    private List<Song> songs;
    private final ManETS_Player manETSPlayer = new ManETS_Player();
    private ProgressDialog progDailog;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        server = new Server();

        try {
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        progDailog = new ProgressDialog(this);
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);

        refreshSong(null);

        manETSPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (progDailog != null && progDailog.isShowing()){
                    progDailog.dismiss();
                }
                mp.start();
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

        if(!sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {

            HttpTask playHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            playHttpTask.execute("pause");
        } else {

            if (manETSPlayer.isPlaying()) {
                manETSPlayer.pause();
            } else {
                manETSPlayer.start();
            }
        }
    }

    public void previous(View view) {


        if (sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {
            if(manETSPlayer.getCurrentPlaylistIdx() == 0){
                playStream(songs.size() - 1);
            } else {
                playStream(manETSPlayer.getCurrentPlaylistIdx() - 1);
            }
        } else {

            HttpTask getHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            getHttpTask.execute("previous");
        }
    }

    public void next(View view) {

        if (sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {
            if(manETSPlayer.getCurrentPlaylistIdx() >= songs.size()){
                playStream(0);
            } else {
                playStream(manETSPlayer.getCurrentPlaylistIdx() + 1);
            }
        } else {

            HttpTask getHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            getHttpTask.execute("next");
        }
    }

    public void stop(View view) {

        if (sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {
            manETSPlayer.stop();
            manETSPlayer.reset();
        } else {

            HttpTask getHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            getHttpTask.execute("stop");
        }
    }

    public void shuffle(View view) {

        if(!sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {
            HttpTask getHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            getHttpTask.execute("random");
        }
    }

    public void repeat(View view) {

        if(!sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {
            HttpTask getHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            getHttpTask.execute("loop");
        }
    }

    public void getPlaylist() {
        HttpTask getHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
        getHttpTask.execute("getPlaylist", "0");
    }

    public void playStream(final int index) {

        new AsyncTask<String, Void, String>() {
            protected void onPreExecute() {
                progDailog.show();
            }

            protected String doInBackground(String... params) {
                if (manETSPlayer.isPlaying()) {
                    stop(null);
                }

                try {
                    manETSPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    final String url = String.format("http://%s:%s/%s", sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""),
                            URLEncoder.encode(songs.get(index).getStreamManifest(), "UTF-8"));
                    manETSPlayer.setDataSource(url);

                    manETSPlayer.prepare(); // Opération qui prend beaucoup de temps.

                } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
                    e.printStackTrace();
                }

                manETSPlayer.setCurrentPlaylistIdx(index);
                return "OK";
            }

            protected void onPostExecute(String result) {
                progDailog.dismiss();
            }
        }.execute();


    }

    public void displaySongs(final String result) {

        final ListView songList = (ListView)findViewById(R.id.songList);
        final Gson gson = new GsonBuilder().create();

        try {
            songs = gson.fromJson(result, new TypeToken<List<Song>>(){}.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        final ArrayAdapter<Song> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songs);
        songList.setAdapter(adapter);


        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                sendPlay(position);
            }
        });
    }

    private void sendPlay(final int index) {

        if (sharedPref.getBoolean(IS_STREAM_MODE_KEY, false)) {
            playStream(index);
        } else {
            HttpTask playHttpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));
            playHttpTask.execute("play", String.valueOf(index));
        }
    }

    public void refreshSong(View view){
        final HttpTask httpTask = new HttpTask(this, sharedPref.getString(IP_KEY, ""), sharedPref.getString(PORT_KEY, ""));

        httpTask.execute("getSongs");
    }

}
