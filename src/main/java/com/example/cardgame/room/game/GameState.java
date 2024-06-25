package com.example.cardgame.room.game;

import com.example.cardgame.dto.message.gameDto.GameStepInfoDto;
import com.example.cardgame.exception.gameException.GameException;
import com.example.cardgame.room.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Getter
@Configurable
@Slf4j
public class GameState {

    @Setter
    private Player player1;

    @Setter
    private Player player2;

    private Player currentPlayer;

    private Player defencePlayer;

    private int countCardsOnTable;

    @Setter
    private CardDeck cardDeck = new CardDeck();

    @Setter
    private List<TableCardPlace> table = new ArrayList<>(6);

    @Setter
    private boolean draw = false;

    @Setter
    private Player winner = null;

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
                changeCurrentPlayer();
                victoryCondition();
                return;
            }
            if (checkDefenceCondition(gameStepInfoDto)) {
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
            log.error("Step error");
        }
    }

    private void isNotPlayerCurrent(String playersSocketId) {
        if (!playersSocketId.equals(currentPlayer.getUser().getSession().getId())) {
            log.error("A player who is not current tries to make a move");
            throw new GameException("A player who is not current tries to make a move");
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
        log.error("The player tries to play with a missing card");
        throw new GameException("The player tries to play with a missing card");
    }

    private void checkAbilityToPlaceCard(GameStepInfoDto gameStepInfoDto) {
        if (gameStepInfoDto.getInTablePos() > 5 || gameStepInfoDto.getInTablePos() > defencePlayer.getCards().size()) {
            log.error("the position on which it is proposed to place a card either at the table or more than the cards the defender has");
            throw new GameException("the position on which it is proposed to place a card either at the table or more than the cards the defender has");
        }
        if (currentPlayer.getUser().getSession().getId().equals(defencePlayer.getUser().getSession().getId()) && gameStepInfoDto.isDown()) {
            log.error("the defending player cannot help but fight");
            throw new GameException("the defending player cannot help but fight");
        }
        if (!currentPlayer.getUser().getSession().getId().equals(defencePlayer.getUser().getSession().getId()) && !gameStepInfoDto.isDown()) {
            log.error("a non-defending player cannot fight");
            throw new GameException("a non-defending player cannot fight");
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
            log.error("Card pulling error");
        }
    }
    private void checkUserDefencePlayer(User user) {
        if (!user.getSession().getId().equals(defencePlayer.getUser().getSession().getId())) {
            log.error("User verification failed");
            throw new GameException("User verification failed");
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
            log.error("User verification failed");
            throw new GameException("User verification failed");
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
    public int getCountCardsAtOpp(String id) {
        if(player1.getUser().getSession().getId().equals(id)){
            return player2.getCards().size();
        }
        if(player2.getUser().getSession().getId().equals(id)){
            return player1.getCards().size();
        }
        return 0;
    }
}
