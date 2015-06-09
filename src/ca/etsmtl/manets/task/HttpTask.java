package ca.etsmtl.manets.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import ca.etsmtl.manets.ui.MainActivity;
import ca.etsmtl.server.NanoHTTPD;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpTask extends AsyncTask<String, Void, String> {

	private String ip;
	private String port;
	private Activity activity;
	private ProgressDialog progDailog;
	private boolean isGetSong;

	public HttpTask(final Activity activity, final String ip, final String port) {
		this.ip = ip;
		this.port = port;
		this.activity = activity;
		this.progDailog = new ProgressDialog(activity);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progDailog.setMessage("Loading...");
		progDailog.setIndeterminate(false);
		progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDailog.setCancelable(true);
		progDailog.show();
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
            } else if(params[0].equals("pause")) {
				try {
					return doPause();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			} else if(params[0].equals("stop")) {
				try {
					return doStop();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			} else if(params[0].equals("next")) {
				try {
					return doNext();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			} else if(params[0].equals("previous")) {
				try {
					return doPrevious();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			} else if(params[0].equals("random")) {
				try {
					return doRandom();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			} else if(params[0].equals("loop")) {
				try {
					return doLoop();
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
					isGetSong = true;
					return doGetSongs();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(final String result) {
		super.onPostExecute(result);
		if(isGetSong) {
			((MainActivity) activity).displaySongs(result);
		}
		progDailog.dismiss();
		isGetSong = false;
	}

	private String doLoop() {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/playlists/looping", ip, port));
	}

	private String doRandom() {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/playlists/random", ip, port));
	}

	private String doPrevious() {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/songs/previous", ip, port));
	}

	private String doNext() {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/songs/next", ip, port));
	}

	private String doStop() {
		return doRequest(NanoHTTPD.Method.PUT, String.format("http://%s:%s/songs/stop", ip, port));
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
