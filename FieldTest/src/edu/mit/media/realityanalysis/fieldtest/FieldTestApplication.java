package edu.mit.media.realityanalysis.fieldtest;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.EmailIntentSender;

import android.app.Application;

@ReportsCrashes(formUri="http://working-title.media.mit.edu:8003/bug_report", formKey="")
public class FieldTestApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		//ACRA.getErrorReporter().setReportSender(new EmailIntentSender(this));
	}
}
