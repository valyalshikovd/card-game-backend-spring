package com.example.cardgame.room.game;

import com.example.cardgame.dto.message.gameDto.GameStepInfoDto;
import com.example.cardgame.dto.message.gameDto.InputGameStateDto;
import com.example.cardgame.dto.message.gameDto.OutputGameStateDto;
import com.example.cardgame.exception.gameException.GameException;
import com.example.cardgame.room.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Configurable
@Slf4j
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

    @Getter
    @Setter
    private boolean draw = false;

    @Getter
    @Setter
    private Player winner = null;

    @Getter
    @Setter
    private boolean isGameOver = false;

    public void start() {


        generateTable(6);

        if (player1 == null || player2 == null)
            throw new GameException("отсутствует игрок");

        dealingCards(player1);
        dealingCards(player2);
        setCurrentPlayer();
    }

    private void dealingCards(Player player) {
        for (int i = 0; i < 6; i++) {
            player.getCards().add(cardDeck.cards.pop());
        }
    }

    private void issueCard(Player p) {

        if (p.getCards().size() > 6)
            return;

        if (cardDeck.cards.isEmpty())
            return;

        for (int i = 0; i < 7 - p.getCards().size(); i++) {
            if (cardDeck.cards.isEmpty())
                return;
            p.getCards().add(cardDeck.getCards().pop());
        }

    }

    private void generateTable(int countCards) {
        table.clear();
        countCardsOnTable = 0;
        for (int i = 0; i < countCards; i++) {
            table.add(new TableCardPlace());
        }
    }

    private void setCurrentPlayer() {
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

    private Player getAttackPlayer() {

        if (player1 == defencePlayer) {
            return player2;
        } else {
            return player1;
        }
    }

    private void changeCurrentPlayer() {
        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
    }

    public OutputGameStateDto getStep(InputGameStateDto string) {
        //В метод приходит информация о картах
        return null;
    }

    public OutputGameStateDto getGameState() {
        return null;
    }

    public Player getPlayer(String sessionId) {
        if (player1.getUser().getSession().getId().equals(sessionId)) {
            return player1;
        } else {
            return player2;
        }

    }

    public void setStep(GameStepInfoDto gameStepInfoDto, String playersSocketId) {

        try {
            isNotPlayerCurrent(playersSocketId);
            Card currentCard = getCurrentCardFromPlayer(gameStepInfoDto);
            checkAbilityToPlaceCard(gameStepInfoDto);


            if (checkAttackCondition(gameStepInfoDto)) {

                table.get(gameStepInfoDto.getInTablePos()).setDownCard(currentCard);
                countCardsOnTable++;

                if (tableContainsCardsLikePlayers()) {
                    victoryCondition();
                    return;
                }


                changeCurrentPlayer();                //обработка атаки
                victoryCondition();

                return;
            }

            if (checkDefenceCondition(gameStepInfoDto)) {      //буквально если карта кладется наверх и карта снизу есть
                //обработка защиты
                Card downCard = table.get(gameStepInfoDto.getInTablePos()).getDownCard();

                if ((currentCard.suit == downCard.suit && Rank.getNumericRang(currentCard.rank) > Rank.getNumericRang(downCard.rank))
                        || (currentCard.suit == cardDeck.trampCard.suit && downCard.suit != cardDeck.trampCard.suit)) {


                    table.get(gameStepInfoDto.getInTablePos()).setUpCard(currentCard);


                    if (areThereAnyUnbrokenCards()) {
                        victoryCondition();
                        return;
                    }

                    issueCard(currentPlayer);
                    victoryCondition();
                    changeCurrentPlayer();

                    return;
                }


            }

            victoryCondition();
        } catch (Exception e) {
            log.error("Ошибка хода");
            return;
        }
        //todo
        return;
    }

    private void isNotPlayerCurrent(String playersSocketId) {
        if (!playersSocketId.equals(currentPlayer.getUser().getSession().getId())) {
            log.error("Ход пытается сделать не текущий игрок");
            throw new GameException("Ход пытается сделать не текущий игрок");
        }
    }

    private Card getCurrentCardFromPlayer(GameStepInfoDto gameStepInfoDto) {
        Card requestedCard = gameStepInfoDto.getCard();
        for (Card card : currentPlayer.getCards()) {
            if (card.rank.equals(requestedCard.rank) && card.suit.equals(requestedCard.suit)) {
                currentPlayer.getCards().remove(card);
                return card;
            }
        }
        log.error("Игрок пытается походить отсутствующей картой");
        throw new GameException("Игрок пытается походить отсутствующей картой");
    }

    private void checkAbilityToPlaceCard(GameStepInfoDto gameStepInfoDto) {
        if (gameStepInfoDto.getInTablePos() > 5 || gameStepInfoDto.getInTablePos() > defencePlayer.getCards().size()) {
            log.error("позиция на которую предлагается положить карту либо за столом либо больше чем карт у защищающегося");
            throw new GameException("позиция на которую предлагается положить карту либо за столом либо больше чем карт у защищающегося");
        }

        if (currentPlayer.getUser().getSession().getId().equals(defencePlayer.getUser().getSession().getId()) && gameStepInfoDto.isDown()) {
            log.error("обороняющийся игрок не может не биться");
            throw new GameException("обороняющийся игрок не может не биться");
        }

        if (!currentPlayer.getUser().getSession().getId().equals(defencePlayer.getUser().getSession().getId()) && !gameStepInfoDto.isDown()) {
            log.error("не обороняющийся игрок не может биться");
            throw new GameException("не обороняющийся игрок не может биться");
        }
    }

    private boolean checkAttackCondition(GameStepInfoDto gameStepInfoDto) {
        return gameStepInfoDto.isDown() && table.get(gameStepInfoDto.getInTablePos()).getDownCard() == null;
    }

    private boolean checkDefenceCondition(GameStepInfoDto gameStepInfoDto) {
        return !gameStepInfoDto.isDown() && table.get(gameStepInfoDto.getInTablePos()).getDownCard() != null;
    }

    private boolean tableContainsCardsLikePlayers() {
        for (Card card : currentPlayer.getCards()) {

            for (TableCardPlace tableCardPlace : table) {

                if (tableCardPlace.getDownCard() != null && tableCardPlace.getDownCard().rank == card.rank)
                    return true;
                if (tableCardPlace.getUpCard() != null && tableCardPlace.getUpCard().rank == card.rank)
                    return true;
            }
        }
        return false;
    }

    public boolean areThereAnyUnbrokenCards() {
        for (TableCardPlace tableCardPlace : table) {
            if (tableCardPlace.getDownCard() != null && tableCardPlace.getUpCard() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean areThereCardsThatPlayerCanDiscard() {             //проверяет можно ли докинуть карту
        for (TableCardPlace tableCardPlace : table) {

            for (Card card : currentPlayer.getCards()) {
                if (card.rank == tableCardPlace.getUpCard().rank) {

                    return true;
                }
                if (card.rank == tableCardPlace.getDownCard().rank) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean IsItPossibleToBeat() {
        for (TableCardPlace tableCardPlace : table) {
            if (tableCardPlace.getDownCard() != null && tableCardPlace.getUpCard() == null) {
                for (Card card : currentPlayer.getCards()) {
                    if ((card.suit == tableCardPlace.getDownCard().suit && Rank.getNumericRang(card.rank) > Rank.getNumericRang(tableCardPlace.getDownCard().rank))
                            || (card.suit == cardDeck.trampCard.suit && tableCardPlace.getDownCard().suit != cardDeck.trampCard.suit)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void passWithChangeOfDefendingPlayer() {
        generateTable(6);

        if (player1 == defencePlayer) {
            defencePlayer = player2;
        } else {
            defencePlayer = player1;
        }
    }

    public void passWithoutDefendingPlayer() {
        generateTable(6);
    }

    public void pullOf(User user) {

        try {
            checkUserDefencePlayer(user);
            List<Card> tableCards = getTableCards();
            defencePlayer.getCards().addAll(tableCards);
            issueCard(getAttackPlayer());
            generateTable(6);
            changeCurrentPlayer();
            victoryCondition();
        } catch (Exception e) {
            log.error("ошибка стягивания карты");
        }
    }

    private void checkUserDefencePlayer(User user) {
        if (!user.getSession().getId().equals(defencePlayer.getUser().getSession().getId())) {
            log.error("Проверка пользователя не пройдена");
            throw new GameException("Проверка пользователя не пройдена");
        }
    }

    private List<Card> getTableCards() {
        List<Card> res = new ArrayList<>();
        Card downCard = null;
        Card upCard = null;
        for (TableCardPlace tableCardPlace : table) {

            downCard = tableCardPlace.getDownCard();
            upCard = tableCardPlace.getUpCard();

            if (downCard != null)
                res.add(downCard);
            if (upCard != null)
                res.add(upCard);
        }
        return res;
    }

    private void checkUserCurrentPlayer(User user) {
        if (!user.getSession().getId().equals(currentPlayer.getUser().getSession().getId())) {
            log.error("Проверка пользователя не пройдена");
            throw new GameException("Проверка пользователя не пройдена");
        }
    }

    public void complete(User user) {

        checkUserCurrentPlayer(user);

        if (!areThereAnyUnbrokenCards()) {
            issueCard(currentPlayer);
            generateTable(6);
            defencePlayer = getAttackPlayer();
        }


        changeCurrentPlayer();
        victoryCondition();
    }

    public int getCountCardsInStack() {
        return cardDeck.getCards().size();
    }

    private void victoryCondition() {
        if (player1.getCards().isEmpty() && player2.getCards().isEmpty()) {
            draw = true;
            isGameOver = true;
            return;
        }

        if (player1.getCards().isEmpty()) {
            winner = player1;
            isGameOver = true;
            return;
        }

        if (player2.getCards().isEmpty()) {
            winner = player2;
            isGameOver = true;
            return;
        }
    }

    public boolean isWinner(User user){
        if(winner == null)
            return false;
        return winner.getUser().getSession().getId().equals(user.getSession().getId());
    }

    public void surrender(User user) {

        isGameOver = true;
        if(player1.getUser().getSession().getId().equals(user.getSession().getId())){
            winner = player2;
        }else {
            winner = player1;
        }

    }
}
