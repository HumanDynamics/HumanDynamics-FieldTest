package edu.mit.media.realityanalysis.fieldtest;

import edu.mit.media.funf.storage.HttpUploadService;
import edu.mit.media.funf.storage.RemoteFileArchive;

import android.content.SharedPreferences;
import android.util.Log;

public class HttpsUploadService extends HttpUploadService {

	@Override
	protected RemoteFileArchive getRemoteArchive(String name) {
		PreferencesWrapper prefs = new PreferencesWrapper(this);
		String access_token = ""; 
		// Getting Access Token
		try {
			//      FileInputStream is = openFileInput("accessToken");
			//      ObjectInputStream ois = new ObjectInputStream(is);
			//      access_token = (String) ois.readObject();
			access_token = prefs.getAccessToken();//.getString(getString(R.string.access_token_prefs_key), "");
			Log.d("UPLOADDATA", "access_token"+access_token);
		} catch(Exception ex) {
		}
		return new HttpsArchive(name, access_token);
	}

}
