package com.example.cardgame.controller;

import com.example.cardgame.dto.message.ExtendedMessageDto;
import com.example.cardgame.dto.message.MessageDto;
import com.example.cardgame.service.SocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@AllArgsConstructor
@Component
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {

    private SocketService socketService;
    private ObjectMapper objectMapper;


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String msg = String.valueOf(message.getPayload());


        log.debug("Message: \n" + msg + "\n from socket: \n " + session.getId() );

        socketService.getMessage(new ExtendedMessageDto(objectMapper.readValue(msg, MessageDto.class), session));

    }
}

