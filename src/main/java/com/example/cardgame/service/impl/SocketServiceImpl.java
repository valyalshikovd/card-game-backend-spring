package com.example.cardgame.service.impl;


import com.example.cardgame.dto.ExtendedMessageDto;
import com.example.cardgame.dto.MessageDto;
import com.example.cardgame.game.Room;
import com.example.cardgame.game.User;
import com.example.cardgame.service.RoomService;
import com.example.cardgame.service.SocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SocketServiceImpl implements SocketService {
    private Map<String, MessageProcessor> runnableMap = new HashMap<>();
    private final RoomService roomService;
    private final ObjectMapper mapper;


    public SocketServiceImpl(RoomService roomService, ObjectMapper mapper) {
        this.roomService = roomService;
        this.mapper = mapper;

        runnableMap.put("regUserInRoom", this::regUserInRoom);

    }

    public interface MessageProcessor {
        String process(ExtendedMessageDto messageDto);
    }

    @Override
    public String getMessage(ExtendedMessageDto message) {

        try {
            return runnableMap.get(message.getMessageDto().getCommand()).process(message);
        } catch (Exception e) {
            System.out.println("Команд пришедшей на сокете нет");
            return "";
        }
    }

    private String regUserInRoom(ExtendedMessageDto extendedMessageDto) {


        System.out.println("Попытка регистрации пользователя");
        try {
            extendedMessageDto.getSession().sendMessage(new TextMessage("Попытка регистрации пользователя"));
            Room currentRoom = roomService.getRoomByStringId(extendedMessageDto.getMessageDto().getRoom());
            currentRoom.add(
                    new User(extendedMessageDto.getMessageDto().getUserName(), extendedMessageDto.getSession())
            );
            currentRoom.getUsers().stream().map((user) -> {
                try{
                    user.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new MessageDto())));
                }catch (Exception e){
                    System.out.println("чзх");
                }
                return null;
            });

        } catch (Exception e) {
            System.out.println("чзх");
        }
        return "";
    }


}
