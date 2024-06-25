package com.example.cardgame.service;

import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.room.Room;

import java.util.List;

public interface RoomService {

    public Room getRoomByStringId(String roomName);
    public RoomDto addRoom(Room room);
    public void removeRoom(String roomName);
    public List<RoomDto> getAll();
}
