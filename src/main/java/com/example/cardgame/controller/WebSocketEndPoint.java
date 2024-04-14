package com.example.cardgame.controller;


import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat/{username}")
public class WebSocketEndPoint {
}
