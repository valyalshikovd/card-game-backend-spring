package com.example.cardgame.dto.message.gameDto;


import com.example.cardgame.room.game.Card;
import lombok.Data;

@Data
public class GameStepInfoDto {

    private int inTablePos;

    private boolean isDown;

    private Card card;

}
