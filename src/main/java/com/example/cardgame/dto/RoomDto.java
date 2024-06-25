package com.example.cardgame.dto;

import com.example.cardgame.room.Status;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class RoomDto {
    private String roomId;
    private String roomName;
    private Status status;
    private int countPlayer;
}
