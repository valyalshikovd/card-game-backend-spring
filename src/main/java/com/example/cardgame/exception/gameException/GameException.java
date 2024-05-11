package com.example.cardgame.exception.gameException;

public class GameException  extends RuntimeException{
    public GameException(String message) {
        super("GameError \n" + message);
    }
}
