package edu.vub.at.weuno.interfaces;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import edu.vub.at.objects.coercion.Async;

/**
 * Interface that the AmbientTalk object needs to implement so that Java objects talk to it.
 */
public interface ATWeUno {
    @Async
    void connectPlayer(String username);

    @Async
    void initializeGame(String[][] deck);

    @Async
    void initializeRelations();

    @Async
    void setDeck(String[][] deck);

    @Async
    void playCard(String[] card);

    /**
        Update a number of opponent cards. Parameter amount states the number of cards player is holding.
     */
    @Async
    void updateOPcards(int amount);

    @Async
    void setTopPlayerCardCount(int n);

    @Async
    void setLeftPlayerCardCount(int n);

    @Async
    void setRightPlayerCardCount(int n);

    @Async
    void nextPlayer();

    @Async
    void switchMoveDirection();

    @Async
    void plusTwoAction();

    @Async
    void skipAction();

    @Async
    void plusFourWild();

    /**
     * This method is called when user on current device clicked uno button (when he had one card in hand)
     */
    @Async
    void unoSignal();

    /**
     * These methods are used for checking whether your opponents playing unfair. In case if someone caught, he must draw two cards
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

    @Async
    void enableDialogWindow();

    @Async
    void updateEndGameScore(int score);
}