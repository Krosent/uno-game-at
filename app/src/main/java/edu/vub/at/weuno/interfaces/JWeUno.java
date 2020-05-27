package edu.vub.at.weuno.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import edu.vub.at.actors.ATFarReference;

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

    // start game
    void startGame();

    void testFunction(HashMap<String, String> otherRefs, ATFarReference FarRefSender, ATFarReference FarRefReceiver);

    void getPlayerIdFromHMList(HashMap<String, Object> otherPlayersHM, ATFarReference FarRef);

    // finish game
    // verify end game

}
