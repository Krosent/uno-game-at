package edu.vub.at.weuno;

import java.util.HashMap;
import java.util.Map;

public class PlayersHashMap<K,V> {

    HashMap<K, V> hashMap;

    public PlayersHashMap(HashMap<K, V> customHashMap) {
        this.hashMap = customHashMap;
    }

    public void put(K k, V v) {
        hashMap.put(k, v);
    }





}
