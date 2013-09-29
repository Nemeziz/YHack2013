package edu.cmu.yahoo.travelog;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DisplayPics extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_pics);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_pics, menu);
		return true;
	}

}
