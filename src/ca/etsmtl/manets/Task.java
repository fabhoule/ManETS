package ca.etsmtl.manets;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Task extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {

		try {
			URL	url = new URL("http://127.0.0.1:8080/play");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("PUT");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			
			String inputLine;
			StringBuilder response = new StringBuilder();
			
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
