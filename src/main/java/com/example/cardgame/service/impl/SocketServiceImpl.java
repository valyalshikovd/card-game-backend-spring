package com.example.cardgame.service.impl;


import com.example.cardgame.dto.message.ExtendedMessageDto;
import com.example.cardgame.dto.message.MessageDto;
import com.example.cardgame.room.Room;
import com.example.cardgame.room.User;
import com.example.cardgame.room.UserStatus;
import com.example.cardgame.service.RoomService;
import com.example.cardgame.service.SocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SocketServiceImpl implements SocketService {
    private Map<String, MessageProcessor> runnableMap = new HashMap<>();
    private final RoomService roomService;
    private final ObjectMapper mapper;


    public SocketServiceImpl(RoomService roomService, ObjectMapper mapper) {
        this.roomService = roomService;
        this.mapper = mapper;
        runnableMap.put("regUserInRoom", this::regUserInRoom);
        runnableMap.put("connecting", this::connect);
        runnableMap.put("readyToPlay", this::readyToPlay);
        runnableMap.put("unreadyToPlay", this::unreadyToPlay);
    }

    public interface MessageProcessor {
        void process(ExtendedMessageDto messageDto);
    }

    @Override
    public void getMessage(ExtendedMessageDto message) {
        try {
            log.info("Сообщение сокета обрабатывается SocketService");
            runnableMap.get(message.getCommand()).process(message);
        } catch (Exception e) {
            log.error("Команды пришедшей на сокете нет. Команда: " + message.getCommand());

        }
    }

    private void regUserInRoom(ExtendedMessageDto extendedMessageDto) {

        log.info("Регистрация пользователя: " + extendedMessageDto.getMessageDto().getUserName());
        try {
            extendedMessageDto.getSession().sendMessage(new TextMessage("Попытка регистрации пользователя"));
            Room currentRoom = roomService.getRoomByStringId(extendedMessageDto.getRoomName());
            currentRoom.add(
                    new User(extendedMessageDto.getUserName(),UserStatus.UNREADY, extendedMessageDto.getSession())
            );
            System.out.println(currentRoom.getUsers());

            for(User user: currentRoom.getUsers()){
                try{
                    System.out.println("отправка");
                    user.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new MessageDto())));
                }catch (Exception e){
                    log.error("ошибка в отправке сообщения пользователю: " + user.getName() );
                }
            }
            log.info("Регистрация пользователя " + extendedMessageDto.getUserName() + " успешна");


        } catch (Exception e) {
            log.error("При регистрации пользователя " + extendedMessageDto.getUserName() + " произошла ошибка: \n" + e.getMessage());
        }
    }

    private void connect(ExtendedMessageDto extendedMessageDto){
        try {
            extendedMessageDto.getSession().sendMessage(new TextMessage("Success"));
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
                    .getUser(extendedMessageDto.getUserName())
                    .setStatus(UserStatus.READY);

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
                    .getUser(extendedMessageDto.getUserName())
                    .setStatus(UserStatus.UNREADY);

        }catch (Exception e){
            log.info("Ошибка принятия готовности игрока " + extendedMessageDto.getUserName());
        }
    }
}
