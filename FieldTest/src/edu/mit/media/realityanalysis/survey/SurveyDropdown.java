package edu.mit.media.realityanalysis.survey;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.mit.media.realityanalysis.fieldtest.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SurveyDropdown extends LinearLayout {

	private TextView mQuestionText;
	private Spinner mQuestionAnswerDropdown;
	private Context mContext;
	
	public SurveyDropdown(Context context) {
		super(context);
		mContext = context;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		inflater.inflate(R.layout.survey_dropdown, this);
		
		mQuestionText = (TextView) findViewById(R.id.question_text);
		mQuestionAnswerDropdown = (Spinner) findViewById(R.id.question_answers);		
	}
	
	public void setQuestionText(String question) {		
		mQuestionText.setText(question);
	}
	
	public void setAnswers(Set<SurveyAnswer> answers) {
		ArrayAdapter<SurveyAnswer> adapter = new ArrayAdapter(mContext, R.layout.survey_dropdown_item);
		for (SurveyAnswer answer : answers) {
			adapter.add(answer);
		}
		mQuestionAnswerDropdown.setAdapter(adapter);
	}
}
