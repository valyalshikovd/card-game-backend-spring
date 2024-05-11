package com.example.cardgame.room.game;

import com.example.cardgame.room.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


public class Player {

    @Getter
    @Setter
    private  User user;
    public List<Card> cards = new ArrayList<>();

    public Player(User user) {
        this.user = user;
    }
}
