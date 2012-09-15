package com.test.util.trie.v2;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyze and construct the prefix keys for trie. 
 *
 */
public class KeyAnalyzer<K> {
	/**
	 * Analyze and construct keys
	 * 
	 * @param input Input object
	 * @return List of keys
	 */
    public List<K> analyze(K input) {
        List<K> list = new ArrayList<K>();
        
        list.add(input);
        return list;
    }
}
