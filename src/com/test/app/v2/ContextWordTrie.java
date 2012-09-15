package com.test.app.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.test.util.trie.v2.KeyAnalyzer;
import com.test.util.trie.v2.Trie;

/**
 * Put/get context word(s) to/from trie, given a morse word.
 * 
 */
public class ContextWordTrie extends Trie<String, String> {
	private static String PerfectMatchAmbiguityIndicator = "!";
	private static String ImperfectMatchSuffixIndicator = "?";
	
	private static Map<String, String> MorseCodeMap = ContextWordFinder.MorseCodeMap;
	
	public ContextWordTrie(KeyAnalyzer<String> analyzer) {
		super(analyzer);
	}

	public ContextWordTrie(KeyAnalyzer<String> analyzer, String morseChar) {
		super(analyzer, morseChar);
	}

	/**
	 * {@link #createTrie(String)}
	 */
	public Trie<String, String> createTrie(String morseChar) {
		Trie<String, String> trie = new ContextWordTrie(analyzer, morseChar);
		trie.setValueSet(new HashSet<String>());
		return trie;
	}

    /**
     * Convert context word to morse word and put the key and value pair into trie
     * 
     * @param contextWord Context word to be put into trie
     * @return mccw MorseWordToContextWord object
     */
    public MorseWordToContextWord put(String contextWord) {
    	MorseWordToContextWord mccw = ((MorseWordAnalyzer) analyzer).analyze(contextWord, MorseCodeMap);
    	put(mccw.getKey(), mccw.getValue());
    	return mccw;
    }
    
    public void put(String morseWord, String contextWord) {
        super.put(morseWord, contextWord);
    }
    
    /**
     * Given a trie, find all shortest traversal paths down to the descendants which have context words.
     * 
     * A path consists of a list of descendant keys by the order in which the nodes are visited.
     * The last key in the ordered list is the only descendant in the list that has the stored 
     * context word(s).
     * 
     *  Ex., The following is a tree with root A. 
     *  
     *  Case 1: Assuming only K, J, L, N have values stored in nodes
     *  
     *  	A->B->C->D->K
     *  	   	->E->G->J
     *  		->F->L
     *  		->M->N
     *  
     *  If the traversal starts from B, all possible paths are CDK, EGJ, FL, and MN. 
     *  But the shortest paths are only FL and MN. 
     *  
     *  Case 2: Beyond K, J, L and N, C has context word.
     *  
     *  The shortest path is only C.
     * 
     * @param trie Trie object is the starting node in finding the paths
     */
    public List<List<String>> findShortestPathToWord(Trie<String, String> trie) {
    	List<List<String>> pathList = new ArrayList<List<String>>();
    	int minSeqLen = Integer.MAX_VALUE;
    	
    	while (true) {
    		List<String> path = new ArrayList<String>();
    		findOnePathToWord(trie, path, true);
    		
    		if (path.isEmpty()) {
    			break;
    		}
    		
    		if (path.size() < minSeqLen) {
    			minSeqLen = path.size();
    			pathList.clear();
    			pathList.add(path);
    		} else if (path.size() == minSeqLen) {
    			pathList.add(path);
    		}
    	}
    	
    	clearVisited(trie);
    	
    	return pathList;
    }
    
