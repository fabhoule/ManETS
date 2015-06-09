package ca.etsmtl.server;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import ca.etsmtl.server.models.ManETS_Player;
import ca.etsmtl.server.models.Playlist;
import ca.etsmtl.server.models.Song;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server extends NanoHTTPD {

	private final File MEDIA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	private final String STREAM_EXT = "m3u8";
	private final String MEDIA_EXT = "mp3";

	private ManETS_Player manETSPlayer;
	final private Gson gson = new GsonBuilder().create();

	public Server() {
		super(8080);
		setUp();
	}

	public Server(String hostname, int port) {
		super("192.168.0.1", 8080);
		setUp();
	}

	private void setUp() {
		manETSPlayer = new ManETS_Player();
		final List<Playlist> playlists = new ArrayList<>();
		final Playlist playlist = new Playlist();
		playlist.setName("Default");

		final List<Song> songList = new ArrayList<>();
		for (final File file : MEDIA_FOLDER.listFiles()) {

			if (getFileExtension(file).equals(".mp3")) {
				songList.add(buildFromPath(file.getAbsolutePath()));
			}
		}

		playlist.setSongs(songList);
		playlists.add(playlist);
		manETSPlayer.setPlaylists(playlists);
		manETSPlayer.setCurrentPlaylistIdx(0);

		manETSPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				if (manETSPlayer.isRepeatOne()) {
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
		final String[] request = uri.split("/");
		String body = "";

		//Mapping des calls http
		if (request.length >= 2) {
			switch (request[1]) {
				case "songs":
					body = processSongs(request, method);
					break;
				case "playlists":
					if (method.equals(Method.GET)) { //URL format -> /playlists/id
						final int index = Integer.parseInt(request[2]);
						body = gson.toJson(manETSPlayer.getPlaylists().get(index));
						//PUT call to playlists
					} else if (method.equals(Method.PUT)) {
						//URL format -> /playlists/action
						switch (request[2]) {
							case "looping":
								manETSPlayer.setLooping(!manETSPlayer.isLooping());
								break;
							case "repeat":
								manETSPlayer.setRepeatOne(!manETSPlayer.isRepeatOne());
								break;
							case "random":
								manETSPlayer.setRandom(!manETSPlayer.isRandom());
								break;
							default:
								body = "Not Supported";
						}
					}
					break;
				default:
					return processFile(uri);
			}
		} else {
			return processFile(uri);
		}

		return newFixedLengthResponse(body);
	}

	private Song previous() {

		final Playlist currentPlaylist = manETSPlayer.getPlaylists().get(manETSPlayer.getCurrentPlaylistIdx());
		int previousIdx;

		if (manETSPlayer.getCurrentSongIdx() <= 0) {
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

		if (manETSPlayer.isRandom()) {
			final Random rn = new Random();
			nextIndex = rn.nextInt(currentPlaylist.getSongs().size() - 1 + 1);
		} else {
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

		if (manETSPlayer.isPlaying()) {
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

	private void pause() {

		manETSPlayer.pause();
	}

	private void stopSong() {

		manETSPlayer.stop();
		manETSPlayer.reset();
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

		final String[] splittedPath = path.split("/");

		song.setStreamManifest(splittedPath[splittedPath.length - 1].replace(MEDIA_EXT, STREAM_EXT));

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

	private String processSongs(final String[] request, final Method method) {

		String body = "";
		if (method.equals(Method.PUT)) {
			if (request.length >= 4) { //URL format -> /songs/id/action
				if (request[3].equals("play")) {

					final int index = Integer.parseInt(request[2]);
					final Song song = play(index);
					body = gson.toJson(song);
				}
			} else { //URL format -> /songs/action
				switch (request[2]) {
					case "pause":
						if (manETSPlayer.isPlaying()) {
							pause();
							body = "OK";
						} else {
							manETSPlayer.start();
							body = "OK";
						}
						break;
					case "stop":
						stopSong();
						body = "OK";
						break;
					case "next":
						body = gson.toJson(next(true));
						break;
					case "previous":
						body = gson.toJson(previous());
						break;
					default:
						body = "Not Supported";
				}
			}
		} else if (method.equals(Method.GET)) {
			if(request.length >= 3 && !request[2].equals("")) {//URL format -> /songs/id/

				final int index = Integer.parseInt(request[2]);
				final Song song = manETSPlayer.getPlaylists().get(manETSPlayer.getCurrentPlaylistIdx()).getSongs().get(index);
				body = gson.toJson(song);
			} else {//URL format -> /songs

				final List<Song> songs = manETSPlayer.getPlaylists().get(manETSPlayer.getCurrentPlaylistIdx()).getSongs();
				body = gson.toJson(songs);
			}
		} else {
			body = "Not supported";
		}

		return body;
	}

	private NanoHTTPD.Response processFile(final String uri) {

		FileInputStream fis = null;

		final String mimeType = (uri.contains(".m3u8") ? "audio/mpeg-url" : "video/MP2T");
		long fileSize = 0;

		try {
			fis = new FileInputStream(MEDIA_FOLDER + uri);
			fileSize = fis.getChannel().size();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new NanoHTTPD.Response(Response.Status.OK, mimeType, fis, fileSize);
	}
}
