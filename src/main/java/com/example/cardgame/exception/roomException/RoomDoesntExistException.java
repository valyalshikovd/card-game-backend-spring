package com.example.cardgame.exception.roomException;

public class RoomDoesntExistException extends RuntimeException {
    public RoomDoesntExistException(String message) {
        super("The room doesn't exist \n" + message);
    }
}
