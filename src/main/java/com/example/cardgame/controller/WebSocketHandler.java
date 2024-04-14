package com.example.cardgame.controller;

import com.example.cardgame.dto.Message;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.io.IOException;

public class WebSocketHandler extends AbstractWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String msg = String.valueOf(message.getPayload());
        System.out.println(message);
        System.out.println(session);
        System.out.println(msg);


        // Send back a unique message depending on the id received from the client
        switch(msg){
            case("1"):
                System.out.println("WebSocket" + "Dog button was pressed");
                session.sendMessage(new TextMessage("Woooof"));
                break;

            case("2"):
                System.out.println("WebSocket"+  "Cat button was pressed");
                session.sendMessage(new TextMessage("Meooow"));
                break;

            case("3"):
                System.out.println("WebSocket"+  "Pig button was pressed");
                session.sendMessage(new TextMessage("Bork Bork"));
                break;

            case("4"):
                System.out.println("WebSocket"+  "Fox button was pressed");
                session.sendMessage(new TextMessage("Fraka-kaka-kaka"));
                break;

            default:
                System.out.println("WebSocket" + "Connected to Client");
        }
    }
}
