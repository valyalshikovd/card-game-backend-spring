package com.example.cardgame.controller;

import com.example.cardgame.dto.ExtendedMessageDto;
import com.example.cardgame.dto.Message;
import com.example.cardgame.dto.MessageDto;
import com.example.cardgame.service.SocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;


@AllArgsConstructor
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    private SocketService socketService;
    private ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String msg = String.valueOf(message.getPayload());
        System.out.println(message);
        System.out.println(session);
        System.out.println(msg);

        try {
            System.out.println(new ObjectMapper().readValue(msg, MessageDto.class));
        } catch (Exception e) {
            System.out.println("-");
        }


        socketService.getMessage(new ExtendedMessageDto(objectMapper.readValue(msg, MessageDto.class), session));

    }
}

