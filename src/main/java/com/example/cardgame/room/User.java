package com.example.cardgame.room;

import com.example.cardgame.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;


@Data
@AllArgsConstructor
public class User {
    private String name;
    private UserStatus status = UserStatus.UNREADY;
    private WebSocketSession session;


    public UserDto mapToUserDto(){
        return new UserDto(name, status);
    }
}
