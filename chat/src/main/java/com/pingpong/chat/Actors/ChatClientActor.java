package com.pingpong.chat.Actors;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.chat.Models.MessageObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

public class ChatClientActor extends AbstractActor {
    private final WebSocketSession session;
    private final ActorRef roomManager;
    private ActorRef room;

    private final ObjectMapper mapper;

    public ChatClientActor(WebSocketSession session,ActorRef roomManager){
        this.mapper = new ObjectMapper();
        this.session = session;
        this.roomManager = roomManager;
    }

//    public static class LeaveRoom{
//        public final String roomName;
//        public final ActorRef user;
//        public LeaveRoom(String roomName,ActorRef user)
//        {
//            this.roomName= roomName;
//            this.user = user;
//        }
//    }

    public static class IncomingMessage{
        public final String textMessage;
        public IncomingMessage(String textMessage)
        {
            this.textMessage = textMessage;
        }
    }
    public static Props props(WebSocketSession session, ActorRef roomMangerActor){
        return Props.create(ChatClientActor.class,() -> new ChatClientActor(session,roomMangerActor));
    }

    @Override
    public void preStart() throws Exception {
        String roomName = getRoomNameFromSession();
        String username = extractUsernameFromQuery(session.getUri().getQuery());
        roomManager.tell(new RoomManagerActor.JoinRoom(roomName,getSelf(),username),getSelf());
    }

    private String extractUsernameFromQuery(String query) {
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "username".equals(pair[0])) {
                    try {
                        return URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        System.out.println("Error decoding username");
                        return "Anonymous";
                    }
                }
            }
        }
        return "Anonymous"; // Default username if not provided
    }
    private String getRoomNameFromSession() {
        String path = session.getUri().getPath();
        String roomName = null;
        // If the roomId is in the path as part of a segment like "roomId=xyz"
        if (path.contains("roomId=")) {
            String[] pathSegments = path.split("/");
            for (String segment : pathSegments) {
                if (segment.startsWith("roomId=")) {
                    roomName = segment.substring("roomId=".length());
                }
            }
        }
        return roomName;
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .matchEquals("joined",msg->{
                    room = getSender();
                })
                .match(Status.Failure.class,err->{
                    session.sendMessage(new TextMessage("Error"+err.cause().getMessage()));
                    session.close();
                })
                .match(RoomActor.SendMessage.class,msg->{
                    String json = mapper.writeValueAsString(msg.getMessageObject());
                    session.sendMessage(new TextMessage(json));
                })
                .match(IncomingMessage.class,msg->{
                    if(room != null){
                        System.out.println(msg.textMessage);
                        MessageObject message = mapper.readValue(msg.textMessage,MessageObject.class);
                        room.tell(new RoomActor.SendMessage(message),getSelf());
                    }
                })
                .build();
    }
}
