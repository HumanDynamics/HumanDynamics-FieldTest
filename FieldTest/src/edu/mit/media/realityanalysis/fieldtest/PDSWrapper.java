package edu.mit.media.realityanalysis.fieldtest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.funf.util.IOUtil;
import edu.mit.media.funf.util.LogUtil;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PDSWrapper {

	private Context mContext; 
	private PreferencesWrapper mPrefs;
	
	public PDSWrapper(Context context) throws Exception {
		mContext = context;
		mPrefs = new PreferencesWrapper(context);
		if (mPrefs.getAccessToken() == null && mPrefs.getPDSLocation() == null && mPrefs.getUUID() == null) {
			throw new Exception("SharedPreferences do not contain the necessary entries to construct a PDS");
		}
	}	
	
	public String buildAbsoluteUrl(String relativeUrl) {
		String cleanedUrl = relativeUrl.replace("pds:/", "");
		String separator = relativeUrl.contains("?")? "&" : "?";
		
		return String.format("%s%s%sbearer_token=%s&datastore_owner=%s", mPrefs.getPDSLocation(), cleanedUrl, separator, mPrefs.getAccessToken(), mPrefs.getUUID());
	}
	
	public String buildAbsoluteUrl(int resId) {
		return buildAbsoluteUrl(mContext.getString(resId));
	}
	
	public String buildAbsoluteApiUrl(String relativeUrl) {
		return String.format("%s%s?bearer_token=%s&datastore_owner__uuid=%s", mPrefs.getPDSLocation(), relativeUrl, mPrefs.getAccessToken(), mPrefs.getUUID());
	}
	
	private String getNotificationApiUrl() {
		return buildAbsoluteApiUrl(mContext.getString(R.string.notification_api_relative_url));
	}
	
	public Boolean savePipelineConfig(String name, Pipeline pipeline) {
		String resourceUrl = buildAbsoluteApiUrl("/api/personal_data/funfconfig/");
		JsonArray pipelinesJsonArray = getPipelinesJsonArray();
		JsonObject pipelineJsonObject = null;
		Boolean exists = false;
		Gson gson = FunfManager.getGsonBuilder(mContext).create();
		
		if (pipelinesJsonArray != null) {
			for (JsonElement pipelineJsonElement : pipelinesJsonArray) { 
				pipelineJsonObject = pipelineJsonElement.getAsJsonObject(); 
				if (pipelineJsonObject.has("name") && pipelineJsonObject.get("name").getAsString().equals(name)) {
					if (pipelineJsonObject.has("config")) {
						pipelineJsonObject.remove("config");
					}
					pipelineJsonObject.add("config", gson.toJsonTree(pipeline));
					resourceUrl = buildAbsoluteApiUrl(pipelineJsonObject.get("resource_uri").getAsString());
					exists = true;
				}				
			}
		}
		
		if (pipelineJsonObject == null) {
			pipelineJsonObject = new JsonObject();			 
			pipelineJsonObject.addProperty("name", name);
			pipelineJsonObject.add("config", gson.toJsonTree(pipeline));
		}
		
		HttpEntityEnclosingRequestBase savePipelineRequest = (exists)? new HttpPut(resourceUrl) : new HttpPost(resourceUrl);

		return postOrPut(savePipelineRequest, pipelineJsonObject.toString());		
	}
	
	/**
	 * Register this device with GCM on the PDS
	 * NOTE: this method blocks on server communication - DO NOT RUN IN THE UI THREAD!
	 * @return true if registration was successful, false otherwise
	 */
	public boolean registerGCMDevice(String regId) {	
		JsonObject deviceJsonObject = new JsonObject();
		JsonObject datastoreOwner = new JsonObject();
		datastoreOwner.addProperty("uuid", mPrefs.getUUID());
		deviceJsonObject.add("datastore_owner", datastoreOwner);
		deviceJsonObject.addProperty("gcm_reg_id", regId);
		
		String deviceUrl = buildAbsoluteApiUrl(mContext.getString(R.string.device_api_relative_url));
		
		HttpPost deviceRequest = new HttpPost(deviceUrl);
		
		if (postOrPut(deviceRequest, deviceJsonObject.toString())) {
			GCMRegistrar.setRegisteredOnServer(mContext, true);
			return true;
		}	
		// If we got this far, and regId is not empty, we succeeded
		return false;
	}
	
	
	private Boolean postOrPut(HttpEntityEnclosingRequestBase request, String data) {
		HttpResponse response = null;
		
		try {		
			StringEntity contentEntity = new StringEntity(data);
			request.setEntity(contentEntity);
			
			request.addHeader("Content-Type", "application/json");
			
			HttpClient client = new DefaultHttpClient();
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			Log.w(LogUtil.TAG, "Error error posting or putting to PDS.");
			return false;
		} catch (IOException e) {
			Log.w(LogUtil.TAG, "IO Exception posting or putting to PDS.");
			return false;
		} catch (Exception e) {
			Log.w(LogUtil.TAG, "Generic Exception posting or putting to PDS.", e);
			return false;
		}
		
		if 	(response != null &&
			(response.getStatusLine().getStatusCode() == 204 || response.getStatusLine().getStatusCode() == 201)) {
			return true;
		}
		
		if (response != null) {
			try {
				String responseContent = IOUtil.inputStreamToString(response.getEntity().getContent(), Charset.defaultCharset().name());
				Log.w(LogUtil.TAG, responseContent);
			} catch (IllegalStateException e) {
				Log.w(LogUtil.TAG, "IO Exception posting or putting to PDS.");				
			} catch (IOException e) {
				Log.w(LogUtil.TAG, "IO Exception posting or putting to PDS.");
			} catch (Exception e) {
				Log.w(LogUtil.TAG, "Generic Exception posting or putting to PDS.", e);
			}
			
		}
		
		return false;
	}

	public Map<String, Pipeline> getPipelines() { 
		Map<String, Pipeline> pipelines = new HashMap<String, Pipeline>();
		JsonArray pipelinesJsonArray = getPipelinesJsonArray();		
		
		if (pipelinesJsonArray != null) {
			Gson gson = FunfManager.getGsonBuilder(mContext).create();
			for (JsonElement pipelineJsonElement : getPipelinesJsonArray()) {
				try {
					JsonObject pipelineJsonObject = pipelineJsonElement.getAsJsonObject();
					if (pipelineJsonObject.has("name") && pipelineJsonObject.has("config")) {
						Pipeline pipeline = gson.fromJson(pipelineJsonObject.get("config"), MainPipelineV4.class);
						pipelines.put(pipelineJsonObject.get("name").getAsString(), pipeline);
					}
				} catch (Exception e) {
					Log.w(LogUtil.TAG, "Error creating pipelines from PDS configs", e);
				}
			}
		}
		
		return pipelines;
	}
	
	protected JsonArray getPipelinesJsonArray() {
		HttpGet getPipelinesRequest = new HttpGet(buildAbsoluteApiUrl("/api/personal_data/funfconfig/"));
		getPipelinesRequest.addHeader("Content-Type", "application/json");		
		HttpClient client = new DefaultHttpClient();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		JsonParser parser = new JsonParser();
		
		try {
			responseBody = client.execute(getPipelinesRequest, responseHandler);
		} catch (ClientProtocolException e) {
	        client.getConnectionManager().shutdown();  
			return new JsonArray();
		} catch (IOException e) {
	        client.getConnectionManager().shutdown();  
			return new JsonArray();
		}
		
		try {			
			JsonObject pipelinesBody = parser.parse(responseBody).getAsJsonObject();
			
			if (pipelinesBody.has("objects")) {
				return pipelinesBody.getAsJsonArray("objects");
			}
		} catch (Exception e) {
			Log.w(LogUtil.TAG, "Error parsing pipeline updates");			
		}
		
		return new JsonArray();
	}
	
	public Map<Integer, Notification> getNotifications() {
		HttpGet getNotificationsRequest = new HttpGet(getNotificationApiUrl());
		getNotificationsRequest.addHeader("Content-Type", "application/json");
		
		HttpClient client = new DefaultHttpClient();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		
		try {
			responseBody = client.execute(getNotificationsRequest, responseHandler);
		} catch (ClientProtocolException e) {
	        client.getConnectionManager().shutdown();  
			return null;
		} catch (IOException e) {
	        client.getConnectionManager().shutdown();  
			return null;
		}
		
		Map<Integer, Notification> notifications = new HashMap<Integer, Notification>();
		ArrayList<String> notificationsToDelete = new ArrayList<String>();
		
		try {
			JSONObject notificationsBody = new  JSONObject(responseBody);
			JSONArray notificationsArray = notificationsBody.getJSONArray("objects");
			
			for (int i = 0; i < notificationsArray.length(); i++) {
				JSONObject notification = notificationsArray.optJSONObject(i);
				
				if (notification != null) {
					NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
					builder.setContentTitle(notification.getString("title")).setContentText(notification.getString("content")).setSmallIcon(R.drawable.ic_launcher);
					
					String uri = notification.optString("uri");
					
					if (notification.getInt("type") == 2 && uri != null && uri.length() > 0) {
						mPrefs.addPendingSurvey(uri);
						Intent pdsIntent = new Intent(mContext, SurveyActivity.class);
						pdsIntent.setData(Uri.parse(uri));
						PendingIntent pdsPendingIntent = PendingIntent.getActivity(mContext, 0, pdsIntent, PendingIntent.FLAG_CANCEL_CURRENT);						
						builder.setContentIntent(pdsPendingIntent);
						
					}
					notifications.put(notification.getInt("type"), builder.build());
					
					if (notification.getInt("type") > 0) {
						notificationsToDelete.add(notification.getString("resource_uri"));
					}
				}
			}			
		} catch (JSONException e) {
			return null;
		}
		
		for (String uriToDelete : notificationsToDelete) {		
			// if we've gotten this far, we've successfully parsed all of the notifications, so clear the list on the server
			HttpDelete deleteNotificationsRequest = new HttpDelete(buildAbsoluteApiUrl(uriToDelete));
			
			try {
				// We don't care about the response here - if it succeeds, then no exception is thrown and the response has no content
				client.execute(deleteNotificationsRequest);
			} catch (ClientProtocolException e) {
				// Log something here
			} catch (IOException e) {
				// Log something here
			}
		}
		
		client.getConnectionManager().shutdown();  
				
		return notifications;
	}
}
