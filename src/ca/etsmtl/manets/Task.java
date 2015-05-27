package ca.etsmtl.manets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class Task extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		
		try {
			URL	url = new URL("http://10.0.3.15:8080/play");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("PUT");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		   	
		
		return null;
	}

	
}
