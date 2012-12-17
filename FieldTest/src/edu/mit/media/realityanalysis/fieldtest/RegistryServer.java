package edu.mit.media.realityanalysis.fieldtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class RegistryServer {

	private static final String LOG_TAG = "RegistryServer";
	
	private Context mContext;
	
	public RegistryServer(Context context) {
		mContext = context;
	}
	
	private String getAbsoluteUrl(String relativeUrl, NameValuePair... params) {
		String url = String.format("%s%s?", mContext.getString(R.string.registry_url), relativeUrl);
		
		for (NameValuePair param : params) {
			url += String.format("%s=%s&", param.getName(), param.getValue());
		}
		
		return url;
	}
	
	private String getAbsoluteUrl(int resId, NameValuePair... params) {
		return getAbsoluteUrl(mContext.getString(resId), params);
	}
	
	protected JSONObject getJSON(HttpResponse response) { 

		try {
			BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
			InputStream inputStream = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder stringBuilder = new StringBuilder();
	
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
	
			return new JSONObject(stringBuilder.toString());
		} catch (JSONException jsonException) {
			Log.e(LOG_TAG, "Error parsing response to JSON: " + jsonException.getMessage());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error parsing response: " + e.getMessage());
		}
		
		return null;
	}
	
	/***
	 * Calls the authorization endpoint on the registry server and parses the response as JSON. 
	 * The entire JSON is returned in order to allow consumers to handle response fields and errors as they please
	 * @param username The username of the user logging in
	 * @param password The user's password
	 * @return A JSONObject representing either a successful OAuth2 authorize response or an authorization error, or null if an exception occurred while processing the response
	 * @throws IOException Thrown if an error occurred while contacting the server - this typically means the server is having connectivity issues.
	 */
	public JSONObject authorize(String username, String password) throws IOException {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					
		nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
		nameValuePairs.add(new BasicNameValuePair("client_id", mContext.getString(R.string.client_key)));
		nameValuePairs.add(new BasicNameValuePair("client_secret", mContext.getString(R.string.client_secret)));
		nameValuePairs.add(new BasicNameValuePair("scope", mContext.getString(R.string.client_scope)));
		nameValuePairs.add(new BasicNameValuePair("username", username));
		nameValuePairs.add(new BasicNameValuePair("password", password));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(getAbsoluteUrl(R.string.token_relative_url));
		httppost.addHeader("AUTHORIZATION", mContext.getString(R.string.client_basic_auth));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			return getJSON(response);
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		
		return null;	
	}
	
	public JSONObject getUserInfo(String token) throws IOException {
		String url = getAbsoluteUrl(R.string.userinfo_relative_url, new BasicNameValuePair("bearer_token", token));
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		
		HttpResponse response = httpClient.execute(httpGet);

		return getJSON(response);
	}
	
	
}