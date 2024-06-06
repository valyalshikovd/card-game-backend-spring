package com.example.cardgame.room;


import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.dto.message.ExtendedMessageDto;
import com.example.cardgame.dto.message.SentMessageDto;
import com.example.cardgame.dto.message.gameDto.GameStepInfoDto;
import com.example.cardgame.dto.message.gameDto.OutputGameStateDto;
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
@Configurable(preConstruction = true)
public class Room {
    @Getter
    @Setter
    private String roomName;
    private GameState gameState;
    private final ObjectMapper mapper;

    public Room(String roomName, Status status) {
        this.roomName = roomName;
        this.status = status;
        this.mapper = new ObjectMapper();
    }


    @Getter
    @Setter
    private Status status;
    private final Map<String, User> users = new HashMap<>();

    public User getUser(String string) {
        return users.get(string);
    }

    public void add(User user) {
        users.put(user.getSession().getId(), user);
    }

    public User remove(String string) {
        return users.remove(string);
    }

    public RoomDto mapToRoomDto() {
        return new RoomDto(roomName, status, users.size());
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean checkReady() {
        return getUsers().stream().noneMatch(user -> user.getStatus() != UserStatus.READY) && getUsers().size() >= 2;
    }

    public void start() {
        status = Status.STARTED;
        for (User user : getUsers()) {
            sendToUserMessageAboutGameStarted(user);
        }
        log.info("Успешно отправлены сообщения о начале игры всем игрокам.");

        startRoom();
    }

    private void sendToUserMessageAboutGameStarted(User user) {
        try {
            user.getSession().sendMessage(new TextMessage(
                    mapper.writeValueAsString(new SentMessageDto("start_game", ""))
            ));
        } catch (Exception e) {
            log.error("Не удалось отправить сооьщение о старте игрку: " + user.getName());
            deleteUser(user);
            throw new RoomException("Не удалось отправить сооьщение о старте игрку: " + user.getName());
        }
    }

    private void startRoom() {
        try {
            Object[] arrUser = users.values().toArray();
            gameState = new GameState();
            gameState.setPlayer1(new Player((User) arrUser[0]));
            gameState.setPlayer2(new Player((User) arrUser[1]));
            gameState.start();

        } catch (Exception e) {
            log.error("Ошибка запуска игры в комнате " + roomName);
            log.error(e.getMessage());
        }
    }

    ;

    public void getGameState() {
        for (User user : getUsers()) {
            try{
                user.getSession().sendMessage(
                        new TextMessage(
                                mapper.writeValueAsString(
                                        new SentMessageDto(
                                                "game_state",
                                                mapper.writeValueAsString(new OutputGameStateDto(
                                                        gameState.getTable(),
                                                        gameState.getPlayer(user.getSession().getId()).getCards(),
                                                        gameState.getCardDeck().getTrampCard(),
                                                        gameState.getCurrentPlayer().getUser().getSession().getId().equals(user.getSession().getId()),
                                                        gameState.getDefencePlayer().getUser().getSession().getId().equals(user.getSession().getId()),
                                                        gameState.getCountCardsOnTable(),
                                                        gameState.areThereAnyUnbrokenCards(),
                                                        gameState.getCountCardsInStack(),
                                                        gameState.isDraw(),
                                                        gameState.isGameOver(),
                                                        gameState.isWinner(user),
                                                        gameState.getCountCardsAtOpp(user.getSession().getId())
                                                ))))));
            }catch (Exception e){
                System.out.println(e.getMessage());
                deleteUser(user);
                log.error("Ошибка отправки состояния игры пользователю :" + user.getName());
            }
        }

    }

    public void setStep(GameStepInfoDto gameStepInfoDto, String playersSocketIs){
        gameState.setStep(gameStepInfoDto, playersSocketIs);

        getGameState();
    }

    public void pullOf(ExtendedMessageDto extendedMessageDto) {

        User user = users.get(extendedMessageDto.getSession().getId());
        if(user.getSession().getId().equals(extendedMessageDto.getSession().getId())){

            gameState.pullOf(user);
            getGameState();
        }
    }

    public void complete(ExtendedMessageDto extendedMessageDto) {

        User user = users.get(extendedMessageDto.getSession().getId());
        if(user.getSession().getId().equals(extendedMessageDto.getSession().getId())){

            gameState.complete(user);
            getGameState();
        }
    }

    public void surrender(ExtendedMessageDto extendedMessageDto) {
        User user = users.get(extendedMessageDto.getSession().getId());
        if(user.getSession().getId().equals(extendedMessageDto.getSession().getId())){

            gameState.surrender(user);
            getGameState();
        }
        users.values().forEach(u -> u.setStatus(UserStatus.UNREADY));
        gameState = null;
    }

    private void deleteUser(User user){
        users.remove(user.getSession().getId());

        for (User u : getUsers()) {
            try {
                u.getSession().sendMessage(new TextMessage(
                        mapper.writeValueAsString(new SentMessageDto("addNotification", "Пользователь " + user.getName() + " был удален"))
                ));
            } catch (Exception e) {
                log.error("Не удалось отправить сооьщение о старте игрку: " + u.getName());
                deleteUser(u);
                throw new RoomException("Не удалось отправить сооьщение о старте игрку: " + u.getName());
            }
        }
    }
}
