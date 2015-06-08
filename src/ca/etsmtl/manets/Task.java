package ca.etsmtl.manets;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Task extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {

		if(params.length >= 1) {
			if(params[0].equals("play")) {
                try {
                    return doPlay(Integer.parseInt(params[1]));
                } catch (final NumberFormatException e) {
                    e.printStackTrace();
                }
            }else if(params[0].equals("pause")) {
				try {
					return doPause();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			} else if(params[0].equals("getPlaylist")) {

				try {
					return doGetPlaylist(Integer.parseInt(params[1]));
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private String doPlay(final int index) {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://127.0.0.1:8080/songs/%s/play", index));
	}

	private String doGetPlaylist(final int index) {
		return doRequest(NanoHTTPD.Method.GET, String.format("http://127.0.0.1:8080/playlists/%s", index));
	}

	private String doPause() {
		return doRequest(NanoHTTPD.Method.PUT, "http://127.0.0.1:8080/songs/pause");
	}

	private String doRequest(final NanoHTTPD.Method method, final String uri) {
		try {
			final URL	url = new URL(uri);
			final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(method.name());

			final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String inputLine;
			final StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
