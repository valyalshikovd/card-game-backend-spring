package com.example.cardgame.game;

import jakarta.websocket.Session;
import lombok.Data;


@Data
public class User {
    private String name;
    private Session session;
}
