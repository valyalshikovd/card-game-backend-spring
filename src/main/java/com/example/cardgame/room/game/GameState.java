package com.example.cardgame.room.game;

import com.example.cardgame.dto.message.gameDto.InputGameStateDto;
import com.example.cardgame.dto.message.gameDto.OutputGameStateDto;
import com.example.cardgame.exception.gameException.GameException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Random;


@Configurable
public class GameState {

    @Getter
    @Setter
    private Player player1;

    @Getter
    @Setter
    private Player player2;
    private Player currentPlayer;
    private CardDeck cardDeck;
    private List<Card> table;

    @Autowired
    public void setCardDeck(CardDeck cardDeck) {
        this.cardDeck = cardDeck;
    }

    public void start(){
        if(player1 == null || player2 == null)
            throw new GameException("отсутствует игрок");

        dealingCards(player1);
        dealingCards(player2);
        setCurrentPlayer();
    }
    private void dealingCards(Player player){
        for(int i = 0; i < 6; i++){
            player.cards.add(cardDeck.cards.pop());
        }
    }

    private void setCurrentPlayer(){
        Random random = new Random();
        double randomNumber = random.nextDouble();

        if (randomNumber < 0.5) {
            currentPlayer = player1;
        } else {
            currentPlayer = player2;

            //currentPlayer.sendMessage("отправить сообщение о том что он начинает") отправить сообщение о текущих картах и просить сделать ход.
        }
    }

    public OutputGameStateDto getStep(InputGameStateDto string){
        //В метод приходит информация о картах
        return null;
    }


}
