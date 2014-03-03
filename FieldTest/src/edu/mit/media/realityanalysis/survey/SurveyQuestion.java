package edu.mit.media.realityanalysis.survey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SurveyQuestion {

	private String mId;
	private int mAnswerType;
	private String mAnswerKey;
	private String mQuestionText;
	private Set<SurveyAnswer> mAnswers;
	
	public SurveyQuestion(String id, int answerType, String answerKey, String questionText, Set<SurveyAnswer> answers) {
		setId(id); 
		setAnswerType(answerType);
		setAnswerKey(answerKey);
		setQuestionText(questionText);
		setAnswers(answers);
	}
	
	public SurveyQuestion(String id, int answerType, String answerKey, String questionText) {
		this(id, answerType, answerKey, questionText, new TreeSet<SurveyAnswer>());
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String mId) {
		this.mId = mId;
	}
	
	public int getAnswerType() {
		return mAnswerType;
	}
	
	public void setAnswerType(int mAnswerType) {
		this.mAnswerType = mAnswerType;
	}
	
	public String getAnswerKey() {
		return mAnswerKey;
	}
	
	public void setAnswerKey(String mAnswerKey) {
		this.mAnswerKey = mAnswerKey;
	}
	
	public String getQuestionText() {
		return mQuestionText;
	}
	
	public void setQuestionText(String mQuestionText) {
		this.mQuestionText = mQuestionText;
	}

	public Set<SurveyAnswer> getAnswers() {
		return mAnswers;
	}

	public void setAnswers(Set<SurveyAnswer> mAnswers) {
		this.mAnswers = mAnswers;
	}
	
	public void addAnswer(SurveyAnswer answer) {
		this.mAnswers.add(answer);
	}
}
