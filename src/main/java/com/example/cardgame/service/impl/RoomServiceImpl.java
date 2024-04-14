package com.example.cardgame.service.impl;

import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.game.Room;
import com.example.cardgame.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Service
public class RoomServiceImpl implements RoomService {


    private final Map<String, Room> activeRooms = new TreeMap<>();
    private final List<RoomDto> activeRoomsDto = new ArrayList<>();

    public Room getRoomByStringId(String name){
        return activeRooms.get(name);
    }

    public Room addRoom(Room room){

    }

}
