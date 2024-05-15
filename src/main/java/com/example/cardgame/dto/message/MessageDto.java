package com.example.cardgame.dto.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String room;
    private String userName;
    private String command;
    private String payload;
}
