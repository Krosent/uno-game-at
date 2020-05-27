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
    void updateDeck(String[][] deck);

    @Async
    void drawCards(int numberOfCards);

    @Async
    void drawedCards(int numberOfCards, String[][] deck);
}
