package edu.mit.media.realityanalysis.survey;

import edu.mit.media.funf.util.StringUtil;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SurveyDatabaseHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_NAME="survey_manager";
	
	// Main Survey Table
	private static final String SURVEY_TABLE = "survey";
	
	private static final String[] SURVEY_COLUMNS = {
		"id TEXT PRIMARY KEY",
		"timestamp INTEGER",
		"values TEXT"
	};

	public SurveyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//String createSurveyTable = String.format("CREATE TABLE %s (%s)", SURVEY_TABLE, StringUtil.join);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
