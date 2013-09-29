package edu.cmu.yahoo.travelog;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class Search extends Activity {
	private AutoCompleteTextView searchPlace;
	private ArrayList<String> places = new ArrayList<String>();
	private final String YAHOO_APP_ID = "oZKjTRfV34HrTKHcm21ofiEWRiTSuyRiPYJ7iGoNUiULPIJtdkIUtEQ_S8xf3B.j4U7RKk1imVenH5XC26hrNb4hQQMc1jE-";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		searchPlace = (AutoCompleteTextView) findViewById(R.id.searchTextView);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, places);
		searchPlace.setAdapter(adapter);

		searchPlace.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				//getPlaces(searchPlace.getText().toString());			
			}
		});        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	public void findPhotos(View view)
	{
		getPlaces(searchPlace.getText().toString());
	}

	public void getPlaces(final String startLetters)
	{
		Thread placesThread = new Thread(new Runnable()
		{

			@Override
			public void run() {

				String requestText = "http://where.yahooapis.com/v1/places.q(" + startLetters + ")?appid=" + YAHOO_APP_ID;
				HttpResponse response = null;
				try {        
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet(requestText);
					request.setHeader("Accept", "application/xml");
					response = client.execute(request);

					HttpEntity entity = response.getEntity();
					String xmlString = EntityUtils.toString(entity);
					//Log.d("XML String",xmlString);

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = factory.newDocumentBuilder();
					InputSource inStream = new InputSource();
					inStream.setCharacterStream(new StringReader(xmlString));
					Document doc = db.parse(inStream); 

					NodeList list = doc.getElementsByTagName("place");
					for (int i =0; i< list.getLength(); i++)
					{
						places.add(list.item(i).getChildNodes().item(2).getNodeValue());
						Log.d("Place Name",list.item(i).getChildNodes().item(2).getNodeValue());
					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} 

			}

		});
		placesThread.start();     
	}
}

