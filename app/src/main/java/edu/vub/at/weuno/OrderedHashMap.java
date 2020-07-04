package edu.vub.at.weuno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.vub.at.actors.ATFarReference;

public class OrderedHashMap<K,V> {


    TreeMap<Integer, ATFarReference> map;

    public OrderedHashMap(TreeMap<Integer, ATFarReference> customHashMap) {
        this.map = customHashMap;
    }

    public OrderedHashMap() { this.map = new TreeMap<>(); }

    public void put(Integer k, ATFarReference v) {
        map.put(k, v);
    }

    public Map.Entry<Integer, ATFarReference> nextEntry(Integer currentKey) {
        if(map.higherEntry(currentKey) == null) {
            return map.firstEntry();
        } else {
            return map.higherEntry(currentKey);
        }
    }

    public Map.Entry<Integer, ATFarReference> prevEntry(Integer currentKey) {
        if(map.lowerEntry(currentKey) == null) {
            return map.lastEntry();
        } else {
            return map.lowerEntry(currentKey);
        }
    }

    public void findClosestNextEntry(Integer currentKey) {
        TreeMap<Integer, ATFarReference> tempMap = new TreeMap<>(map);

        //while()
        //map.entrySet().iterator().next();
        //Map.Entry<Integer, ATFarReference> tempEntry = map.get(currentKey);
    }

    public void findclosestPredEntry(Integer currentKey) {

    }


    public boolean containsKey(Integer k) {
        return map.containsKey(k);
    }

    public Set<Map.Entry<Integer,ATFarReference>> entrySet() {
        return this.map.entrySet();
    }

    public int size() {
        return this.map.size();
    }

    public int getIndexOf(Integer k) {
        List<Integer> indexes = new ArrayList<>(map.keySet()); // <== Set to List
        return indexes.indexOf(k);
    }

    public int getPlayerId(ATFarReference FarRef) {
        int id = -1;
        for (LinkedHashMap.Entry<Integer, ATFarReference> me : this.map.entrySet()) {
            if(me.getValue() == FarRef) {
                return me.getKey();
            }
        }

        return id;
    }

    public ATFarReference getPlayerFarRef(int id) {
        ATFarReference ref;
        for (HashMap.Entry<Integer, ATFarReference> me : this.map.entrySet()) {
            if(me.getKey() == id) {
                ref = me.getValue();
                return ref;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "OrderedHashMap{" +
                "map=" + map +
                '}';
    }
}
