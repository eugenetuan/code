package com.test.app.v2;

/**
 * Matching attribute is defined in order to know the 
 * look-up status for a given morse word.
 * 
 */
public class LookupStatus {
	private boolean perfectMatch;

	public LookupStatus() {
	}

	public void setPerfectMatch(boolean perfectMatch) {
		this.perfectMatch = perfectMatch;
	}
	
	public boolean isPerfectMatch() {
		return perfectMatch;
	}
}
