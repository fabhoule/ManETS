package ca.etsmtl.manets;

import android.media.MediaPlayer;
import android.os.Environment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.ManETS_Player;
import models.Playlist;
import models.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

			if(getFileExtension(file).equals(".mp3")) {
				songList.add(buildFromPath(file.getAbsolutePath()));
			}
		}

		playlist.setSongs(songList);
		playlists.add(playlist);
		manETSPlayer.setPlaylists(playlists);

		manETSPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				if(manETSPlayer.isRepeatOne()) {
					play(manETSPlayer.getCurrentSongIdx());
				} else {
					next(manETSPlayer.isLooping());
				}

			}
		});
	}

	@Override
	public Response serve(final IHTTPSession session) {

		final String uri = session.getUri();
		final Method method = session.getMethod();
		final Map<String,String> queryParams = session.getParms();

		String body = "";

		//Mapping des calls http
		//call to songs
		if(uri.contains("/songs")) {
			//PUT call to songs
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
				} else if (uri.contains("/stop")) {
					stopSong();
					body = "OK";
				} else if (uri.contains("/next")) {
					body = gson.toJson(next(true));
				} else if (uri.contains("/previous")) {
					body = gson.toJson(previous());
				}

			//GET call to songs
			} else if(method.equals(Method.GET)) {
				if (queryParams.get("index") != null && !queryParams.get("index").equals("")) {
					final int index = Integer.parseInt(queryParams.get("index"));
					final Song song = manETSPlayer.getPlaylists().get(manETSPlayer.getCurrentPlaylistIdx()).getSongs().get(index);
					body = gson.toJson(song);
				} else {
					body = "Missing params";
				}
			}

		//call to playlists
		} else if (uri.contains("/playlists")) {
			//GET call to playlists
			if(method.equals(Method.GET)) {
				if(queryParams.get("index") != null && !queryParams.get("index").equals("")) {
					final int index = Integer.parseInt(queryParams.get("index"));
					body = gson.toJson(manETSPlayer.getPlaylists().get(index));
				} else {
					body = "Missing params";
				}

			//PUT call to playlists
			} else if(method.equals(Method.PUT)) {

				if(uri.contains("/looping")) {
					manETSPlayer.setLooping(!manETSPlayer.isLooping());
				} else if(uri.contains("/repeat")) {
					manETSPlayer.setRepeatOne(!manETSPlayer.isRepeatOne());
				} else if(uri.contains("/random")) {
					manETSPlayer.setRandom(!manETSPlayer.isRandom());
				}
			}
		} else {

			FileInputStream fis = null;

			final String mimeType = (uri.contains(".m3u8") ? "audio/mpeg-url" : "video/MP2T");

			try {
				fis = new FileInputStream(MEDIA_FOLDER + uri);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return new NanoHTTPD.Response(Response.Status.OK, mimeType, fis, 260);

		}

		return newFixedLengthResponse(body);
	}

	private Song previous() {

		final Playlist currentPlaylist = manETSPlayer.getPlaylists().get(manETSPlayer.getCurrentPlaylistIdx());
		int previousIdx;

		if(manETSPlayer.getCurrentSongIdx() <= 0) {
			previousIdx = currentPlaylist.getSongs().size() - 1;
		} else {
			previousIdx = manETSPlayer.getCurrentSongIdx() - 1;
		}

		stopSong();
		return play(previousIdx);
	}

	private Song next(final boolean loop) {
		final Playlist currentPlaylist = manETSPlayer.getPlaylists().get(manETSPlayer.getCurrentPlaylistIdx());
		int nextIndex;

		if(manETSPlayer.isRandom()) {
			final Random rn = new Random();
			nextIndex = rn.nextInt(currentPlaylist.getSongs().size() - 1 + 1);
		}else {
			if (manETSPlayer.getCurrentSongIdx() >= currentPlaylist.getSongs().size() - 1) {
				if (loop) {
					nextIndex = 0;
				} else {
					stopSong();
					return null;
				}
			} else {
				nextIndex = manETSPlayer.getCurrentSongIdx() + 1;
			}
		}

		stopSong();
		return play(nextIndex);
	}

	private Song play(final int index) {

		if (manETSPlayer.isPlaying()){
			stopSong();
		}

		final Song song = buildFromPath(manETSPlayer.getPlaylists().get(0).getSongs().get(index).getLocation());

		try {
			manETSPlayer.setDataSource(song.getLocation());

			manETSPlayer.prepare(); // Opération qui prend beaucoup de temps.

		} catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
			e.printStackTrace();
		}

		manETSPlayer.start();
		manETSPlayer.setCurrentPlaylistIdx(0);
		manETSPlayer.setCurrentSongIdx(index);

		return song;
	}

	private void pause(){

		manETSPlayer.pause();
	}

	private void stopSong() {

		manETSPlayer.stop();
		manETSPlayer.reset();
	}

	private Song buildFromPath(final String path) {

//		final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//		mediaMetadataRetriever.setDataSource(path);

		final Song song = new Song();
//		song.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
//		song.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
//		song.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//		song.setDuration(Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
		song.setLocation(path);

		return song;
	}

	private String getFileExtension(final File file) {
		final String name = file.getName();
		try {
			return name.substring(name.lastIndexOf("."));

		} catch (Exception e) {
			return "";
		}

	}
}
