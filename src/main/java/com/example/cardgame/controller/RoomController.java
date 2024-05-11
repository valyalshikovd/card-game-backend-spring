package com.example.cardgame.controller;


import com.example.cardgame.configuration.ControllerConfiguration;
import com.example.cardgame.dto.RoomDto;
import com.example.cardgame.room.Room;
import com.example.cardgame.room.Status;
import com.example.cardgame.service.RoomService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;




@RestController
@AllArgsConstructor
@RequestMapping(ControllerConfiguration.routing + "/room")
@Slf4j
@CrossOrigin(origins = "*")
public class RoomController {


    private RoomService roomService;


    @GetMapping("/{roomName}")
    public ResponseEntity<RoomDto> get(@PathVariable String roomName){
        try {

            log.info("Запрос на получение комнаты: " + roomName);
            return ResponseEntity.ok(roomService.getRoomByStringId(roomName).mapToRoomDto());
        }catch (Exception e ){
            log.info("Запрос на получение комнаты: " + roomName + "выполнен неудачно");
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/getAll")
    public List<RoomDto> getAll() {
        log.info("Запрос на получение всех комнат ");
        return roomService.getAll();
    }


    @PostMapping
    public void createRoom(@RequestBody String roomName){
        log.info("Запрос на создание комнаты: " + roomName);
        roomService.addRoom(new Room(roomName, Status.OPEN));
    }

    @DeleteMapping
    public void deleteRoom(@RequestBody String roomName){
        try{
            log.info("Запрос на удаление  комнаты: " + roomName);
            roomService.removeRoom(roomName);
        }catch (Exception e){
            log.info("Запрос на удаление  комнаты: " + roomName + "выполнен неудачно");
        }

    }
}
