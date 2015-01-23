/**
 * 
 */
package com.pushio.basic;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author wtyree
 *
 */
public class AboutThisApp extends Activity {
	private TextView mTextView;
	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_about_app);
	        mTextView = (TextView)findViewById(R.id.aboutTextView);
	        mTextView.setText(Html.fromHtml(getResources().getString(R.string.about_us_html)));
	        
	        //If this activity gets launched from a notification then there will be an Intent.
	        //If this activity gets launched from the "About" menu in the Main activity there
	        //won't be an intent
	        if (getIntent().getStringExtra("alert") != null) {
	        String alert = getIntent().getStringExtra("alert");
			Toast.makeText(getBaseContext(), alert, Toast.LENGTH_LONG).show();
	        }
	 }
}
