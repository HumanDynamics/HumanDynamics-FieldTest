package edu.mit.media.realityanalysis.survey;

public class SurveyAnswer implements Comparable<SurveyAnswer> {	
	
	private String mId;
	private int mValue;
	private String mDescription;
	
	public SurveyAnswer(String id, int value, String description) {
		setId(id);
		setValue(value);
		setDescription(description);
	}

	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public int getValue() {
		return mValue;
	}

	public void setValue(int mValue) {
		this.mValue = mValue;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}

	@Override
	public int compareTo(SurveyAnswer another) {
		return getValue() - another.getValue();
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
