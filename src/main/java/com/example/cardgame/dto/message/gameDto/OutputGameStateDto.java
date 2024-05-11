package com.example.cardgame.dto.message.gameDto;

import com.example.cardgame.room.game.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class OutputGameStateDto {


    private List<Card> table;
    private List<Card> playerCards;
    private Card trump;
}
