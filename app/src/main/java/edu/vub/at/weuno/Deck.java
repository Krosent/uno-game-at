package edu.vub.at.weuno;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Deck {

    Queue<Card> cards;

    public Deck() {
        LinkedList<Card> c = makeFullDeck();
        Collections.shuffle(c);

        cards = c;
    }

    public Deck(LinkedList<Card> newCards) {
        this.cards = newCards;
    }

    public void setDeck(LinkedList<Card> UpdatedCards) {
        this.cards = UpdatedCards;
    }

    public String[][] getDeckSerialized() {
        String[][] cardsArr = new String[this.cards.size()][2];
        int sizeOfQueue = cards.size();
        ArrayList cardsAL = new ArrayList(cards);
            for(int i=0; i<sizeOfQueue; i++) {
                    cardsArr[i][0] = ((Card) cardsAL.get(i)).getColor().name();
                    cardsArr[i][1] = ((Card) cardsAL.get(i)).getAction().name();
            }
        return cardsArr;
    }

    public Card peekTopCard() {
        final Iterator<Card> itr = cards.iterator();
        Card lastElement = itr.next();
        while(itr.hasNext()) {
            lastElement = itr.next();
        }
        return lastElement;
    }

    public Card drawCard() {
        LinkedList<Card> c = new LinkedList<>(cards);
        Collections.shuffle(c);
        cards = c;
        return cards.poll();
    }

    public static LinkedList<Card> makeFullDeck() {
        LinkedList<Card> deck = new LinkedList<>();

        for (Card.Color c : Card.Color.values())
            for (Card.Action a : Card.Action.values())
                if (c != Card.Color.wild && a != Card.Action.color && a != Card.Action.plus4)
                    if (a == Card.Action.a0)
                        deck.add(new Card(c, a));
                    else
                        Collections.addAll(deck, new Card[]{new Card(c, a), new Card(c, a)});


        for (int i=0; i<4; i++) {
            deck.add(new Card(Card.Color.wild, Card.Action.color));
            deck.add(new Card(Card.Color.wild, Card.Action.plus4));
        }

        return deck;
    }

    public int getAmountOfCards() { return cards.size(); }

    public HashMap peekTopCardSerialize() {
        /*
            We serialize card into hashmap which has two keys: color and action. They represented as strings.
         */

        Card topCard = peekTopCard();

        HashMap<String, String> card = new HashMap<>();

        card.put("color", topCard.getColor().name());
        card.put("action", topCard.getAction().name());

        return card;
    }

}
