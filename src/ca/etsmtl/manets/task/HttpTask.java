package ca.etsmtl.manets.task;

import android.os.AsyncTask;
import ca.etsmtl.server.NanoHTTPD;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpTask extends AsyncTask<String, Void, String> {

	private String ip;
	private String port;

	public HttpTask(final String ip, final String port) {
		this.ip = ip;
		this.port = port;
	}

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
			} else if(params[0].equals("getSongs")) {

				try {
					return doGetSongs();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private String doPlay(final int index) {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/songs/%s/play",ip,port, index));
	}

	private String doGetPlaylist(final int index) {
		return doRequest(NanoHTTPD.Method.GET, String.format("http://%s:%s/playlists/%s",ip,port, index));
	}

	private String doGetSongs() {
		return doRequest(NanoHTTPD.Method.GET, String.format("http://%s:%s/songs", ip, port));
	}

	private String doPause() {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/songs/pause", ip, port));
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
