package com.test.app.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Find context words, given Morse code (morse) and morse words as input.
 * 
 * Problem Analysis:
 * 
 * 	The problem is similar to text match or auto-complete problem. Auto-complete involves the program 
 * 	predicting a word or phrase that the user wants to type in without the user actually typing it in completely.
 * 
 * 	Morse word matching conditions:
 * 			
 * 	1. Exact match
 * 	2. Extra morse elements
 *	3. Truncated morse word 
 *		ex., IM = ..--, IN = ..-.
 *		morse: ..- 
 *			
 * 	4. Both 2 and 3 meet
 * 	   
 * 		Assuming "SALES" and "SALESFORCE" are context words, and the morse input is "....-.-........-.---"
 * 		(ie., "SALESFO"). On one hand, it's a truncated word of "SALESFORCE". On the other hand, it has extra 
 * 		elements to "SALES". In this situation, choose the context word "SALESFORCE" that matches the longest 
 * 		prefix of morse. 
 * 		
 * Algorithm/Data Structure:
 * 
 * Build up trie (prefix tree) by storing morse words and context words to the trie and providing look-up methods.
 * 
 *  Trie: http://en.wikipedia.org/wiki/Trie
 *  
 * 	A trie, or prefix tree, is an ordered tree data structure that is used to store an associative array 
 * 	where the keys are usually strings. It's position in the tree defines the key with which it is associated. 
 * 	All the descendants of a node have a common prefix of the string associated with that node, and the root 
 * 	is associated with the empty string. Values are normally not associated with every node, only with leaves and 
 *	some inner nodes that correspond to keys of interest. All the descendants of a node have a common prefix of the 
 * 	string associated with that node, and the root is associated with the empty string.
 *  
 * 	1. Use Map and Trie to store Morse code tables, morse words and context words. 
 * 
 * 		1). build up Morse code look-up table.
 * 		2). build up morse word look-up trie.
 * 
 * 	2. Look-up:
 * 
 * 		1) Scan forward: If a given morse word cannot be found, extend the prefix search by traversing 
 * 		   downward to the nearest descendants in the tree which have context words.
 *  
 * 		2) Scan backward: If forward looking doesn't return context word, perform backward looking. 
 * 		   Get context word stored in the nearest ancestor node. 
 *    
 * 		3) LookUpStatus object is storing the matching attributes, such as whether it is perfect match or not.
 * 
 * 		4) The display functionality is implemented in the main program ContextWordFinder.java.
 * 
 * 	3. Tests:
 * 		
 * 		1) Test cases are organized into ContextWordFinderTest.java. It can run under Junit test framework.
 * 		2) ContextWordFinder main() method test against sample data and data entered from console.
 * 
 * @author Eugene Tuan
 */
public class ContextWordFinder {
	private static String Delimiters = " \t";
	private static String TabDelimiter = "\t";
	public static Map<String, String> MorseCodeMap = new HashMap<String, String>();
	
	private ContextWordTrie wordDictionary = new ContextWordTrie(new MorseWordAnalyzer());
	private List<String> morseWordList = new ArrayList<String>();

	public static void inputMorseTable(String path) throws Exception {
		if (MorseCodeMap.size() != 0) {
			return;
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		String line = null;
        
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(TabDelimiter);
			if (tokens != null) {
				// build up look-up table
				MorseCodeMap.put(tokens[0], tokens[1]); // character to code map
			}
		}
		
		reader.close();
	}
	
	public void inputContextWords(String path) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		String contextWord = null;
        
		while ((contextWord = reader.readLine()) != null) {
			wordDictionary.put(contextWord);
		}
		reader.close();
	}
	
	public void inputMorseWords(String path) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		String line = null;
        
		while ((line = reader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, Delimiters);
			while (st.hasMoreTokens()) {
				morseWordList.add(st.nextToken());
			}
		}
		reader.close();
	}
	
	public void inputAll(String morseContxtWordPath) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(new File(morseContxtWordPath)));
		String line = null;

		while ((line = reader.readLine()) != null && line.trim().equals("*") == false) {
			StringTokenizer st = new StringTokenizer(line, Delimiters);
			MorseCodeMap.put(st.nextToken(), st.nextToken());
		}
		
		while ((line = reader.readLine()) != null && line.trim().equals("*") == false) {
			wordDictionary.put(line.trim());
		}
		
		while ((line = reader.readLine()) != null && line.trim().equals("*") == false) {
			StringTokenizer st = new StringTokenizer(line, Delimiters);
			while (st.hasMoreTokens()) {
				morseWordList.add(st.nextToken());
			}
		}
	}
	
	public ContextWordTrie getDictionary() {
		return wordDictionary;
	}
	
	public List<String> getMorseWordList() {
		return morseWordList;
	}
	
	public void printBestCandidates(List<Set<String>> resultList) {
		for (Set<String> resultSet : resultList) {
			printBestCandidates(resultSet);
		}
	}
	
	public void printBestCandidates(Set<String> resultSet) {
		for (String candidateWord : resultSet) {
			System.out.println(candidateWord);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ContextWordFinder finder = new ContextWordFinder();
		finder.inputAll("c:\\data\\morsecode_contextwords_morsewords.txt");
		ContextWordTrie dictionary = finder.getDictionary();
		
		// look up
		List<Set<String>> resultList = dictionary.getBestMatches(finder.getMorseWordList());
		finder.printBestCandidates(resultList);
		
		// additional tests
        System.out.println("------------- Additional tests -------------" );
        
		BufferedReader reader = null;
        String morseWord = null;
                
        try {
        	reader = new BufferedReader(new InputStreamReader(System.in));
        	System.out.print("Enter morse word: " );
        	
        	while ((morseWord = reader.readLine()) != null) {
        		Set<String> resultSet = dictionary.getBestMatch(morseWord);
        		finder.printBestCandidates(resultSet);
        		
        		System.out.println();
                System.out.print("Enter morse word: " );
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	if (reader != null) {
        		reader.close();
        	}
        }
	}
}
