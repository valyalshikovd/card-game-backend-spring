package com.example.cardgame.room.game;

import java.util.*;

public class CardDeck {

    public final Stack<Card> cards = generateDeck();

    public Card trampCard = null;



    private  Stack<Card> generateDeck(){
        List<Card> res = new ArrayList<>(36);


        for (Rank r : Rank.values()){
            for (Suit s : Suit.values()){
                res.add(new Card(s ,r ));
            }
        }

        Collections.shuffle(res);

        trampCard = res.get(35);

        Stack<Card> resultStack = new Stack<>();
        resultStack.addAll(res);

        return resultStack;
    }





}
