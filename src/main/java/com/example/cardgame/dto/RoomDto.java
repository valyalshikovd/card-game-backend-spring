package com.example.cardgame.dto;

import com.example.cardgame.game.Status;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class RoomDto {
    private String roomName;
    private Status status;
    private int countPlayer;
}