    /**
     * Given a trie object, find one traversal path down to a descendant node that has 
     * context word. When visiting a node that has context word, return the path.
     * 
     * A trie is marked visited when its descendants are all marked visited, or when the 
     * descendant trie has context word(s). 
     * 
     * @param parentTrie Parent trie object
     * @param traversalRoot True if the parent trie object passed in the 1st parameter is the starting node 
     * in finding the path, else false. 
     * 
     * @return True if the immediate child node is marked visited, else false.
     */
	private boolean findOnePathToWord(Trie<String, String> parentTrie, List<String> path, boolean traversalRoot) {
		List<Trie<String, String>> childTrieList 
								= new ArrayList<Trie<String, String>>(parentTrie.getChildren());

		if (traversalRoot == false) {
			path.add(parentTrie.getKey());
			
			if (parentTrie.getValueSet().size() > 0) {
				parentTrie.setVisited(true);
				return true;
			}
		}
		
		if (childTrieList.isEmpty() == false) {
			int numChildVisited = 0;
			
			for (int i = 0; i < childTrieList.size(); i++) {
				if (childTrieList.get(i).isVisited() == false) {
					numChildVisited = findOnePathToWord(
							childTrieList.get(i), path, false) ? ++numChildVisited : numChildVisited;
					
					if (traversalRoot == false) {
						// If all descendants are marked visited, mark this trie visited
						if (numChildVisited == childTrieList.size()) {
							parentTrie.setVisited(true);
						}
					}

					// return immediately when one path is found
					return parentTrie.isVisited();
				}
				numChildVisited++;
			}
			parentTrie.setVisited(true);
		} else {
			if (traversalRoot == false) {
				// no children, mark visited
				parentTrie.setVisited(true);
			}
		}
		return parentTrie.isVisited();
	}
    
	/**
	 * Scan forward to find context word that matches the longest prefix of the truncated morse 
	 * with the fewest extra elements beyond those in morse.
	 * 
	 * @param trie
	 * @param morseCharList
	 * @param index
	 * @param status
	 * @return
	 */
	private Set<String> scanForward(
					Trie<String, String> trie, 
					List<String> morseCharList, 
					int index, 
					LookupStatus status) {
    	
		ContextWordTrie mwTrie = (ContextWordTrie) trie;
		Set<String> wordSet = null;
		Set<String> matchSet = new HashSet<String>();
    	
    	// get shortest paths to descendants which have context words.
    	List<List<String>> shortestPathList = findShortestPathToWord(trie);
    	List<String> extendMorseCharList = new ArrayList<String>();
    	
    	index++;
    	// for each path, match it
    	for (List<String> path : shortestPathList) {
    		extendMorseCharList.clear();
    		extendMorseCharList.addAll(morseCharList);
    		extendMorseCharList.addAll(path);
    		
    		wordSet = mwTrie.get(extendMorseCharList, index, status);
    		if (wordSet != null) {
    			matchSet.addAll(wordSet);
    		}
    	}

    	status.setPerfectMatch(false);
    	return matchSet;
	}
	
	/**
	 * Scan backward to find context word that matches the longest prefix of the morse.
	 * 
	 * This is called when the input morse word has wrong elements appended.

	 * Because get() is called recursively from root to leaf, simply returning word stored 
	 * in the nearest ancestor node, which will naturally make the returned word the longest 
	 * morse prefix of all ancestors.
	 * 
	 */
	private Set<String> scanBackward(Trie<String, String> trie, LookupStatus status) {
    	return trie.getValueSet();
	}
	
