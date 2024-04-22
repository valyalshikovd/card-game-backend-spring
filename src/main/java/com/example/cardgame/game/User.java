package com.example.cardgame.game;

import com.example.cardgame.controller.WebSocketHandler;
import com.example.cardgame.dto.UserDto;
import jakarta.websocket.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;


@Data
@AllArgsConstructor
public class User {
    private String name;
    private WebSocketSession session;


    public UserDto mapToUserDto(){
        return new UserDto(name);
    }
}
