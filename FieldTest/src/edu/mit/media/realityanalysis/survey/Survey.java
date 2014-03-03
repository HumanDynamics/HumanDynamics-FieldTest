package edu.mit.media.realityanalysis.survey;

import java.util.HashSet;
import java.util.Set;

public class Survey {
	
	private String mId;
	private String mName;
	private Set<SurveyQuestion> mQuestions;
	
	public Survey(String id, String name, Set<SurveyQuestion> questions) {
		setId(id);
		setName(name);
		setQuestions(questions);
	}
	
	public Survey(String id, String name) {
		this(id, name, new HashSet<SurveyQuestion>());
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String mId) {
		this.mId = mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String mName) {
		this.mName = mName;
	}

	public Set<SurveyQuestion> getQuestions() {
		return mQuestions;
	}

	public void setQuestions(Set<SurveyQuestion> mQuestions) {
		this.mQuestions = mQuestions;
	}	
	
	public void addQuestion(SurveyQuestion question) {
		getQuestions().add(question);
	}
	
	public SurveyQuestion getQuestion(String questionId) {
		// Note: we don't anticipate having many questions, so linear time complexity should be fine
		// If this changes, we can add a mapping between questionId and questions to compensate
		
		for (SurveyQuestion question : getQuestions()) {
			if (question.getId().equals(questionId)) {
				return question;
			}
		}
		
		return null;
	}
}
