package com.example.cardgame.room.game;

import lombok.Getter;

import java.util.*;

public class CardDeck {

    @Getter
    public Card trampCard = null;

    @Getter
    public final Stack<Card> cards = generateDeck();


    private  Stack<Card> generateDeck(){
        List<Card> res = new ArrayList<>(36);


        for (Rank r : Rank.values()){
            for (Suit s : Suit.values()){
                res.add(new Card(s ,r ));
            }
        }

        Collections.shuffle(res);

        trampCard = res.get(0);

        Stack<Card> resultStack = new Stack<>();
        resultStack.addAll(res);

        return resultStack;
    }





}
