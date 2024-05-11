package com.example.cardgame.exception.roomException;

public class RoomExistsException extends RuntimeException {
    public RoomExistsException(String message) {
        super("The room already exists \n" + message);
    }
}
