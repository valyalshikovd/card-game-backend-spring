package com.example.cardgame.dto.message.gameDto;

import com.example.cardgame.room.game.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InputGameStateDto {

    private Card beatingCard;
    private Card brokenCard;
    private boolean pass;
}
