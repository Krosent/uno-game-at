package edu.vub.at.weuno.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import edu.vub.at.actors.ATFarReference;
import edu.vub.at.weuno.Player;
import edu.vub.at.weuno.PlayersHashMap;

/**
 * Interface that the Java object implements with the methods that AmbientTalk objects call on it.
 */
public interface JWeUno {
    JWeUno registerAtApplication(ATWeUno uno);

    void updateConnectedGamersCounter(int counter);

    void disableConnectButton();

    void enableGameButton();

    // update deck of other gamers (shared deck);
    void updateDeck(String[][] deck);

    void setGameState();

    void disableConnectionDialog();

    void setTopPlayerCardCount(int n);

    void setLeftPlayerCardCount(int n);

    void setRightPlayerCardCount(int n);


    // start game
    void startGame();


    // finish game
    // verify end game

}