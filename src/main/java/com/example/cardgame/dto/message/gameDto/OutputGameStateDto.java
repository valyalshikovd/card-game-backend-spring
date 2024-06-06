package com.example.cardgame.dto.message.gameDto;

import com.example.cardgame.room.game.Card;
import com.example.cardgame.room.game.TableCardPlace;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class OutputGameStateDto {


    private List<TableCardPlace> table;
    private List<Card> playerCards;
    private Card trump;
    private boolean currentPlayer;
    private boolean deffencePlayer;
    private int countCardsOnTable;
    private boolean areThereAnyUnbrokenCards;
    private int countCardsInStack;
    private boolean draw;
    private boolean isGameOver;
    private boolean isWinner;
    private int countCardAtOpp;
}
