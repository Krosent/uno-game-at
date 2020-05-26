package edu.vub.at.weuno.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface that the Java object implements with the methods that AmbientTalk objects call on it.
 */
public interface JWeUno {
    JWeUno registerAtApplication(ATWeUno uno);

    void updateConnectedGamersCounter(int counter);

    void disableConnectButton();

    void enableGameButton();

    // init game
    void initGame();

    // update deck of other gamers (shared deck);
    void updateDeck(String[][] deck);

    // start game
    void startGame();

    // finish game
    // verify end game

}
