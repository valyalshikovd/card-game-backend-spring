package com.example.cardgame.dto;


import com.example.cardgame.room.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String name;
    private UserStatus status;
}
