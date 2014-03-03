package edu.mit.media.realityanalysis.survey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class SurveyFactory {

	public SurveyFactory() {}
	
	public Survey getSurvey(JSONObject surveyJson) {
		JSONArray objectsArray = surveyJson.optJSONArray("objects");

		if (objectsArray == null || objectsArray.length() == 0) {
			return null;
		}	
		
		String surveyId;
		String surveyName; 
		
		try {
			JSONObject firstObject = objectsArray.getJSONObject(0);
			JSONObject survey = firstObject.getJSONObject("survey");
			surveyId = survey.getString("id");
			surveyName = survey.getString("name");
		} catch (JSONException e) {
			// if we're here, the JSON is not formatted as we expected... might make sense to log this
			return null;
		}
		
		Survey survey = new Survey(surveyId, surveyName);
		
		for (int i = 0; i < objectsArray.length(); i++) {
			JSONObject surveyQuestionAnswerLink = objectsArray.optJSONObject(i);
			JSONObject surveyQuestionJson = surveyQuestionAnswerLink.optJSONObject("survey_question");
			String surveyQuestionId = surveyQuestionJson.optString("id");
			SurveyQuestion surveyQuestion = survey.getQuestion(surveyQuestionId);
			
			if (surveyQuestion == null) {			
				String surveyQuestionAnswerKey = surveyQuestionJson.optString("answer_key");
				String surveyQuestionText = surveyQuestionJson.optString("question");
				int surveyQuestionAnswerType = surveyQuestionJson.optInt("answer_type");
				
				surveyQuestion = new SurveyQuestion(surveyQuestionId, surveyQuestionAnswerType, surveyQuestionAnswerKey, surveyQuestionText);
				survey.addQuestion(surveyQuestion);
			}

			JSONObject surveyAnswerJson = surveyQuestionAnswerLink.optJSONObject("survey_answer");
			String surveyAnswerId = surveyAnswerJson.optString("id");
			int value = surveyAnswerJson.optInt("value");
			String surveyAnswerDescription = surveyAnswerJson.optString("description");
			
			SurveyAnswer surveyAnswer = new SurveyAnswer(surveyAnswerId, value, surveyAnswerDescription);
			surveyQuestion.addAnswer(surveyAnswer);				
		}
		
		return survey;
	}
	
}
