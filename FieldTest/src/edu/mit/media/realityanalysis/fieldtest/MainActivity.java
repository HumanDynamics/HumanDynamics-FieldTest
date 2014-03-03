package edu.mit.media.realityanalysis.fieldtest;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONObject;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.realityanalysis.survey.Survey;
import edu.mit.media.realityanalysis.survey.SurveyFactory;

import android.app.Notification;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private ViewPager mViewPager;
	private WebViewFragmentPagerAdapter mFragmentAdapter;	
	private FunfManager mFunfManager;
	private HashSet<String> mSurveysShown = new HashSet<String>();
	
	private ServiceConnection mPipelineConnection = new ServiceConnection() {
		public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {			
			mFunfManager = ((FunfManager.LocalBinder) service).getManager();
		};	
		
		public void onServiceDisconnected(android.content.ComponentName name) {
			mFunfManager = null;
		};		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferencesWrapper prefs = new PreferencesWrapper(this);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		    WebView.setWebContentsDebuggingEnabled(true);
		}

		
		String token = prefs.getAccessToken();
		String uuid = prefs.getUUID();
		String pdsLocation = prefs.getPDSLocation();
		
		if (token != null && uuid != null && pdsLocation != null) {
			setContentView(R.layout.activity_main);
			mViewPager = (ViewPager) findViewById(R.id.viewpager);
			
			if (mFragmentAdapter == null) {
				mFragmentAdapter = new WebViewFragmentPagerAdapter(getSupportFragmentManager());
				addSurveyViews(prefs);
				try {
					PDSWrapper pds = new PDSWrapper(this);
					addStandardViews(pds);
				} catch (Exception e) {
					Log.e("MainActivity", "Unable to create PDS", e);
				}
				mViewPager.setAdapter(mFragmentAdapter);
			}
			
		} else {
			startLoginActivity();
			finish();
		}
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		addSurveyViews(new PreferencesWrapper(this));
	}
	
	private void addSurveyViews(PreferencesWrapper prefs) {
		if (prefs.getPendingSurveys() != null) {
			for (String uri : prefs.getPendingSurveys()) {
				//SurveyWebViewJavascriptInterface jsInterface = new SurveyWebViewJavascriptInterface(mViewPager, this);
				//WebViewFragment webViewFragment = WebViewFragment.Create(surveyUrl, "Survey", this, mViewPager, jsInterface);
				//jsInterface.setWebViewFragment(webViewFragment);
				//mFragmentAdapter.addItem(webViewFragment);
				if (!mSurveysShown.contains(uri)) {
					mSurveysShown.add(uri);
					Intent intent = new Intent(this, SurveyActivity.class);
					intent.setData(Uri.parse(uri));
					startActivity(intent);
				}
			}
		}
	}
		
	private void addStandardViews(PDSWrapper pds) {		
		String radialUrl = pds.buildAbsoluteUrl(R.string.radial_relative_url);//"file:///android_asset/web/socialHealthRadial.html?bearer_token=868a387700&datastore_owner=5241576e-43da-4b08-8a71-b477f931e021";//
		String activityUrl = pds.buildAbsoluteUrl(R.string.activity_relative_url);
		String socialUrl = pds.buildAbsoluteUrl(R.string.social_relative_url);
		String focusUrl = pds.buildAbsoluteUrl(R.string.focus_relative_url);
		String placesUrl = pds.buildAbsoluteUrl(R.string.places_relative_url);
		String adminUrl = pds.buildAbsoluteUrl("/admin/audit.html");
		
		mFragmentAdapter.addItem(WebViewFragment.Create(radialUrl, "My Social Health", this, mViewPager));
		mFragmentAdapter.addItem(WebViewFragment.Create(activityUrl, "Activity", this, mViewPager));
		mFragmentAdapter.addItem(WebViewFragment.Create(socialUrl, "Social", this, mViewPager));
		mFragmentAdapter.addItem(WebViewFragment.Create(focusUrl, "Focus", this, mViewPager));
		//mFragmentAdapter.addItem(WebViewFragment.Create(placesUrl, "Places", this, mViewPager));
		//mFragmentAdapter.addItem(WebViewFragment.Create(sharingUrl, "Settings", getApplicationContext()));
		//mFragmentAdapter.addItem(WebViewFragment.Create(adminUrl, "Audit Logs", this, mViewPager));
	}
//	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}	
//	
//	@Override 
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.menu_settings:
//				startSettingsActivity();
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//		}
//	}
	
	private void startLoginActivity() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);	
	}
	
	private void startSettingsActivity() {
		Intent settingsIntent = new Intent(this, DebugActivity.class);
		startActivity(settingsIntent);	
	}	
}
