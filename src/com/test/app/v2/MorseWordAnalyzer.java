package com.test.app.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.test.util.trie.v2.KeyAnalyzer;

/**
 * Analyze morse word or context word, and convert it to a form, such as list of morse characters,
 * which can be later used as the keys in the trie. 
 *
 */
public class MorseWordAnalyzer extends KeyAnalyzer<String> {
	/**
	 * Analyze the morse word and convert into morse characters.
	 * 
	 * @param morseWord Morse word
	 * @return List of morse characters
	 */
    public List<String> analyze(String morseWord) {
        ArrayList<String> list = new ArrayList<String>();
        
        // convert string to char list
        for (int i = 0; i < morseWord.length(); i++) {
        	String ch = Character.toString(morseWord.charAt(i));
        	list.add(ch);
        }
        return list;
    }
    
    /**
     * Convert word from context to morse word and put both into MorseWordToContextWord object.
     * 
     * @param contextWord Word from context
     * @param morseCodeMap More code table
     * @return MorseWordToContextWord map entry
     */
    public MorseWordToContextWord analyze(String contextWord, Map<String, String> morseCodeMap) {
		StringBuilder morseWordBuf = new StringBuilder();
		StringBuilder contextWordBuffer = new StringBuilder();
		
		// in case context word is in lower case
		contextWord = contextWord.toUpperCase();
		for (int i = 0; i < contextWord.length(); i++) {
			char ch = contextWord.charAt(i);
			contextWordBuffer.append(Character.toString(ch));
			String code = morseCodeMap.get(Character.toString(ch));
			
			// If no such code, skip it.
			if (code != null) {
				morseWordBuf.append(code);
			}
		}
		
		return new MorseWordToContextWord(morseWordBuf.toString(), contextWordBuffer.toString());
    }
    
    /**
     * Convert text to morse word list. The text can be a list of words such as 
     * sentences or paragraphs.
     * 
     * @param text Text containing context words
     * @param morseCodeMap More code table
     * @return List of MorseWordToContextWord entries
     */
    public List<MorseWordToContextWord> analyzeText(String text, Map<String, String> morseCodeMap) {
    	List<MorseWordToContextWord> mwccList = new ArrayList<MorseWordToContextWord>();
		
		// in case it is not in upper case
		text = text.toUpperCase();
		StringTokenizer st = new StringTokenizer(text, " \r\n\'\"-_.,");
		
		while (st.hasMoreTokens()) {
			String contextWord = st.nextToken();
			mwccList.add(analyze(contextWord, morseCodeMap));
		}
		
		return mwccList;
    }
}
