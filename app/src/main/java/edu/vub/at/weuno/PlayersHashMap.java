package edu.vub.at.weuno;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.vub.at.actors.ATFarReference;
import edu.vub.at.objects.natives.NATNumber;

public class PlayersHashMap<K,V> {

    HashMap<Integer, Player> hashMap;

    public PlayersHashMap(HashMap<Integer, Player> customHashMap) {
        this.hashMap = customHashMap;
    }

    public PlayersHashMap() { this.hashMap = new HashMap<>(); }

    public void put(Integer k, Player v) {
        hashMap.put(k, v);
    }

    public boolean containsKey(K k) {
        return hashMap.containsKey(k);
    }

    public Set<Map.Entry<Integer,Player>> entrySet() {
        return this.hashMap.entrySet();
    }

    public int getPlayerId(ATFarReference FarRef) {
        int id = -1;
        for (HashMap.Entry<Integer, Player> me : this.hashMap.entrySet()) {
            if(me.getValue().farReference == FarRef) {
                return me.getKey();
            }
        }

        return id;
    }

    @Override
    public String toString() {
        return "PlayersHashMap{" +
                "hashMap=" + hashMap.toString() +
                '}';
    }
}
