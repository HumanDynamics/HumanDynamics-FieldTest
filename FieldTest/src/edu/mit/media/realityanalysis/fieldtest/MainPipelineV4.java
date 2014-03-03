package edu.mit.media.realityanalysis.fieldtest;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.config.Configurable;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.ContinuableProbe;
import edu.mit.media.funf.storage.DatabaseService;
import edu.mit.media.funf.storage.HttpUploadService;
import edu.mit.media.funf.storage.NameValueDatabaseService;
import edu.mit.media.funf.storage.UploadService;
import edu.mit.media.funf.util.AsyncSharedPrefs;
import edu.mit.media.funf.util.LogUtil;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.content.IntentSender;
import android.util.Log;

public class MainPipelineV4 extends BasicPipeline {

	public static final String LAST_DATA_UPLOAD = "LAST_DATA_UPLOAD";	
	
	@Configurable
	private String dataUploadUrl;
	
	@Configurable
	private String updateUrl;
	
	private AsyncTask<Void, Void, Void> mRegisterTask;
	
	@Override
	public void onCreate(final FunfManager manager) {
		super.onCreate(manager);	
		this.manager = manager;	
		
		for (JsonElement probeConfig : data) {
			Probe probe = manager.getGson().fromJson(probeConfig, Probe.class);
			
			if (probe instanceof ContinuableProbe) {
				String checkpointString = AsyncSharedPrefs.async(manager.getSharedPreferences(manager.getString(R.string.prefs_file), Context.MODE_PRIVATE)).getString(probe.getClass().getName()+"_checkpoint", null);
				
				if (checkpointString != null) {
					JsonElement checkpoint = (new JsonParser()).parse(checkpointString);
					((ContinuableProbe)probe).setCheckpoint(checkpoint);
				}
			}
		}		

		// Handle GCM registration
		final PDSWrapper pds;
		try {
			pds = new PDSWrapper(manager);
		} catch (Exception e) {
			Log.w("MainPipelineV4", "Couldn't construct PDS", e);
			return;
		}
		
		mRegisterTask = new AsyncTask<Void,Void,Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				GCMRegistrar.checkDevice(manager);
				GCMRegistrar.checkManifest(manager);
				String regId = GCMRegistrar.getRegistrationId(manager);
				if (regId.equals("")) {
					GCMRegistrar.register(manager, manager.getString(R.string.gcm_sender_id));
					// NOTE: don't need to register with server here if it's a new registration as the GCMIntentService will handle that
				} else if (!GCMRegistrar.isRegisteredOnServer(manager) && !pds.registerGCMDevice(regId)) {
					GCMRegistrar.unregister(manager);					
				}	
				return null;
			}
			
			protected void onPostExecute(Void result) {
				mRegisterTask = null;
			}
		};
		
		mRegisterTask.execute(null, null, null);
	}
	
	@Override
	public void onRun(String action, JsonElement config) {
		super.onRun(action, config);
		if (action.equalsIgnoreCase("archive")) {
			archiveData();
		}
		if (action.equalsIgnoreCase("upload")){
			uploadData();
		}
		if (action.equalsIgnoreCase("notify")) {
			checkForNotifications();
		}
		if (action.equalsIgnoreCase("update")) {
			updatePipelines();
		}
		if (action.equalsIgnoreCase("save")) {
			saveToPDS();
		}
	}
	
	
	@Override
	public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {
		super.onDataReceived(probeConfig, data);
		String probeName = probeConfig.getAsJsonPrimitive("@type").getAsString();//probeConfig.get("@type").toString();
		long timestamp = data.get("timestamp").getAsLong();

		storeData(probeName, timestamp, data);
	}
	
	@Override
	public void onDataCompleted(IJsonObject probeConfig, JsonElement checkpoint) {
		// Unfortunately, continuableProbes don't handle checkpoints on their own... we need to handle that here
		super.onDataCompleted(probeConfig, checkpoint);
		
		if (checkpoint != null) {
			SharedPreferences prefs = AsyncSharedPrefs.async(manager.getSharedPreferences(manager.getString(R.string.prefs_file), Context.MODE_PRIVATE));
			Editor editor = prefs.edit();
			Probe probe = manager.getGson().fromJson(probeConfig,  Probe.class);
			
			editor.putString(probe.getClass().getName() + "_checkpoint", checkpoint.toString());
			editor.commit();
			
			if (probe instanceof ContinuableProbe) {
				((ContinuableProbe)probe).setCheckpoint(checkpoint);
			}
		}
	}
	
	public void updatePipelines() {
//		new Thread() {
//			@Override
//			public void run() {
//				PDSWrapper pds = new PDSWrapper(manager);			
//				Map<String, Pipeline> pipelines = pds.getPipelines();
//				
//				for (String name : pipelines.keySet()) {
//					manager.registerPipeline(name, pipelines.get(name));
//				}
//			}
//		}.start();
	}
	
	public void saveToPDS() { 
//		new Thread() {
//			@Override
//			public void run() {
//				PDSWrapper pds = new PDSWrapper(manager);
//				pds.savePipelineConfig(manager.getPipelineName(MainPipelineV4.this), MainPipelineV4.this);
//			}
//		}.start();
	}
	
	private void checkForNotifications() {
//		Intent i = new Intent(manager, NotificationService.class);
//		manager.startService(i);
	}
	
	private void storeData(String name, long timestamp, IJsonObject data) {
		Bundle b = new Bundle();
		b.putString(NameValueDatabaseService.DATABASE_NAME_KEY,  manager.getPipelineName(this));
		b.putLong(NameValueDatabaseService.TIMESTAMP_KEY, timestamp);
		b.putString(NameValueDatabaseService.NAME_KEY, name);
		b.putString(NameValueDatabaseService.VALUE_KEY, data.toString());
		Intent i = new Intent(manager, getDatabaseServiceClass());
		i.setAction(DatabaseService.ACTION_RECORD);
		i.putExtras(b);
		manager.startService(i);
	}
	
	public void archiveData() {
		Intent i = new Intent(manager, getDatabaseServiceClass());
		i.setAction(DatabaseService.ACTION_ARCHIVE);
		i.putExtra(DatabaseService.DATABASE_NAME_KEY, manager.getPipelineName(this));
		manager.startService(i);
	}
	
	public void uploadData() {		
		archiveData();		
		if (dataUploadUrl != null && dataUploadUrl != "") {
			String archiveName = manager.getPipelineName(this);
			Intent i = new Intent(manager, getUploadServiceClass());
			i.putExtra(UploadService.ARCHIVE_ID, archiveName);
			i.putExtra(UploadService.REMOTE_ARCHIVE_ID, dataUploadUrl);
			manager.startService(i);
		}
		
		FunfManagerService.getSystemPrefs(manager).edit().putLong(LAST_DATA_UPLOAD, System.currentTimeMillis()).commit();
	}
		
	public Class<? extends DatabaseService> getDatabaseServiceClass() {
		return NameValueDatabaseService.class;
	}
	
	public Class<? extends UploadService> getUploadServiceClass() {
		return HttpsUploadService.class;
	}
}
