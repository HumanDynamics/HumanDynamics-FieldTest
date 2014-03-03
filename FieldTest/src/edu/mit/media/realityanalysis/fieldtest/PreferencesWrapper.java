package edu.mit.media.realityanalysis.fieldtest;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesWrapper {
	private static final String ACCESS_TOKEN_KEY = "accessToken";
	private static final String PDS_LOCATION_KEY = "pds_location";
	private static final String UUID_KEY = "uuid";
	private static final String PREFS_FILE = "TokenPrefs";
	private static final String REFRESH_TOKEN_KEY = "refreshToken";
	private static final String EXPIRATION_TIME_KEY = "tokenExpirationTime";

	protected SharedPreferences mPreferences;
	protected Context mContext;
	
	public PreferencesWrapper(Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
	}
	
	public String getAccessToken() {
		return mPreferences.getString(ACCESS_TOKEN_KEY, null);
	}
	
	public boolean setAccessToken(String accessToken) {
		return mPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken).commit();
	}
		
	public String getPDSLocation() {
		// NOTE: Using IP addresses for the PDS locations was a mistake... we know where they live. Let's just return that. 
		return "http://working-title.media.mit.edu:8004"; //mPreferences.getString("pds_location", null);
	}
	
	public boolean setPDSLocation(String accessToken) {
		return mPreferences.edit().putString(PDS_LOCATION_KEY, accessToken).commit();
	}
	
	public String getUUID() {
		return mPreferences.getString(UUID_KEY, null);
	}
	
	public boolean setUUID(String accessToken) {
		return mPreferences.edit().putString(UUID_KEY, accessToken).commit();
	}
	
	public String getRefreshToken() {
		return mPreferences.getString(REFRESH_TOKEN_KEY, null);
	}
	
	public boolean setRefreshToken(String refreshToken) {
		return mPreferences.edit().putString(REFRESH_TOKEN_KEY, refreshToken).commit();
	}
	
	public long getTokenExpirationTime() {
		return mPreferences.getLong(EXPIRATION_TIME_KEY, 0);
	}
	
	public boolean setTokenExpirationTime(long tokenExpirationTime) {
		return mPreferences.edit().putLong(EXPIRATION_TIME_KEY, tokenExpirationTime).commit();
	}
	
	public Set<String> getPendingSurveys() {
		// NOTE: we're returning a copy here, rather than the original
		// This is so that modifications to the set will be saved properly if we store them in the same preferences key.
		return new HashSet<String>(mPreferences.getStringSet("surveys", new HashSet<String>()));
	}
	
	public void addPendingSurvey(String survey) {
		Editor editor = mPreferences.edit();
		Set<String> pendingSurveys = getPendingSurveys();
		
		pendingSurveys.remove(survey);
		pendingSurveys.add(survey);
	
		editor.putStringSet("surveys", pendingSurveys);
		
		editor.commit();
	}
	
	public void removePendingSurvey(String survey) {
		Editor editor = mPreferences.edit();
		
		Set<String> pendingSurveys = getPendingSurveys();
		
		if (pendingSurveys == null) {
			return;
		}
		
		pendingSurveys.remove(survey);
		
		editor.putStringSet("surveys", pendingSurveys);
		editor.commit();
	}
}
