package com.example.cardgame.game;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
public class Room {
    @Getter
    @Setter
    private String roomName;

    @Getter
    @Setter
    private Status status;
    private final Map<String, User> users = new HashMap<>();
    public User get(String string){
        return users.get(string);
    }
    public void add(User user){
        users.put(user.getSession().getId(), user);
    }
    public User remove(String string){
        return users.remove(string);
    }

}
