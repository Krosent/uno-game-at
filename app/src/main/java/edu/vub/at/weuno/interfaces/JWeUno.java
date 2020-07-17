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

    void displayToast(String text);

    void playTurn();

    void switchMoveDirection();

    void unoSignalVerification();

    void unoSignaled();

    void endGame();

    void enableEndGameDialog(String winnerName);


    // finish game
    // verify end game

}
