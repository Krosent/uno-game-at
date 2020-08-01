package edu.vub.at.weuno;

import android.util.Log;

import java.lang.reflect.Field;

public class Card {


    public static enum Action {
        a0, a1, a2, a3, a4, a5, a6, a7, a8, a9,
        skip, reverse, plus2, plus4, color
    }

    public static enum Color {
        yellow, red, blue, green, wild
    }

    private Color color;
    private Action action;

    private int id = 0;

    public Card(Color color, Action action) {
        this.color = color;
        this.action = action;
    }

    public Card(String color, String action, Boolean bool) {
        this.color = getColorFromString(color);
        this.action = getActionFromString(action);
        Log.e("Color: ", color);
    }

    public Card(Card c) {
        this.color = c.getColor();
        this.action = c.getAction();
    }

    public int getResourceId() {
        if (id != 0)
            return id;

        try {
            Class d = R.drawable.class;
            Field en = d.getDeclaredField(this.toString());
            id = en.getInt(null);

            return id;
        } catch (Exception e) {
            return R.drawable.weuno;
        }
    }

    @Override
    public String toString() {
        return color.toString() + "_" + action.toString();
    }

    public static Color getColorFromString(String color) {
        return Color.valueOf(color);
    }

    public static Action getActionFromString(String action) {
        return Action.valueOf(action);
    }


    public Color getColor() {
        return color;
    }

    public Action getAction() {
        return action;
    }

    public String[] getCardSerialized() {

        String[] card = new String[2];

        card[0] = this.getColor().toString();
        card[1] = this.getAction().toString();

        Log.e("Get Color:", " " + card);
        Log.e("Get colorr:", " " + card[0]);

        return card;
    }
}
