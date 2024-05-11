package com.example.cardgame.dto.message;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SentMessageDto {
    private String command;
    private String payload;
}
