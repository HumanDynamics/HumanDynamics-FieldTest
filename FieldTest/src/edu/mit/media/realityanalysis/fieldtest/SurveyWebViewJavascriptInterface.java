package edu.mit.media.realityanalysis.fieldtest;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.webkit.JavascriptInterface;

public class SurveyWebViewJavascriptInterface extends WebViewFragmentJavascriptInterface {

	private Set<String> mQuestionIds;
	private Set<String> mAnsweredQuestionIds;
		
	public SurveyWebViewJavascriptInterface(SurveyActivity activity) {
		super(null, activity);
		mQuestionIds = new HashSet<String>();
		mAnsweredQuestionIds = new HashSet<String>();
	}

	@JavascriptInterface
	public void registerQuestion(String questionId) {
		if (!mQuestionIds.contains(questionId)) {
			mQuestionIds.add(questionId);
		}
	}
	
	@JavascriptInterface
	public void markQuestionAsAnswered(String questionId) {
		if (!mAnsweredQuestionIds.contains(questionId)) {
			mAnsweredQuestionIds.add(questionId);
		}
		
		if (surveyCompleted()) {
			((SurveyActivity)mActivity).finishSurvey();
		}
	}
	
	@JavascriptInterface
	public boolean surveyCompleted() {
		return !mQuestionIds.isEmpty() && mAnsweredQuestionIds.containsAll(mQuestionIds);
	}
	
	@JavascriptInterface
	public String getSurveyQuestionUrl() {
		return "/questions/api/surveyapi/survey_question_answer_link/";
	}
	
}
