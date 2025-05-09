package com.pingpong.chat.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.chat.Actors.RoomManagerActor;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.pingpong.chat.Models.RoomRequest;
import com.pingpong.chat.Utils.Guid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RestController
public class ChatController {

    private final ActorRef roomManager;
    private final ObjectMapper mapper;

    @Autowired
    public ChatController(ActorRef roomManager,ObjectMapper mapper) {
        this.roomManager = roomManager;
        this.mapper = mapper;
    }

    @PostMapping("/api/create")
    public CompletableFuture<ResponseEntity<String>> createRoom(@RequestBody RoomRequest request) {
        return Patterns.ask(roomManager, new RoomManagerActor.Create(request.getRoomName()), Duration.ofSeconds(3))
                .thenApply(result -> {
                    RoomManagerActor.RoomCreated msg = (RoomManagerActor.RoomCreated) result;
                    Guid out = new Guid();
                    out.setGuid(msg.getRoomId());
                    String json = null;
                    try {
                        json = mapper.writeValueAsString(out);
                        System.out.println(json);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return ResponseEntity.status(HttpStatus.OK).body(json);
                }).toCompletableFuture();
    }

    @GetMapping("/api/fetchRooms")
    public CompletableFuture<ResponseEntity<String>> fetchRooms() {
        return Patterns.ask(roomManager, new RoomManagerActor.FetchAllRooms(), Duration.ofSeconds(3))
                .thenApply(result -> {
                    RoomManagerActor.FetchedAll msg = (RoomManagerActor.FetchedAll) result;
                    try {
                        String json = mapper.writeValueAsString(msg.getAllRooms());
                        return ResponseEntity.ok(json);
                    } catch (JsonProcessingException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error while processing JSON.");
                    }
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof akka.pattern.AskTimeoutException) {
                        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                                .body("Request timed out while fetching rooms.");
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Unexpected error: " + ex.getMessage());
                    }
                })
                .toCompletableFuture();
    }

}
