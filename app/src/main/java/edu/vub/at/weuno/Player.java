package edu.vub.at.weuno;

import edu.vub.at.actors.ATFarReference;

public class Player {
    String name;
    ATFarReference farReference;

    public Player(String name, ATFarReference farReference) {
        this.name = name;
        this.farReference = farReference;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", farReference=" + farReference +
                '}';
    }
}
