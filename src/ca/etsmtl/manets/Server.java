package ca.etsmtl.manets;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.ManETS_Player;
import models.Playlist;
import models.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server extends NanoHTTPD {

	private final File MEDIA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

	private ManETS_Player manETSPlayer;
	final private Gson gson = new GsonBuilder().create();

	public Server () {
		super(8080);
		setUp();
	}

	public Server(String hostname, int port) {
		super("192.168.0.1", 8080);
		setUp();
	}

	private void setUp(){
		manETSPlayer = new ManETS_Player();
		final List<Playlist> playlists = new ArrayList<>();
		final Playlist playlist = new Playlist();
		playlist.setName("Default");

		final List<Song> songList =new ArrayList<>();
		for(final File file: MEDIA_FOLDER.listFiles()) {
			songList.add(buildFromPath(file.getAbsolutePath()));
		}

		playlist.setSongs(songList);
		playlists.add(playlist);
		manETSPlayer.setPlaylists(playlists);
	}

	@Override
	public Response serve(final IHTTPSession session) {

		final String uri = session.getUri();
		final Method method = session.getMethod();
		final Map<String,String> queryParams = session.getParms();

		String body = "";

		if(uri.contains("/songs")) {
			if (method.equals(Method.PUT)) {
				if (uri.contains("/play")) {
					if (queryParams.get("index") != null && !queryParams.get("index").equals("")) {
						final int index = Integer.parseInt(queryParams.get("index"));
						final Song song = play(index);

						body = gson.toJson(song);

					} else {
						body = "Missing params";
					}
				}else if (uri.contains("/pause")) {
					if(manETSPlayer.isPlaying()) {
						pause();
						body = "OK";
					} else {
						manETSPlayer.start();
						body = "OK";
					}
				}
			}
		} else if (uri.contains("/playlist")){
			if(method.equals(Method.GET)) {
				if(queryParams.get("index") != null && !queryParams.get("index").equals("")) {
					body = gson.toJson(manETSPlayer.getCurrentPlaylist());
				} else {
					body = "Missing params";
				}
			}
		}

		return newFixedLengthResponse(body);
	}

	private Song play(final int index){

		final Song song = buildFromPath(manETSPlayer.getPlaylists().get(0).getSongs().get(index).getLocation());

		try {
			manETSPlayer.setDataSource(song.getLocation());

			manETSPlayer.prepare(); // Opération qui prend beaucoup de temps.

		} catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
			e.printStackTrace();
		}

		manETSPlayer.start();

		return song;
	}

	private void pause(){

		manETSPlayer.pause();
	}

	private Song buildFromPath(final String path) {

		final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
		mediaMetadataRetriever.setDataSource(path);

		final Song song = new Song();
		song.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
		song.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		song.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
		song.setDuration(Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
		song.setLocation(path);

		return song;
	}
}
