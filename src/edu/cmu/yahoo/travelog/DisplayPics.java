package edu.cmu.yahoo.travelog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.model.GraphObject;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class DisplayPics extends Activity {
	private double latitude;
	private double longitude;
	private ArrayList<String> fbPicURLs = new ArrayList<String>();
	private ArrayList<String> friendIDs = new ArrayList<String>();
	private static String APP_ID = "385471371581009";
	private static String APP_SECRET = "772e3a9987eca4f9d671309bf260a47e";
	private static final String ACCESS_TOKEN = "CAACEdEose0cBAFWZAAfXztusZBc0u9iuzKyo17ICEJ0FNhOgCYWkoSFR3fVlyuXuZAEa3KaTDJMsuCziUQ7ZCtnUUzM4mtsqT4kzYXUWEtMhoOiT8hsocGl79cXhFwbkqQTsCBmZCo8c6vZA5njkPyfkNKT6ZAoNefR2x8kT1S8qK7bXDJtZBZASbRMGpUwGqpmnitOaJGBYWUQZDZD";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_pics);

		Intent intent = getIntent();
		latitude = intent.getDoubleExtra("Latitude", 0.0);
		longitude = intent.getDoubleExtra("Longitude", 0.0);

		getPhotosFromFb();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_pics, menu);
		return true;
	}

	public void getPhotosFromFb()
	{
		Thread fbThread = new Thread(new Runnable()
		{
			@Override
			public void run() {
				HttpClient client = new DefaultHttpClient();

				try {
					URI uri = new URI("https","graph.facebook.com","/me/friends","access_token=CAACEdEose0cBAFWZAAfXztusZBc0u9iuzKyo17ICEJ0FNhOgCYWkoSFR3fVlyuXuZAEa3KaTDJMsuCziUQ7ZCtnUUzM4mtsqT4kzYXUWEtMhoOiT8hsocGl79cXhFwbkqQTsCBmZCo8c6vZA5njkPyfkNKT6ZAoNefR2x8kT1S8qK7bXDJtZBZASbRMGpUwGqpmnitOaJGBYWUQZDZD", null);
					HttpGet get = new HttpGet(uri.toASCIIString());
					HttpResponse responseGet;
					responseGet = client.execute(get);
					HttpEntity responseEntity = responseGet.getEntity();
					String response = EntityUtils.toString(responseEntity);
					Log.d("test", response);
					getFriendIDs(response);
					getLocationPhotos();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}							
			}
		});
		fbThread.start();


	}

	public String getAuthToken()
	{
		String authToken = null;
		HttpClient client = new DefaultHttpClient();
		String getURL = "https://graph.facebook.com/oauth/access_token?client_id="
				+ APP_ID + "&client_secret=" + APP_SECRET + "&grant_type=client_credentials";          
		HttpGet get = new HttpGet(getURL);
		HttpResponse responseGet;
		try {
			responseGet = client.execute(get);
			HttpEntity responseEntity = responseGet.getEntity();
			String response = EntityUtils.toString(responseEntity);
			Log.d("test", response);
			authToken = response.substring(response.indexOf("=")+1);
			Log.d("test",authToken);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return authToken;
	}

	private void getFriendIDs(String JSONString) throws JSONException {
		final JSONObject obj = new JSONObject(JSONString);
		final JSONArray data = obj.getJSONArray("data");

		final int n = data.length();
		for (int i = 0; i < n; ++i) {
			friendIDs.add(data.getJSONObject(i).getString("id"));
		}

	}

	public void getLocationPhotos()
	{
		Thread fbThread = new Thread(new Runnable()
		{
			@Override
			public void run() {
				HttpClient client = new DefaultHttpClient();
				for (String id: friendIDs)
				{
					try {

						URI uri = new URI("https","graph.facebook.com", "/" + id + "/photos", "latitude=" + latitude + "&longitude="+ longitude + "&access_token=CAACEdEose0cBAFWZAAfXztusZBc0u9iuzKyo17ICEJ0FNhOgCYWkoSFR3fVlyuXuZAEa3KaTDJMsuCziUQ7ZCtnUUzM4mtsqT4kzYXUWEtMhoOiT8hsocGl79cXhFwbkqQTsCBmZCo8c6vZA5njkPyfkNKT6ZAoNefR2x8kT1S8qK7bXDJtZBZASbRMGpUwGqpmnitOaJGBYWUQZDZD", null);
						HttpGet get = new HttpGet(uri.toASCIIString());
						HttpResponse responseGet;
						responseGet = client.execute(get);
						HttpEntity responseEntity = responseGet.getEntity();
						String response = EntityUtils.toString(responseEntity);
						getPhotoURL(response);
						Log.d("TestURL", fbPicURLs.toString());
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		fbThread.start();
	}
	
	private void getPhotoURL(String JSONString) throws JSONException
	{
		final JSONObject obj = new JSONObject(JSONString);
		final JSONArray data = obj.getJSONArray("data");

		final int n = data.length();
		for (int i = 0; i < n; ++i) {
			fbPicURLs.add(data.getJSONObject(i).getString("source"));
		}
	}
}
