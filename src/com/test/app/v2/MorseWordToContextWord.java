package com.test.app.v2;

import java.util.Map;

/**
 * The class is to store a map entry from morse word to context word. 
 *
 */
public class MorseWordToContextWord implements Map.Entry<String, String> {
	private String morseWord;
	private String contextWord;

	public MorseWordToContextWord(String morseWord, String contextWord) {
		this.morseWord = morseWord;
		this.contextWord = contextWord;
	}

	/**
	 * {@link #getKey()}
	 */
	public String getKey() {
		return morseWord;
	}

	/**
	 * {@link #getValue()}
	 */
	public String getValue() {
		return contextWord;
	}

	/**
	 * {@link #setValue(String)}
	 */
	public String setValue(String contextWord) {
		String oldValue = this.contextWord;
		this.contextWord = contextWord;
		return oldValue;
	}
}
