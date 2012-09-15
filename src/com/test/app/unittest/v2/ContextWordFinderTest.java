package com.test.app.unittest.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.test.app.v2.ContextWordFinder;
import com.test.app.v2.MorseWordAnalyzer;
import com.test.app.v2.MorseWordToContextWord;
import com.test.app.v2.ContextWordTrie;

import junit.framework.TestCase;

public class ContextWordFinderTest extends TestCase {
	public void setUp() {
		try {
			ContextWordFinder.inputMorseTable("c:\\data\\morsecodetable.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testSample() throws Exception {
		System.out.println("----- start testSample -----");
		
		ContextWordFinder finder = new ContextWordFinder();

		finder.inputAll("c:\\data\\morsecode_contextwords_morsewords.txt");
		ContextWordTrie dictionary = finder.getDictionary();
		
		// look up
		List<Set<String>> resultList = dictionary.getBestMatches(finder.getMorseWordList());
		finder.printBestCandidates(resultList);
		System.out.println("----- end testSalesForceSample -----");
		System.out.println();
	}
	
	public void testContextWordToMorseWordConversion() {
		System.out.println("----- Additional tests -----" );
		System.out.println("----- start testContextWordToMorseWordConversion -----");
		
		ContextWordFinder finder = new ContextWordFinder();
		ContextWordTrie wordDictionary = finder.getDictionary();
		
		MorseWordToContextWord mccw = wordDictionary.put("SALE");
		assertTrue(mccw.getKey().equals("....-.-...")); //SALE
		
		mccw = wordDictionary.put("SALES");
		assertTrue(mccw.getKey().equals("....-.-......")); //SALES
		
		mccw = wordDictionary.put("SALESFORCE");
		assertTrue(mccw.getKey().equals("....-.-........-.---.-.-.-..")); //SALESFORCE
		
		mccw = wordDictionary.put("SALESMAN");
		assertTrue(mccw.getKey().equals("....-.-......--.--.")); //SALESMAN
		
		mccw = wordDictionary.put("SALESMEN");
		assertTrue(mccw.getKey().equals("....-.-......--.-.")); //SALESMEN
		
		System.out.println("----- end testContextWordToMorseWordConversion -----");
	}
	
	public void testPerfectMatch() {
		System.out.println("----- start testPerfectMatch (input from Google News) -----");
		
		// text is copied from Google News
		String text = "The statistics tell the story. In seven months, the number of people using the service " +
					  "each month has jumped 26 percent to nearly a quarter of a billion, affirming Skype's status " +
					  "as one of the crown jewels of consumer Internet services. ";
		
		ContextWordFinder finder = new ContextWordFinder();
		ContextWordTrie wordDictionary = finder.getDictionary();
		
		MorseWordAnalyzer analyzer = (MorseWordAnalyzer) wordDictionary.getAnalyzer();
		List<MorseWordToContextWord> mwccList = analyzer.analyzeText(text, ContextWordFinder.MorseCodeMap);
		for (MorseWordToContextWord mwcc : mwccList) {
			wordDictionary.put(mwcc.getKey(), mwcc.getValue());
		}
		
		for (MorseWordToContextWord mwcc : mwccList) {
			Set<String> set = wordDictionary.getBestMatch(mwcc.getKey());
			finder.printBestCandidates(set);
		}
		
		System.out.println("----- end testPerfectMatch (input from Google News) -----");
	}
	
	public void testOnePrefixMatch() throws Exception {
		System.out.println("----- start testOnePrefixMatch -----");
		
		ContextWordFinder finder = new ContextWordFinder();
		
		ContextWordTrie dictionary = finder.getDictionary();

    	dictionary.put(".--.-.----....", "WROTH");
		dictionary.put(".--.....--", "WHAT");
    	dictionary.put("..-.-.-....--.-..-.--.-.", "EARTHQUAKE");
    	
    	// test WROTH
    	Set<String> set = dictionary.getBestMatch(".--.-.----...."); //exact
    	assertTrue(set.contains("WROTH") && set.size() == 1);
    	
    	// test super class get method
    	set = dictionary.get(".--.-.----...."); //exact
    	assertTrue(set.contains("WROTH") && set.size() == 1);
    	
    	set = dictionary.getBestMatch(".--.-.----...");	// truncated
		assertTrue(set.contains("WROTH?") && set.size() == 1);
    	
		set = dictionary.getBestMatch(".--.-.----"); // truncated
    	assertTrue(set.contains("WROTH?") && set.size() == 1);
    	
    	set = dictionary.getBestMatch(".--.-.----.."); // truncated
    	assertTrue(set.contains("WROTH?") && set.size() == 1);
    	
    	set = dictionary.getBestMatch(".--.-.----......"); // extra
    	assertTrue(set.contains("WROTH?") && set.size() == 1);
    	
    	// negative testing. APPLE is not defined.
    	set = dictionary.getBestMatch(".-.--..--..-..."); // exact
    	assertTrue(set.size() == 0);
    	
    	// test WHAT
    	set = dictionary.getBestMatch(".--.....----..-"); // extra
    	assertTrue(set.contains("WHAT?") && set.size() == 1);
    	
    	set = dictionary.getBestMatch(".--....."); // truncated
    	assertTrue(set.contains("WHAT?") && set.size() == 1);
    	
    	// test EARTHQUAKE
    	set = dictionary.getBestMatch("..-.-.-....--.-..-.--.-."); // exact
    	assertTrue(set.contains("EARTHQUAKE") && set.size() == 1);
    	
    	set = dictionary.getBestMatch("..-.-.-....-"); // truncated
    	assertTrue(set.contains("EARTHQUAKE?") && set.size() == 1);
    	
    	set = dictionary.getBestMatch("..-.-.-....--.-..-."); // truncated
    	assertTrue(set.contains("EARTHQUAKE?") && set.size() == 1);
    	
    	set = dictionary.getBestMatch("..-.-.-....--.-..-.--.-.--."); // extra
    	assertTrue(set.contains("EARTHQUAKE?") && set.size() == 1);
    	
		System.out.println("----- end testOnePrefixMatch -----");
	}
	
	public void testShortestPathsToContextWords() {
		System.out.println("----- start testShortestPathsToContextWords -----");
    	// test finding shortest paths down to descendants which have stored values
		ContextWordFinder finder = new ContextWordFinder();
		ContextWordTrie wordDictionary = finder.getDictionary();
		
		wordDictionary.put(".--.....--", "WHAT");
    	wordDictionary.put(".--.....-.", "WHEN");
    	wordDictionary.put(".--......-..", "WHERE");
    	wordDictionary.put(".--....---", "WHO");
    	wordDictionary.put(".--....-----", "WHOM");
    	
    	List<List<String>> pathList = wordDictionary.findShortestPathToWord(wordDictionary);
    	Set<String> resultSet = new HashSet<String>();
    	for (List<String> path : pathList) {
    		StringBuilder sb = new StringBuilder();
    		for (String element : path) {
    			sb.append(element);
    		}
    		resultSet.add(sb.toString());
    	}

    	// Morse words for WHEN, WHO and WHAT are the shortest
    	assertTrue(resultSet.contains(".--.....--") 
    				&& resultSet.contains(".--.....-.") 
    				&& resultSet.contains(".--....---")
    				&& resultSet.size() == 3);
    	
		System.out.println("----- end testShortestPathsToContextWords -----");
	}

	public void testMultiplePrefixMatches() throws Exception {
		System.out.println("----- start testMultiplePrefixMatches -----");
		
		ContextWordFinder finder = new ContextWordFinder();
		ContextWordTrie dictionary = finder.getDictionary();
		
		dictionary.put("IM");	// ..--
		dictionary.put("IN");	// ..-.
		dictionary.put("INEE");	// ..-...
		
		dictionary.put("SALE");
		dictionary.put("SALES");
		dictionary.put("SALESFORCE");
		dictionary.put("SALESMAN");
		dictionary.put("SALESMEN");
		dictionary.put("SALES1");
		dictionary.put("SALES2");	//....-.-........---
		dictionary.put("SALES3");	//....-.-.........--
		dictionary.put("SALES4");	//....-.-..........-
		dictionary.put("SALES5");	//....-.-...........
		
		// length of morse words are the same
    	dictionary.put(".--.-.----....", "WROTH");
    	dictionary.put(".--.-.----....", "WROTIEE");
    	
    	// both morse words and context words length are the same
    	dictionary.put(".-.--..--..-...", "APPLE");
    	dictionary.put(".-.--..--..-...", "CXDTS");

    	// morse word: SALE-
		Set<String> set = dictionary.getBestMatch("....-.-...-");
    	// words matches the longest prefix of the morse
    	assertTrue(set.contains("SALE?") && set.size() == 1);
    	
    	// morse word: SALES
    	set = dictionary.getBestMatch("....-.-......");
    	assertTrue(set.contains("SALES") && set.size() == 1); // exact match
    	
    	// morse word: SALESF
    	set = dictionary.getBestMatch("....-.-........-.");
    	assertTrue(set.contains("SALESFORCE?") && set.size() == 1);
    	
    	// morse word: SALESFORCE---
    	set = dictionary.getBestMatch("....-.-........-.---.-.-.-..---");
    	assertTrue(set.contains("SALESFORCE?") && set.size() == 1);
    	
    	// morse word: SALESM.
    	set = dictionary.getBestMatch("....-.-......--.");
       	// word has fewest extra elements beyond those in morse
    	assertTrue(set.contains("SALESMEN?") && set.size() == 1); 
    	
    	// morse word: SALE..
    	set = dictionary.getBestMatch("....-.-.....");
    	// word has fewest extra elements beyond those in morse
    	assertTrue(set.contains("SALES?") && set.size() == 1); 
    	
    	// ambiguity test. morse word: SALES...
    	set = dictionary.getBestMatch("....-.-.........");
    	// words have fewest extra elements beyond those in morse
    	assertTrue(set.contains("SALES3?") 
    				&& set.contains("SALES4?") 
    				&& set.contains("SALES5?")
    				&& set.size() == 3
    			);

    	// morse word: SALES..
    	set = dictionary.getBestMatch("....-.-........");
       	// words have fewest extra elements beyond those in morse
    	assertTrue(set.contains("SALES2?") 
    				&& set.contains("SALES3?")
    				&& set.contains("SALES4?") 
    				&& set.contains("SALES5?")
    				&& set.size() == 4
    			);

    	set = dictionary.getBestMatch(".--.-.----...");	// truncated
		assertTrue(set.contains("WROTH?") 
					&& set.contains("WROTIEE?") 
					&& set.size() == 2
				);
    	
		set = dictionary.getBestMatch(".--.-.----"); // truncated
    	assertTrue(set.contains("WROTH?") 
					&& set.contains("WROTIEE?") 
					&& set.size() == 2
    			);
    	
    	set = dictionary.getBestMatch(".--.-.----.."); // truncated
    	assertTrue(set.contains("WROTH?") 
				&& set.contains("WROTIEE?") 
				&& set.size() == 2
			);
    	
    	set = dictionary.getBestMatch(".--.-.----......"); // extra
    	assertTrue(set.contains("WROTH?") 
				&& set.contains("WROTIEE?") 
				&& set.size() == 2
			);
    	
    	// test multiple matches with APPLE
    	set = dictionary.getBestMatch(".-.--..--..-..."); // exact
    	assertTrue(set.contains("APPLE!") 
    				&& set.contains("CXDTS!") 
    				&& set.size() == 2
    			);
    	
    	set = dictionary.getBestMatch(".-.--..--..-....-."); // extra 
    	assertTrue(set.contains("APPLE?") 
    				&& set.contains("CXDTS?") 
    				&& set.size() == 2
    			);

    	set = dictionary.getBestMatch(".-.--..--..-.."); // truncated 
    	assertTrue(set.contains("APPLE?") 
    				&& set.contains("CXDTS?") 
    				&& set.size() == 2
    			);
    	
    	// test IM and IN. Both should be returned if an element is truncated
    	set = dictionary.getBestMatch("..-");
		// words have fewest extra elements beyond those in morse
		assertTrue(set.contains("IM?") && set.contains("IN?"));
		
    	// morse word IN-
		set = dictionary.getBestMatch("..-.-");
		
		/*
		 * Words matches the longest prefix of the morse: IN (..-.) and INEE (..-...)
		 * IN has 0 extra element beyond those in morse.
		 */
		assertTrue(set.contains("IN?"));
		System.out.println("----- end testMultiplePrefixMatches -----");
	}
}
