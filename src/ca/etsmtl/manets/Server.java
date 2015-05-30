package ca.etsmtl.manets;

import android.media.MediaPlayer;
import android.os.Environment;

import java.io.IOException;

public class Server extends NanoHTTPD {

	public Server () {
		super(8080);
	}

	public Server(String hostname, int port) {
		super("192.168.0.1", 8080);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public Response serve(IHTTPSession session) {

		final String uri = session.getUri();
		final Method method = session.getMethod();

		if(uri.contains("/play")) {
			if(method.equals(Method.PUT)) {
				play();
			}
		}

		final String msg = "HELLO WORLD!!";



		return newFixedLengthResponse(msg);
	}

	private void play(){

		MediaPlayer mediaPlayer = new MediaPlayer();

		try {
			mediaPlayer.setDataSource(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/LimpBizkit-THIEVES_10_11.mp3");

			mediaPlayer.prepare(); // Op�ration qui prend beaucoup de temps.

		} catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();



//			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/LimpBizkit-THIEVES_10_11.mp3");
//			file.exists()
		}

		mediaPlayer.start();
	}
}
