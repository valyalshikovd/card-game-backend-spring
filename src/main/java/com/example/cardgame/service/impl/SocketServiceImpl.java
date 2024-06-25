package com.example.cardgame.service.impl;


import com.example.cardgame.dto.message.ExtendedMessageDto;
import com.example.cardgame.dto.message.MessageDto;
import com.example.cardgame.dto.message.SentMessageDto;
import com.example.cardgame.dto.message.gameDto.GameStepInfoDto;
import com.example.cardgame.room.Room;
import com.example.cardgame.room.User;
import com.example.cardgame.room.UserStatus;
import com.example.cardgame.service.RoomService;
import com.example.cardgame.service.SocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SocketServiceImpl implements SocketService {
    private Map<String, MessageProcessor> runnableMap = new HashMap<>();

    private Map<String, WebSocketSession> sessionMap = new HashMap<>();
    private final RoomService roomService;
    private final ObjectMapper mapper;


    public SocketServiceImpl(RoomService roomService, ObjectMapper mapper) {
        this.roomService = roomService;
        this.mapper = mapper;
        runnableMap.put("regUserInRoom", this::regUserInRoom);
        runnableMap.put("connecting", this::connect);
        runnableMap.put("readyToPlay", this::readyToPlay);
        runnableMap.put("unreadyToPlay", this::unreadyToPlay);
        runnableMap.put("getGameState", this::getGameState);
        runnableMap.put("gameStep", this::gameStep);
        runnableMap.put("pullOf", this::pullOf);
        runnableMap.put("complete", this::complete);
        runnableMap.put("surrender", this::surrender);
    }


    public interface MessageProcessor {
        void process(ExtendedMessageDto messageDto);
    }

    @Override
    public void getMessage(ExtendedMessageDto message) {
        try {
            if(!sessionMap.containsKey(message.getSession().getId())){
                sessionMap.put(message.getSession().getId(), message.getSession());
            }
            log.info("Сообщение сокета обрабатывается SocketService");
            runnableMap.get(message.getCommand()).process(message);
        } catch (Exception e) {
            log.error("Команды пришедшей на сокете нет. Команда: " + message.getCommand());

        }
    }

    private void regUserInRoom(ExtendedMessageDto extendedMessageDto) {

        log.info("Регистрация пользователя: " + extendedMessageDto.getMessageDto().getUserName());
        try {
            extendedMessageDto.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new SentMessageDto("textNotification", "Попытка регистрации пользователя"))));
            Room currentRoom = roomService.getRoomByStringId(extendedMessageDto.getRoomName());

            if(currentRoom.getUsers().size() > 1){
                log.error("При регистрации пользователя " + extendedMessageDto.getUserName() + " произошла ошибка: \n" + "Комната переполнена");
                return;
            }
            currentRoom.add(
                    new User(extendedMessageDto.getUserName(),UserStatus.UNREADY, extendedMessageDto.getSession())
            );
            System.out.println(currentRoom.getUsers());

            emitToAllUserInRoom(currentRoom, new SentMessageDto("addNotification", "В комнату присоединился игрок: " + extendedMessageDto.getUserName()));
            log.info("Регистрация пользователя " + extendedMessageDto.getUserName() + " успешна");


        } catch (Exception e) {
            log.error("При регистрации пользователя " + extendedMessageDto.getUserName() + " произошла ошибка: \n" + e.getMessage());
        }
    }

    private void connect(ExtendedMessageDto extendedMessageDto){
        try {
            extendedMessageDto.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new SentMessageDto("textNotification", "Success"))));
            log.info("Сокет " + extendedMessageDto.getSession().getId() + " был подключен успешно");
        } catch (IOException e) {
            log.error("Сокет " + extendedMessageDto.getSession().getId() + " не был подключен успешно");
            throw new RuntimeException(e);
        }
    }

    private void readyToPlay(ExtendedMessageDto extendedMessageDto){
        log.info("Игрок " + extendedMessageDto.getUserName() + " объявил о готовности");
        Room currentRoom = roomService.getRoomByStringId(extendedMessageDto.getRoomName());
        try{
            currentRoom
                    .getUser(extendedMessageDto.getSession().getId())
                    .setStatus(UserStatus.READY);
            emitToAllUserInRoom(currentRoom, new SentMessageDto("addNotification", "Игрок " + extendedMessageDto.getUserName() + " готов" ));

            if (currentRoom.checkReady()){
                currentRoom.start();
                log.info("Началась игра в комнате " + extendedMessageDto.getRoomName());
            }
        }catch (Exception e){
            log.info("Ошибка принятия готовности игрока " + extendedMessageDto.getUserName());

        }

    }

    private void unreadyToPlay(ExtendedMessageDto extendedMessageDto){
        log.info("Игрок " + extendedMessageDto.getUserName() + " объявил о своей неготовности");
        Room currentRoom = roomService.getRoomByStringId(extendedMessageDto.getRoomName());
        try{
            currentRoom
                    .getUser(extendedMessageDto.getSession().getId())
                    .setStatus(UserStatus.UNREADY);
            emitToAllUserInRoom(currentRoom, new SentMessageDto("addNotification", "Игрок " + extendedMessageDto.getUserName() + "не готов" ));
        }catch (Exception e){
            log.info("Ошибка принятия готовности игрока " + extendedMessageDto.getUserName());
        }
    }

    private void emitToAllUserInRoom(Room currentRoom, SentMessageDto sentMessageDto){
        for(User user: currentRoom.getUsers()){
            try{
                user.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(sentMessageDto)));
            }catch (Exception e){
                currentRoom.remove(user.getSession().getId());
                log.error("error in sending message to user: " + user.getName() );
            }
        }
    }

    private void getGameState(ExtendedMessageDto extendedMessageDto){
        try {
            roomService.getRoomByStringId(extendedMessageDto.getRoomName()).getGameState();
        }catch (Exception e){
            log.error("error in sending a request to get the game state: " + extendedMessageDto.getRoomName() );
        }
    }

    private void gameStep(ExtendedMessageDto extendedMessageDto){
        try {
            GameStepInfoDto gameStepInfoDto = mapper.readValue(extendedMessageDto.getPayload(), GameStepInfoDto.class);
            roomService.getRoomByStringId(extendedMessageDto.getRoomName()).setStep(gameStepInfoDto, extendedMessageDto.getSession().getId());
        }catch (Exception e){
            log.error("error in sending a request to get the game state after making a move: " + extendedMessageDto.getRoomName() );
            log.error(e.getMessage());

        }
    }

    private void pullOf(ExtendedMessageDto extendedMessageDto){
        try {
            roomService.getRoomByStringId(extendedMessageDto.getRoomName()).pullOf(extendedMessageDto);
        }catch (Exception e){
            log.error("error in sending a request to draw a card in a room: " + extendedMessageDto.getRoomName() );
            log.error(e.getMessage());
        }
    }

    private void complete(ExtendedMessageDto extendedMessageDto){
        try {
            roomService.getRoomByStringId(extendedMessageDto.getRoomName()).complete(extendedMessageDto);
        }catch (Exception e){
            log.error("error in sending a request to interrupt the progress in the room: " + extendedMessageDto.getRoomName() );
            log.error(e.getMessage());
        }
    }

    private void surrender(ExtendedMessageDto extendedMessageDto){
        try{
            roomService.getRoomByStringId(extendedMessageDto.getRoomName()).surrender(extendedMessageDto);
        }catch (Exception e){
            log.error("error in sending a request for rent in a room: " + extendedMessageDto.getRoomName() );
            log.error(e.getMessage());
        }
    }


}
