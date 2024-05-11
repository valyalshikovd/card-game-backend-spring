package com.example.cardgame.room;


import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.dto.message.SentMessageDto;
import com.example.cardgame.exception.roomException.RoomException;
import com.example.cardgame.room.game.GameState;
import com.example.cardgame.room.game.Player;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Slf4j
@Configurable
public class Room {
    @Getter
    @Setter
    private String roomName;
    private  GameState gameState;
    private  ObjectMapper mapper;
    public Room(String roomName, Status status) {
        this.roomName = roomName;
        this.status = status;
    }

    @Autowired
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Autowired
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Getter
    @Setter
    private Status status;
    private final Map<String, User> users = new HashMap<>();
    public User getUser(String string){
        return users.get(string);
    }
    public void add(User user){
        users.put(user.getSession().getId(), user);
    }
    public User remove(String string){
        return users.remove(string);
    }
    public RoomDto mapToRoomDto(){
        return new RoomDto(roomName, status, users.size());
    }
    public List<User> getUsers(){
        return new ArrayList<>(users.values());
    }

    public boolean checkReady() {
        return getUsers().stream().noneMatch(user -> user.getStatus() != UserStatus.READY);
    }
    public void start()  {
        status = Status.STARTED;
        for(User user : getUsers()){
            sendToUserMessageAboutGameStarted(user);
        }
        log.info("Успешно отправлены сообщения о начале игры всем игрокам.");

        startRoom();
    }

    private void sendToUserMessageAboutGameStarted(User user){
        try {
            user.getSession().sendMessage(new TextMessage(
                    mapper.writeValueAsString(new SentMessageDto("start_game", ""))
            ));
        }catch (Exception e){
            log.error("Не удалось отправить сооьщение о старте игрку: " + user.getName());
            throw new RoomException("Не удалось отправить сооьщение о старте игрку: " + user.getName());
        }
    }

    private void startRoom(){
        try {
            Object[] arrUser = users.values().toArray();
            gameState.setPlayer1(new Player((User)arrUser[0]));
            gameState.setPlayer2(new Player((User)arrUser[1]));
            gameState.start();
        }catch (Exception e){
            log.error("Ошибка запуска игры в комнате " + roomName);
            log.error(e.getMessage());
        }
    };

}
