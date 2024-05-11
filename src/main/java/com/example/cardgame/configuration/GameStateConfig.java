package com.example.cardgame.configuration;


import com.example.cardgame.room.game.CardDeck;
import com.example.cardgame.room.game.GameState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class GameStateConfig {


    @Bean
    @Scope("prototype")
    public GameState gameState(){
        return new GameState();
    }

    @Bean
    @Scope("prototype")
    public CardDeck cardDeck(){
        return new CardDeck();
    }

}
