package com.example.cardgame.controller;

import com.example.cardgame.dto.message.ExtendedMessageDto;
import com.example.cardgame.dto.message.MessageDto;
import com.example.cardgame.service.SocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;


@AllArgsConstructor
@Component
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {

    private SocketService socketService;
    private ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String msg = String.valueOf(message.getPayload());

        log.debug("Сообщение: \n" + msg + "\n С сокета: \n " + session.getId() );

        socketService.getMessage(new ExtendedMessageDto(objectMapper.readValue(msg, MessageDto.class), session));

    }
}

