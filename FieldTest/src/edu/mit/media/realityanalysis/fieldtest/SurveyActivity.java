package edu.mit.media.realityanalysis.fieldtest;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SurveyActivity extends FragmentActivity {
	private String mSurveyUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		setFinishOnTouchOutside(false);
		Intent intent = getIntent();
		
		if (intent != null && intent.getData() != null) {
			mSurveyUrl = intent.getDataString();
			String url;
			
			try {
				PDSWrapper pds = new PDSWrapper(this);
				url = pds.buildAbsoluteUrl(mSurveyUrl);
			} catch (Exception e) {
				Log.e("SurveyActivity", "Unable to create PDS", e);
				return;
			}
			SurveyWebViewJavascriptInterface jsInterface = new SurveyWebViewJavascriptInterface(this);
			Fragment settingsFragment = WebViewFragment.Create(url, "Survey", this, null, jsInterface);
			
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			
			fragmentTransaction.add(R.id.settings_content_layout, settingsFragment);
			fragmentTransaction.commit();			
		}		
	}
	
	public void finishSurvey() {
		PreferencesWrapper prefs = new PreferencesWrapper(this);
		prefs.removePendingSurvey(mSurveyUrl);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				// NOTE: currently all surveys are using notification id 2 - this may change and really shouldn't be hard-coded
				// That said, demo or die.
				notificationManager.cancel(2);
				finish();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
