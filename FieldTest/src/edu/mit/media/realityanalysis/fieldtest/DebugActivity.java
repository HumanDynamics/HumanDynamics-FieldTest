package edu.mit.media.realityanalysis.fieldtest;

import java.util.ArrayList;

import edu.mit.media.funf.FunfManager;
//import edu.mit.media.funf.configured.FunfConfig;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DebugActivity extends Activity {

	//private MainPipeline mPipeline;
	private FunfManager mFunfManager;
	private TextView mUploadPeriodTextView;
	
	
	private ServiceConnection mPipelineConnection = new ServiceConnection() {
		public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
			//mPipeline = ((MainPipeline.LocalBinder) service).getPipeline();			
			//FunfConfig config = mPipeline.getConfig();
			
			//mUploadPeriodTextView.setText(String.format("%s: %d", "Upload Interval", config.getDataUploadPeriod()));
			
			mFunfManager = ((FunfManager.LocalBinder) service).getManager();
		};
		
		public void onServiceDisconnected(android.content.ComponentName name) {
			mFunfManager = null;
		};		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		mUploadPeriodTextView = (TextView) DebugActivity.this.findViewById(R.id.data_upload_period_textview);
		
		bindService(new Intent(this, FunfManager.class), mPipelineConnection, 0);
		
		TextView pdsLocationTextView = (TextView) findViewById(R.id.pds_location_textview);
		PreferencesWrapper prefs = new PreferencesWrapper(this);
		if (!TextUtils.isEmpty(prefs.getPDSLocation())) {
			pdsLocationTextView.setText(prefs.getPDSLocation());
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_debug, menu);
		return true;
	}

	@Override
	protected void onPause() {
		if (mPipelineConnection != null) {
			unbindService(mPipelineConnection);
		}
		super.onPause();
	}
}