	/**
	 * When no perfect matches for morse are found, display the word from context that matches
	 * the longest prefix of morse, or has the fewest extra elements beyond those in morse. 
	 * 
	 * Conditions:
	 * 
	 * 	1. Exact match
	 * 	2. Extra unexpected morse elements 
	 * 	   	
	 * 		Assuming "IM" and "IN" are context words, and the word for the morse is "I". Both "IM" and
	 * 		"IN" will be displayed because they have the same string length.
	 * 		
	 * 	3. Truncated morse word elements
	 * 
	 * 	   	Assuming "SALES" and "SALESFORCE" are context words, and the word for the morse 
	 * 		is "SALE".
	 * 		
	 * 		In this situation, display "SALES" instead of "SALESFORCE" because "SALES" has fewest extra 
	 * 		elements beyond those in morse.
	 * 
	 * 	4. Both 2 and 3
	 * 	   
	 * 	   	Assuming "SALES" and "SALESFORCE" are context words. If the word for the morse 
	 * 		is "SALESF", it's a truncated word to "SALESFORCE", but it has the unwanted elements 
	 * 		"F", "O", "R", "C", and "E" to "SALES".
	 * 		
	 * 		In this situation, display "SALESFORCE" because it matches the longest prefix of 
	 * 		the morse for "SALESF".
	 *
	 * @param morseCharList List of input morse characters
	 * @param index Index to the element in the list
	 * @return Set of matched strings
	 */
    private Set<String> get(List<String> morseCharList, int index, LookupStatus status) {
    	Set<String> matchSet = null;
    	
    	// end of morse input
    	if (morseCharList.size() == index + 1) {
            ContextWordTrie trie = (ContextWordTrie) map.get(morseCharList.get(index));
            
            // trie is null if the input morse word has wrong elements appended
            if (trie == null) {
            	status.setPerfectMatch(false);
            	return matchSet;
            }
            
            matchSet = trie.getValueSet();
        	
        	/*
        	 * When code runs here, we have reached the end of morse input.
        	 * 	
        	 * 	If word set is null, it means 
        	 * 		1. not be able to find context word for the given morse input.
        	 * 		2. however, the leaf node in the tree hasn't been reached yet.
        	 * 		3. need to look forward to match the longest prefix of the truncated 
        	 * 		   morse input. 
        	 */
            if (matchSet == null || matchSet.isEmpty()) {
            	return scanForward(trie, morseCharList, index, status);
            }
        	
            status.setPerfectMatch(true);
        } else {
        	ContextWordTrie trie = (ContextWordTrie) map.get(morseCharList.get(index));

        	/*
        	 * If trie object is not null, it means it hasn't reached the leaf node of the tree.
        	 */
        	if (trie != null) {
	            matchSet = trie.get(morseCharList, ++index, status);
	            
	            if (matchSet == null || matchSet.isEmpty()) {
	            	return scanBackward(trie, status);
	            }
            } else {
            	// trie is null if the input morse word has wrong elements appended
            	status.setPerfectMatch(false);
            }
        }

    	return matchSet;
    }
    
    public Set<String> getBestMatch(String morseWord) {
    	List<String> morseCharList = analyzer.analyze(morseWord);
    	LookupStatus status = new LookupStatus();
        Set<String> wordSet = get(morseCharList, 0, status);
        
        // get a defensive copy
        Set<String> bestSet = new HashSet<String>();
       	
        if (status.isPerfectMatch()) {
        	if (wordSet.size() > 1) {
	        	/*
	        	 * If multiple context words match morse perfectly, then 
	        	 * select the matching word with the fewest characters.
	        	 */
	        	int minLen = Integer.MAX_VALUE;
	        	Iterator<String> it = wordSet.iterator();
	        	while (it.hasNext()) {
	        		String contextWord = it.next();
	        		
	        		if (contextWord.length() < minLen) {
	        			minLen = contextWord.length();
	        		}
	        	}
	        	
	        	it = wordSet.iterator();
	        	while (it.hasNext()) {
	        		String contextWord = it.next();	
	        		if (contextWord.length() == minLen) {
	        			bestSet.add(contextWord + PerfectMatchAmbiguityIndicator);
	        		}
	        	}        	
        	} else {
        		bestSet.addAll(wordSet);
        	}
        } else {
        	Iterator<String> it = wordSet.iterator();
        	
        	while (it.hasNext()) {
    			bestSet.add(it.next() + ImperfectMatchSuffixIndicator);
        	}
        }
        return bestSet;
    }
    
    public List<Set<String>> getBestMatches(List<String> morseWordList) {
		List<Set<String>> resultList = new ArrayList<Set<String>>();
		
		for (String morseWord : morseWordList) {	
			Set<String> resultSet = getBestMatch(morseWord.trim());
			if (resultSet != null) {
				resultList.add(resultSet);
			}
		}
		return resultList;
    }
    
    public static void main(String[] args) {
    }
}

