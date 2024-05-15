package com.example.cardgame.room.game;

import com.example.cardgame.dto.message.gameDto.GameStepInfoDto;
import com.example.cardgame.dto.message.gameDto.InputGameStateDto;
import com.example.cardgame.dto.message.gameDto.OutputGameStateDto;
import com.example.cardgame.exception.gameException.GameException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
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

    @Getter
    private Player currentPlayer;

    @Getter
    private Player defencePlayer;

    @Getter
    private int countCardsOnTable;

    @Getter
    @Setter
    private CardDeck cardDeck = new CardDeck();

    @Getter
    @Setter
    private List<TableCardPlace> table = new ArrayList<>(6);


    public void start(){


        generateTable(6);

        if(player1 == null || player2 == null)
            throw new GameException("отсутствует игрок");

        dealingCards(player1);
        dealingCards(player2);
        setCurrentPlayer();
    }
    private void dealingCards(Player player){
        for(int i = 0; i < 6; i++){
            player.getCards().add(cardDeck.cards.pop());
        }
    }

    private void issueCard(Player p){

        if(p.getCards().size() > 6)
            return;

        for(int i = 0; i < 6 - p.getCards().size(); i++){
            p.getCards().add(cardDeck.getCards().pop());
        }

    }

    private void generateTable(int countCards){
        countCardsOnTable = 0;
        for(int i = 0; i < countCards; i++){
            table.add(new TableCardPlace());
        }
    }

    private void setCurrentPlayer(){
        Random random = new Random();
        double randomNumber = random.nextDouble();

        if (randomNumber < 0.5) {
            currentPlayer = player1;
            defencePlayer = player2;
        } else {
            currentPlayer = player2;
            defencePlayer = player1;

            //currentPlayer.sendMessage("отправить сообщение о том что он начинает") отправить сообщение о текущих картах и просить сделать ход.
        }
    }

    private void changeCurrentPlayer(){
        if(currentPlayer == player1){
            currentPlayer = player2;
        }else {
            currentPlayer = player1;
        }
    }

    public OutputGameStateDto getStep(InputGameStateDto string){
        //В метод приходит информация о картах
        return null;
    }

    public OutputGameStateDto getGameState(){
        return null;
    }

    public  Player getPlayer(String sessionId) {
        if(player1.getUser().getSession().getId().equals(sessionId)){
            return player1;
        }else{
            return player2;
        }

    }

    public void setStep(GameStepInfoDto gameStepInfoDto, String playersSocketId) {

        if(!playersSocketId.equals(currentPlayer.getUser().getSession().getId())){
            return;        //игрок не ходящий
        }

        Card currentCard = null;

        for(Card card : currentPlayer.getCards()){
            if(card.rank.equals(gameStepInfoDto.getCard().rank) && card.suit.equals(gameStepInfoDto.getCard().suit)){
                currentPlayer.getCards().remove(card);
                currentCard = card;
                break;
            }
        }

        if(currentCard == null)
            return; //карты почему то не оказалось у игрока

        if(gameStepInfoDto.getInTablePos() > 5 || gameStepInfoDto.getInTablePos() > defencePlayer.getCards().size()){
            return;  //позиция на которую предлагается положить карту либо за столом либо больше чем карт у защищающегося
        }

        if(currentPlayer.getUser().getSession().getId().equals(defencePlayer.getUser().getSession().getId()) && gameStepInfoDto.isDown())
            return;       //обороняющийся игрок не может не биться

        if(!currentPlayer.getUser().getSession().getId().equals(defencePlayer.getUser().getSession().getId()) && !gameStepInfoDto.isDown())
            return;    //не обороняющийся игрок не можеть биться


        if(gameStepInfoDto.isDown() && table.get(gameStepInfoDto.getInTablePos()).getDownCard() == null){

            table.get(gameStepInfoDto.getInTablePos()).setDownCard(currentCard);

            countCardsOnTable++;

            if(tableContainsCardsLikePlayers())
                return;

            issueCard(currentPlayer);
            changeCurrentPlayer();                //обработка атаки

            return;
        }

        if(!gameStepInfoDto.isDown()
        && table.get(gameStepInfoDto.getInTablePos()).getDownCard() != null){      //буквально если карта кладется наверх и карта снизу есть
            //обработка защиты
            Card downCard = table.get(gameStepInfoDto.getInTablePos()).getDownCard();

            if((currentCard.suit == downCard.suit && Rank.getNumericRang(currentCard.rank) > Rank.getNumericRang(downCard.rank))
                || (currentCard.suit == cardDeck.trampCard.suit && downCard.suit != cardDeck.trampCard.suit)) {

                table.get(gameStepInfoDto.getInTablePos()).setUpCard(currentCard);


                if(areThereAnyUnbrokenCards()){
                    return;
                }

                issueCard(currentPlayer);
                changeCurrentPlayer();
            }

        }



        //todo
        return;
    }

    private boolean tableContainsCardsLikePlayers(){
        for (Card card : currentPlayer.getCards()){

            for(TableCardPlace tableCardPlace : table){                     //содержит ли стол карты как у игрока
                if(tableCardPlace.getDownCard().rank == card.rank)
                    return true;

                if(tableCardPlace.getUpCard().rank == card.rank)
                    return true;
            }

        }
        return false;
    }


    private boolean areThereAnyUnbrokenCards(){
        for (TableCardPlace tableCardPlace : table){
            if(tableCardPlace.getDownCard() != null && tableCardPlace.getUpCard() == null){
                return true;
            }
        }
        return false;
    }
}
