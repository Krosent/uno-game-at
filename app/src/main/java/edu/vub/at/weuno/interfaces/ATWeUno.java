package edu.vub.at.weuno.interfaces;

import edu.vub.at.objects.coercion.Async;

/**
 * Interface that the AmbientTalk object needs to implement so that Java objects talk to it.
 */
public interface ATWeUno {
    /*
    * This method is used to connect player to the session and notify others
     */
    @Async
    void connectPlayer(String username);

    /*
    * This method is used once when user decides to start game after session is ready.
     */
    @Async
    void initializeGame(String[][] deck);

    /*
    * This method is used to update deck on all devices. Actually it updates deck on Tuple Space.
     */
    @Async
    void setDeck(String[][] deck);

    /*
    * This method does what it tells.
     */
    @Async
    void playCard(String[] card);

    /**
        Update a number of opponent cards. Parameter amount states the number of cards player is holding.
     */
    @Async
    void updateOPcards(int amount);

    /*
    * set***PlayerCardCount(n) methods are used to update counter of opponent players.
    * When player played a card, updateOPcards is used.
     */
    @Async
    void setTopPlayerCardCount(int n);

    @Async
    void setLeftPlayerCardCount(int n);

    @Async
    void setRightPlayerCardCount(int n);

    /*
    * This method is used when card is played and next player should make his\her turn.
     */
    @Async
    void nextPlayer();

    /*
    * Method for switching game direction. Game has two directions: Forward and Backward.
    * Initially game has Forward(Clockwise) direction.
     */
    @Async
    void switchMoveDirection();

    /*
    * When a player plays card with draw two action this method is called.
     */
    @Async
    void plusTwoAction();

    /*
     * When a player plays card with skip action this method is called.
     */
    @Async
    void skipAction();

    /*
     * When a player plays wild&draw 4 card this method is called.
     */
    @Async
    void plusFourWild();

    /**
     * This method is called when user on current device clicked uno button (when he had one card in hand)
     */
    @Async
    void unoSignal();

    /**
     * These methods are used for checking whether your opponents playing unfair.
     * In case if someone caught, he must draw two cards
     */
    @Async
    void leftPlayerUnoSignal();

    @Async
    void rightPlayerUnoSignal();

    @Async
    void topPlayerUnoSignal();
    /*
        --------------------------
     */

    /*
    * This method is used to enable end game dialog which is used to display winner and his\her score.
     */
    @Async
    void enableDialogWindow();

    /*
    * This method used to update score from TS to UI.
     */
    @Async
    void updateEndGameScore(int score);
}