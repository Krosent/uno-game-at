package edu.vub.at.weuno.interfaces;


/**
 * Interface that the Java object implements with the methods that AmbientTalk objects call on it.
 */
public interface JWeUno {
    /*
    * Register your Ambient Talk code at Android part.
     */
    JWeUno registerAtApplication(ATWeUno uno);

    void updateConnectedGamersCounter(int counter);

    /*
     * Method for disabling connect button when number of players reached to four.
     */
    void disableConnectButton();

    /*
     * Method for enabling game button when number of players reached to two and less than four.
     */
    void enableGameButton();

    // update deck of other gamers (shared deck);
    void setDeck(String[][] deck);

    /*
     * This method set game state to running. You can ignore this method, since it was used in
     * first iterations of application and after that does not give much functionality,
     */
    void setGameState();

    /*
     * When game is started, you need to disable connection dialog.
     */
    void disableConnectionDialog();

    /*
     * Description of these methods can be found at AT API file.
     */
    void setTopPlayerCardCount(int n);

    void setLeftPlayerCardCount(int n);

    void setRightPlayerCardCount(int n);

    /*
     * Show Toast on the screen.
     */
    void displayToast(String text);

    /*
     * When current device turn has come this method is called.
     */
    void playTurn() throws InterruptedException;

    /*
     * Method for switching game direction. Game has two directions: Forward and Backward.
     * Initially game has Forward(Clockwise) direction.
     */
    void switchMoveDirection();

    /*
     * Verify whether uno was called on time.
     */
    void unoSignalVerification();

    /*

     */
    void unoSignaled();

    /*
     * When player played all cards this method is called.
     */
    void endGame();

    void enableEndGameDialog(String winnerName);

    /*
     * Call when player reconnected.
     */
    void playerReconnected();

    /*
     * This method called when the next player is disconnected and system should do something
     * about it.
     */
    void nextPlayerDisconnected();

    /*
     * When game is ended each player should give to others their score. This method is called in
     * that situation.
     */
    void calculateScoreAndSendBack();
}
