package com.example.cardgame.service.impl;

import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.exception.roomException.RoomExistsException;
import com.example.cardgame.exception.roomException.RoomDoesntExistException;
import com.example.cardgame.game.Room;
import com.example.cardgame.service.RoomService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
public class RoomServiceImpl implements RoomService {

    private final Map<String, Room> activeRooms = new TreeMap<>();

    @Override
    public Room getRoomByStringId(String roomName){
        if (activeRooms.get(roomName) == null)
            throw new RoomDoesntExistException("Room name: " + roomName);
        return activeRooms.get(roomName);
    }
    @Override
    public void addRoom(Room room){
        if (activeRooms.get(room.getRoomName()) != null)
            throw new RoomExistsException("Room name: " + room.getRoomName());
        activeRooms.put(room.getRoomName(), room);
    }
    @Override
    public void removeRoom(String roomName){
        if (activeRooms.get(roomName) == null)
            throw new RoomDoesntExistException("Room name: " + roomName);
        activeRooms.remove(roomName);
    }
    @Override
    public List<RoomDto> getAll(){
        System.out.println("запрос на получение всего");
        return activeRooms.values().stream().map(Room::mapToRoomDto).collect(Collectors.toList());
    }
}
