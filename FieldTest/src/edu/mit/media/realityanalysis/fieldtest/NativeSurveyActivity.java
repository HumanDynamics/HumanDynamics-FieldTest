package edu.mit.media.realityanalysis.fieldtest;

import java.io.IOException;

import org.json.JSONObject;

import edu.mit.media.realityanalysis.survey.Survey;
import edu.mit.media.realityanalysis.survey.SurveyDropdown;
import edu.mit.media.realityanalysis.survey.SurveyFactory;
import edu.mit.media.realityanalysis.survey.SurveyQuestion;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.LinearLayout;

public class NativeSurveyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native_survey);
		
		new Thread() {
			public void run() {
				RegistryServer registry = new RegistryServer(NativeSurveyActivity.this);
				final JSONObject surveyJson;
				
				try {
					surveyJson = registry.getSurvey("8");					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				runOnUiThread(new Runnable() {
					public void run() {				
						LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.native_survey_layout);
					
						SurveyFactory surveyFactory = new SurveyFactory();
						Survey survey = surveyFactory.getSurvey(surveyJson);
						
						for (SurveyQuestion question : survey.getQuestions()) {
							SurveyDropdown questionDropdown = new SurveyDropdown(NativeSurveyActivity.this);
							questionDropdown.setQuestionText(question.getQuestionText());
							questionDropdown.setAnswers(question.getAnswers());
							surveyLayout.addView(questionDropdown);
						}
					};
				});			

			};
		}.start();
	}
}
