package com.example.cardgame.service.impl;

import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.exception.roomException.RoomExistsException;
import com.example.cardgame.exception.roomException.RoomDoesntExistException;
import com.example.cardgame.room.Room;
import com.example.cardgame.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final Map<String, Room> activeRooms = new TreeMap<>();

    @Override
    public Room getRoomByStringId(String roomName){
        log.info("Получение комнаты: " + roomName);
        if (activeRooms.get(roomName) == null)
            throw new RoomDoesntExistException("Room name: " + roomName);
        log.info("Комната " + roomName + " успешно получена");
        return activeRooms.get(roomName);
    }
    @Override
    public void addRoom(Room room){
        log.info("Создание комнаты: " + room.getRoomName());
        if (activeRooms.get(room.getRoomName()) != null)
            throw new RoomExistsException("Room name: " + room.getRoomName());
        activeRooms.put(room.getRoomName(), room);
        log.info("Комната " + room.getRoomName() + " успешно создана");
    }
    @Override
    public void removeRoom(String roomName){
        log.info("удаление комнаты: " + roomName);
        if (activeRooms.get(roomName) == null)
            throw new RoomDoesntExistException("Room name: " + roomName);
        activeRooms.remove(roomName);
        log.info("Комната " + roomName + " успешно удалена");
    }
    @Override
    public List<RoomDto> getAll(){
        log.info("Получение всех комнат");
        return activeRooms.values().stream().map(Room::mapToRoomDto).collect(Collectors.toList());
    }
}
