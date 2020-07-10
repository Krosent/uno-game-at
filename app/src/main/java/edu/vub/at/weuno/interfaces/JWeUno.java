package edu.vub.at.weuno.interfaces;


/**
 * Interface that the Java object implements with the methods that AmbientTalk objects call on it.
 */
public interface JWeUno {
    JWeUno registerAtApplication(ATWeUno uno);

    void updateConnectedGamersCounter(int counter);

    void disableConnectButton();

    void enableGameButton();

    // update deck of other gamers (shared deck);
    void setDeck(String[][] deck);

    void setGameState();

    void disableConnectionDialog();

    void setTopPlayerCardCount(int n);

    void setLeftPlayerCardCount(int n);

    void setRightPlayerCardCount(int n);

    // start game
    void startGame();

    void displayToast(String text);


    // finish game
    // verify end game

}
