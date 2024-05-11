package com.example.cardgame.room.game;

import java.util.Map;
import java.util.TreeMap;

public enum Rank {

    SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;


    private static final Map<Rank, Integer> numericRanges = createNumericRange();

    private static Map<Rank, Integer> createNumericRange() {

        Map<Rank, Integer> res = new TreeMap<>();
        int i = 1;
        for(Rank r : Rank.values()){
            res.put(r, i);
            System.out.println(r);
            i++;
        }
        return res;
    }

    public static int getNumericRang(Rank rank){
        return numericRanges.get(rank);
    }
}
