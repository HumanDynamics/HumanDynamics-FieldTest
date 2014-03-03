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
public class AssetBackedWebViewClient extends WebViewClient {
	
	private Context mContext;
	
	public AssetBackedWebViewClient(Context context) {
		mContext = context;
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		String path = null;
		String mimeType = null;
		WebResourceResponse response = null;
		Uri uri = Uri.parse(url);

		String urlPath = uri.getPath();
		String filename = uri.getLastPathSegment();
		
		if (filename.contains(".js")) {
			path = "web/js";
			mimeType = "application/javascript";
		} else if (filename.contains(".css")) {
			path = "web/css";
			mimeType = "text/css";
		} else if (filename.contains(".png")) {
			path = "web/img";
			mimeType = "img/png";
		} else  if (urlPath.contains(".ico")) {
			return super.shouldInterceptRequest(view, url);
		} else {		
			// Else, try html
			path = "web";
			mimeType = "text/html";
			filename = filename + ".html";
			// Static files only expire every day... Removes some of the flexibility that hosting things in webviews provided us, but decreases load times. 
			//expirationTime = 24 * 60 * 60 * 1000;
		} 

		if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(mimeType)) {
			try {
				InputStream fileStream = mContext.getAssets().open(path + "/" + filename);
				response = new WebResourceResponse(mimeType, "UTF-8", fileStream);
			} catch (Exception e) {
				// Local asset doesn't exist - fall through to standard behavior
				return super.shouldInterceptRequest(view, url);
			}
		}
		return (response == null)? super.shouldInterceptRequest(view, url) : response;
	}
}
