package com.example.cardgame.exception.roomException;

public class RoomException extends RuntimeException{
    public RoomException(String message) {
        super("Room's error \n" + message);
    }
}
