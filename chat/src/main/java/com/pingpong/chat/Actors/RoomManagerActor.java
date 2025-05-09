package com.pingpong.chat.Actors;

import akka.actor.*;

import akka.japi.pf.ReceiveBuilder;
import com.pingpong.chat.Models.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class RoomManagerActor extends AbstractActor {

    
    public static class Create{
        private final String roomName;
        public Create(String roomName){
            this.roomName = roomName;
        }
    }

    public static class JoinRoom{
        public final String roomId;
        public final ActorRef user;
        public final String username;
        public JoinRoom(String roomId, ActorRef user,String username)
        {
            this.username = username;
            this.roomId = roomId;
            this.user = user;
        }
    }
    public static class LeaveRoom{
        public final String roomId;
        public final ActorRef user;
        public LeaveRoom(String roomId,ActorRef user)
        {
            this.roomId= roomId;
            this.user = user;
        }
    }
    public static class RoomCreated
    {
        private final String roomId;
        public RoomCreated(String roomId){
            this.roomId = roomId;
        }
        public String getRoomId() {
            return roomId;
        }
    }

    public static class FetchAllRooms { }

    public class FetchedAll {
        private final ConcurrentHashMap<String,String> AllRooms;
        public FetchedAll(ConcurrentHashMap<String,String> AllRooms){
            this.AllRooms = AllRooms;
        }
        public ConcurrentHashMap<String,String> getAllRooms() {
            return AllRooms;
        }

    }
    private final ConcurrentHashMap<String,ActorRef> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,String> roomsWithNames = new ConcurrentHashMap<>();

    public static Props props(){
        return Props.create(RoomManagerActor.class);
    }

    public RoomManagerActor() {
        System.out.println("RoomManagerActor created: " + self().path().toString());
    }
    
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Create.class,msg -> {
                    String roomId = UUID.randomUUID().toString();
                    ActorRef roomRef = getContext().actorOf(RoomActor.props(roomId));
                    getContext().watch(roomRef);
                    roomsWithNames.put(roomId,msg.roomName);
                    rooms.put(roomId, roomRef);
                    getSender().tell(new RoomCreated(roomId), getSelf());
                })
                .match(JoinRoom.class,msg->{
                    if (!rooms.containsKey(msg.roomId)){
                        System.out.println(msg.roomId);
                        msg.user.tell(new Status.Failure(new Exception("room does not exists")),getSelf());
                    }
                    else {
                        rooms.get(msg.roomId).forward(new RoomActor.Join(msg.user, msg.username),getContext());
                    }
                })
                .match(LeaveRoom.class,msg->{
                    if (!rooms.containsKey(msg.roomId)){
                        System.out.println(msg.roomId);
                        msg.user.tell(new Status.Failure(new Exception("room does not exists")),getSelf());
                    }
                    else {
                        rooms.get(msg.roomId).forward(new RoomActor.Leave(msg.user),getContext());
                    }
                })
                .match(FetchAllRooms.class,msg->{
                    ConcurrentHashMap<String,String> AllRooms = new ConcurrentHashMap<>(roomsWithNames);
                    getSender().tell(new FetchedAll(AllRooms), getSelf());
                })
                .build();
    }



}
