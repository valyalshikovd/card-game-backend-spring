package com.example.cardgame.dto;

import com.example.cardgame.game.Status;
import com.example.cardgame.game.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class RoomDto {
    private String roomName;
    private Status status;
    private int countPlayer;
}
