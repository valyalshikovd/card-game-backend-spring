package com.example.cardgame.service.impl;

import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.dto.message.SentMessageDto;
import com.example.cardgame.exception.roomException.RoomExistsException;
import com.example.cardgame.exception.roomException.RoomDoesntExistException;
import com.example.cardgame.room.Room;
import com.example.cardgame.room.User;
import com.example.cardgame.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final ObjectMapper mapper;
    private final Map<String, Room> activeRooms = new TreeMap<>();

    @Override
    public Room getRoomByStringId(String roomNameId){
        log.info("Getting room: " + roomNameId);
        if (activeRooms.get(roomNameId) == null)
            throw new RoomDoesntExistException("Room name: " + roomNameId);
        log.info("Room " + roomNameId + " received successfully");
        return activeRooms.get(roomNameId);
    }
    @Override
    public RoomDto addRoom(Room room){
        log.info("Creating room: " + room.getRoomName());
        if (activeRooms.get(room.getRoomName()) != null)
            throw new RoomExistsException("Room name: " + room.getRoomName());
        activeRooms.put(room.getRoomId(), room);
        log.info("Room " + room.getRoomName() + " received successfully");
        return room.mapToRoomDto();
    }
    @Override
    public void removeRoom(String roomId){
        log.info("Removing room: " + roomId);
        if (activeRooms.get(roomId) == null)
            throw new RoomDoesntExistException("Room name: " + roomId);
        activeRooms.remove(roomId);
        log.info("Room " + roomId + " removed successfully");
    }
    @Override
    public List<RoomDto> getAll(){
        log.info("Getting all rooms");
        return activeRooms.values().stream().map(Room::mapToRoomDto).collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 30000)
    private void checkVoidRooms(){
        for(String key : activeRooms.keySet()){
            if(activeRooms.get(key).getUsers().isEmpty()){
                activeRooms.remove(key);
                log.info("Room was deleted");
            }
        }
    }
    @Scheduled(fixedRate = 15000)
    private void checkDisconnectedSocket(){
        for(Room room : activeRooms.values()){
            for(User user : room.getUsers()){
                try {
                    user.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new SentMessageDto("textNotification", "check"))));
                }catch (Exception e){
                    log.info("Обнаружен отключившийся пользователь");
                    room.deleteUser(user);
                }
            }
        }
    }
}
