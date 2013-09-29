package edu.cmu.yahoo.travelog;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DisplayPics extends Activity {
	private double latitude;
	private double longitude;
	private ArrayList<String> fbPicURLs = new ArrayList<String>();
	private ArrayList<String> friendIDs = new ArrayList<String>();
	private static String APP_ID = "385471371581009";
	private static String APP_SECRET = "772e3a9987eca4f9d671309bf260a47e";
	private static final String ACCESS_TOKEN = "CAACEdEose0cBAMR881CMz9YXQ1wAm0mBACuPWcS1IE9YnQrU4HA4g3AXY6YDrl3wdDSYVGraC8nI8LecQIaZBbzrpJrnEAGPuz2KXYQt11d9EqXZCN48k0se5vtM9BJfzbOvzdmq9gF2hwMEwfHEGFNYBQkxlJV9GRneXZASggchKiTH5tvgpSA7UV5Vjfu9LfICUf10AZDZD";
	private Context context;
	private Activity displayPics;
	private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
	public ProgressDialog pd;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_pics);
		context = getApplicationContext();
		displayPics = this;

		Intent intent = getIntent();
		latitude = intent.getDoubleExtra("Latitude", 0.0);
		longitude = intent.getDoubleExtra("Longitude", 0.0);
		Log.d("LAT","Lat:" + latitude);

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
					URI uri = new URI("https","graph.facebook.com","/me/friends","access_token=" + ACCESS_TOKEN, null);
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

						URI uri = new URI("https","graph.facebook.com", "/" + id + "/photos", "latitude=" + latitude + "&longitude="+ longitude + "&access_token=" + ACCESS_TOKEN + "&limit=2", null);
						HttpGet get = new HttpGet(uri.toASCIIString());
						HttpResponse responseGet;
						responseGet = client.execute(get);
						HttpEntity responseEntity = responseGet.getEntity();
						String response = EntityUtils.toString(responseEntity);
						getPhotoURL(response);
						Log.d("TestURL", response);						
						images = new FetchBitmaps().execute(fbPicURLs).get();
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
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
			//Log.d("Test", data.getJSONObject(i).getString("latitude"));
			fbPicURLs.add(data.getJSONObject(i).getString("source"));
		}
	}

	private void displayGallery()
	{
		Gallery ga = (Gallery)findViewById(R.id.Gallery01);
		ga.setAdapter(new ImageAdapter(this));

		final ImageView imageView = (ImageView)findViewById(R.id.ImageView01);
		ga.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				imageView.setImageBitmap(images.get(arg2));
			}
		});
	}
	public class ImageAdapter extends BaseAdapter {

		private Context ctx;
		int imageBackground;

		public ImageAdapter(Context c) {
			ctx = c;
			TypedArray ta = obtainStyledAttributes(R.styleable.Gallery1);
			imageBackground = ta.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 1);
			ta.recycle();
		}

		@Override
		public int getCount() {

			return fbPicURLs.size();
		}

		@Override
		public Object getItem(int arg0) {

			return arg0;
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			final ImageView iv = new ImageView(ctx);
			iv.setImageBitmap(images.get(arg0));
			iv.setScaleType(ImageView.ScaleType.FIT_XY);
			iv.setLayoutParams(new Gallery.LayoutParams(200,170));
			iv.setBackgroundResource(imageBackground);
			return iv;
		}

	}

	public class FetchBitmaps extends AsyncTask<ArrayList<String>, Void, ArrayList<Bitmap>> {

		
		@Override
		protected ArrayList<Bitmap> doInBackground(ArrayList<String>... params) {
			ArrayList<Bitmap> images = new ArrayList<Bitmap>();
			for ( String urlString: params[0])
			{
				URL url;
				try {
					url = new URL(urlString);
					images.add(BitmapFactory.decodeStream(url.openConnection().getInputStream()));

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return images;

		}

		@Override
		protected void onPostExecute(ArrayList<Bitmap> result) {
			//pd.dismiss();
			displayGallery();
			super.onPostExecute(result);
		}

	}

}

