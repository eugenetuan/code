package com.test.util.trie.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 	A trie, or prefix tree, is an ordered tree data structure that is used to store an associative array 
 * 	where the keys are usually strings. It's position in the tree defines the key with which it is associated. 
 * 	All the descendants of a node have a common prefix of the string associated with that node, and the root 
 * 	is associated with the empty string. Values are normally not associated with every node, only with leaves and 
 *	some inner nodes that correspond to keys of interest. All the descendants of a node have a common prefix of the 
 * 	string associated with that node, and the root is associated with the empty string.
 *
 */
public class Trie<K, V> {
    /**
     * Common prefix of descendants of the node
     */
    protected K key;
     
    /**
     * Depth relative to the root
     */
    protected int depth;

    /**
     * Values are normally not associated with every node, only with leaves and some inner nodes 
     * that correspond to keys of interest. 
     */
    protected Set<V> valueSet;
    
    /**
     * Map from next prefix to immediate descendants 
     */
    protected Map<K, Trie<K, V>> map = new HashMap<K, Trie<K, V>>(2);
    
    /**
     * Analyze input data and build up prefix keys
     */
    protected KeyAnalyzer<K> analyzer;
    
    /**
     * The flag is only used for finding the shortest path to descendants 
     * which have value. If it is true, the node will not be visited again 
     * to avoid duplication. 
     */
    private boolean visited;
    
    public Trie(KeyAnalyzer<K> analyzer) {
        this.analyzer = analyzer;
    }
    
    public Trie(KeyAnalyzer<K> analyzer, K key) {
        this.analyzer = analyzer;
        this.key = key;
    }

	public void clearVisited(Trie<K, V> trie) {
		List<Trie<K, V>> childTrieList = new ArrayList<Trie<K, V>>(trie.map.values());

		if (childTrieList.isEmpty() == false) {
			for (Trie<K, V> childTrie : childTrieList) {
				childTrie.visited = false;
				clearVisited(childTrie);
			}
		} 
		trie.visited = false;
	}
	
	/**
	 * For each key, create a trie object
	 * 
	 * @param key Key object
	 * @return Trie object being created
	 */
	public Trie<K, V> createTrie(K key) {
		Trie<K, V> trie = new Trie<K, V>(analyzer, key);
		trie.valueSet = new HashSet<V>();
		return trie;
	}
	
    protected void put(List<K> keys, V value, int index) {
        K key = null;
        
        if (keys.size() == index + 1) {
        	key = keys.get(index);
            Trie<K, V> trie = map.get(key);
            if (trie == null) {
            	trie = createTrie(key);
                map.put(key, trie);
                trie.depth = index + 1;
            }
            
            /* 
             * The trie object corresponding to the last key in the 
             * key list stores values.
             */
            trie.valueSet.add(value);
        } else {
            key = keys.get(index);
            Trie<K, V> trie = map.get(key);
            if (trie == null) {
            	trie = createTrie(key);
                map.put(key, trie);
                trie.depth = index + 1;
            }
            trie.put(keys, value, ++index);
        }        
    }
    
    public Set<V> get(List<K> keys, int index) {
        if (keys.size() == index + 1) {
            Trie<K, V> trie = map.get(keys.get(index));
            return trie != null ? trie.valueSet : null;
        } else {
            Trie<K, V> trie = map.get(keys.get(index));
            return trie.get(keys, ++index);
        }        
    }
    
    public void put(K key, V value) {
        List<K> keys = analyzer.analyze(key);
        put(keys, value, 0);
    }

    public Set<V> get(K key) {
        List<K> keys = analyzer.analyze(key);
        return get(keys, 0);
    }
    
    public Set<V> get(List<K> keys) {
        return get(keys, 0);
    }
    
    public Set<V> getValueSet() {
    	return valueSet;
    }
    
    public void setValueSet(Set<V> valueSet) {
    	this.valueSet = valueSet;
    }
    
    public K getKey() {
    	return key;
    }
    
    public void setVisited(boolean visited) {
    	this.visited = visited;
    }
    
    public boolean isVisited() {
    	return visited;
    }
    
    public Collection<Trie<K, V>> getChildren() {
    	return this.map.values();
    }
    
    public KeyAnalyzer<K> getAnalyzer() {
    	return analyzer;
    }
    
    public static void main(String[] args) {

    }
}
