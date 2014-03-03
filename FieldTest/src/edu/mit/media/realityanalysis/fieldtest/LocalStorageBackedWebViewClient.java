package edu.mit.media.realityanalysis.fieldtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.DownloadManager.Query;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A WebViewClient that attempts to load static files from assets included with the application or local cache files.
 * For whitelisted API calls and html pages, the files are pulled from the network only as needed - 30 minutes for answerlists, less for static HTML
 * 
 * 
 * @author BS
 *
 */
public class LocalStorageBackedWebViewClient extends WebViewClient {
	
	private Context mContext;
	private Set<String> answerKeyWhitelist;
	private Set<String> mAssetBackedPages;
	
	//private static HashMap<String, WebResourceResponse> RESPONSE_CACHE = new HashMap<String, WebResourceResponse>();
	
	public LocalStorageBackedWebViewClient(Context context) {
		mContext = context;
		answerKeyWhitelist = new HashSet<String>();
		mAssetBackedPages = new HashSet<String>();
		Collections.addAll(answerKeyWhitelist, "socialhealth", "RecentActivityByHour", "RecentSocialByHour", "RecentFocusByHour");
		Collections.addAll(mAssetBackedPages, "socialHealthRadial", "social", "activity", "focus", "survey");
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		//String path = null;
		String mimeType = null;
		WebResourceResponse response = null;
		Uri uri = Uri.parse(url);

		String urlPath = uri.getPath();
		String filename = uri.getLastPathSegment();
		boolean updateInBackground = false;
		long expirationTime = 1000 * 60 * 30;		
		
		if (mAssetBackedPages.contains(filename) || filename.endsWith(".js") || filename.endsWith(".css") || filename.endsWith(".png")) {
			return new AssetBackedWebViewClient(mContext).shouldInterceptRequest(view,  url);
		}
		
		if (filename.contains(".js")) {
			mimeType = "application/javascript";
		} else if (filename.contains(".css")) {
			mimeType = "text/css";
		} else if (filename.contains(".png")) {
			mimeType = "img/png";
		} else if (urlPath.contains("/api/")) {
			mimeType = "application/json";
			if (urlPath.contains("answer") && uri.getQueryParameter("key") != null && answerKeyWhitelist.contains(uri.getQueryParameter("key"))) {
				filename = uri.getQueryParameter("key") + ".json";
			} else if (urlPath.contains("surveyapi") && uri.getQueryParameter("survey") != null) {
				// This is a survey, which doesn't change often - set a longer expirationTime (1 day, in this case)
				expirationTime = 1000 * 60 * 60 * 24;
				filename = "survey_" + uri.getQueryParameter("survey") + ".json";
				String registryUrl = String.format("%s%s?%s", mContext.getString(R.string.registry_url), urlPath, uri.getQuery());
				uri = Uri.parse(registryUrl);
			} else {
				// If it's not a static survey, and it's not social health metrics, don't intercept the request
				// This is necessary for any URL we might need to post to from the application (survey answers, for example)
				return super.shouldInterceptRequest(view, url);
			}
		} else  if (urlPath.contains(".ico")) {
			return super.shouldInterceptRequest(view, url);
		} else {		
			// Else, try html
			mimeType = "text/html";
			filename = filename + ".html";
			// Static files only expire every day... Removes some of the flexibility that hosting things in webviews provided us, but decreases load times. 
			//expirationTime = 24 * 60 * 60 * 1000;
			updateInBackground = true;
		} 
		
		final File file = mContext.getFileStreamPath(filename);
		if (!file.exists() || file.lastModified() < (new Date()).getTime() - expirationTime) {
			if (file.exists() && updateInBackground) {
				// If the file exists and isn't likely to have changed, we don't need to wait on the update to return the contents
				final Uri localUri = uri;
				final long localExpirationTime = expirationTime;
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						updateLocalFile(file, localUri, localExpirationTime);
						return null;
					}
				}.execute();
			} else {
				updateLocalFile(file, uri, expirationTime);
			}
		}
		
		try {
			InputStream fileStream = mContext.openFileInput(filename);

			response = new WebResourceResponse(mimeType, "UTF-8", fileStream);
		} catch (Exception e) {
			// Local asset doesn't exist - fall through to standard behavior
			return super.shouldInterceptRequest(view, url);
		}
		
		return (response == null)? super.shouldInterceptRequest(view, url) : response;
	}
	
	private void updateLocalFile(File file, Uri uri, long expirationTime) {
		HttpGet get = new HttpGet(uri.toString());
		get.addHeader("Content-Type", "application/json");
		HttpClient client = new DefaultHttpClient();
		
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		    public String handleResponse(final HttpResponse response) throws HttpResponseException, IOException {
		        StatusLine statusLine = response.getStatusLine();
		        if (statusLine.getStatusCode() >= 300) {
		            throw new HttpResponseException(statusLine.getStatusCode(),
		                    statusLine.getReasonPhrase());
		        }
                HttpEntity entity = response.getEntity();
	            return entity == null ? null : EntityUtils.toString(entity, "UTF-8");
		    }
		};
		try {
			String content = client.execute(get, responseHandler);
			FileOutputStream outStream = mContext.openFileOutput(file.getName(), Context.MODE_PRIVATE);
			outStream.write(content.getBytes());
			outStream.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
