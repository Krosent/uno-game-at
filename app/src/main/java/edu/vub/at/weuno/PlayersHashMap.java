package edu.vub.at.weuno;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.vub.at.actors.ATFarReference;
import edu.vub.at.objects.natives.NATNumber;

public class PlayersHashMap<K,V> {

    LinkedHashMap<Integer, Player> hashMap;

    public PlayersHashMap(LinkedHashMap<Integer, Player> customHashMap) {
        this.hashMap = customHashMap;
    }

    public PlayersHashMap() { this.hashMap = new LinkedHashMap<>(); }

    public void put(Integer k, Player v) {
        hashMap.put(k, v);
    }

    public boolean containsKey(K k) {
        return hashMap.containsKey(k);
    }

    public Set<Map.Entry<Integer,Player>> entrySet() {
        return this.hashMap.entrySet();
    }

    public int size() {
        return this.hashMap.size();
    }

    public int getPlayerId(ATFarReference FarRef) {
        int id = -1;
        for (LinkedHashMap.Entry<Integer, Player> me : this.hashMap.entrySet()) {
            if(me.getValue().farReference == FarRef) {
                return me.getKey();
            }
        }

        return id;
    }

    public ATFarReference getPlayerFarRef(int id) {
        ATFarReference ref;
        for (HashMap.Entry<Integer, Player> me : this.hashMap.entrySet()) {
            if(me.getKey() == id) {
                ref = me.getValue().farReference;
                return ref;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "PlayersHashMap{" +
                "hashMap=" + hashMap.toString() +
                '}';
    }
}
