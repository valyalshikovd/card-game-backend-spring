package com.example.cardgame.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;


@Data
@AllArgsConstructor
public class ExtendedMessageDto {
    private MessageDto messageDto;
    private WebSocketSession session;

    public String getUserName(){
        return messageDto.getUserName();
    }

    public String getRoomName(){
        return messageDto.getRoom();
    }



    public String getCommand(){
        return messageDto.getCommand();
    }

    public String getPayload() {return messageDto.getPayload();}
}
