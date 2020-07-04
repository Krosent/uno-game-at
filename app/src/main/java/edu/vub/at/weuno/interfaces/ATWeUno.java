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

    @Async
    void askDrawCards(int numberOfCards, int userID);

    @Async
    void drawedCards(int numberOfCards, String[][] deck);
}
