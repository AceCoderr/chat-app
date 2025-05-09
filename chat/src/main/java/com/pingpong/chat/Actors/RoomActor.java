package com.pingpong.chat.Actors;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.pingpong.chat.Models.MessageObject;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class RoomActor extends AbstractActor {

    private  String roomName;
    private final Map<ActorRef,String> users = new ConcurrentHashMap<>();

    public RoomActor(String roomName){
        this.roomName = roomName;
    }

    public static class Join{
        public final String username;
        public final ActorRef user;
        public Join(ActorRef user,String username)
        {
            this.username = username;
            this.user = user;

        }
    }

    public static class Leave{
        public final ActorRef user;
        public Leave(ActorRef user)
        {
            this.user = user;
        }
    }

    public static class SendMessage{
        private final MessageObject messageObject;
        public SendMessage(MessageObject messageObject){
            this.messageObject = messageObject;
        }
        public MessageObject getMessageObject() {
            return messageObject;
        }
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Join.class,msg->{
                    System.out.println("In Room Actor Joining Room");
                    users.put(msg.user,msg.username);
                    getContext().watch(msg.user);
                    msg.user.tell("joined",getSelf());
                    for (ActorRef user : users.keySet()){
                        if(user != msg.user){
                            user.tell(new SendMessage(new MessageObject("USER_JOINED",users.get(msg.user),"User has joined",LocalDateTime.now().toString())),getSelf());
                        }
                    }
                })
                .match(Leave.class,msg-> {
                    System.out.println("Leaving room");
                    for (ActorRef user : users.keySet()){
                        if(user != msg.user)
                        {
                            user.tell(new SendMessage(new MessageObject("USER_LEFT",users.get(msg.user),"User has left Chat", LocalDateTime.now().toString())),getSelf());
                        }
                    }
                    users.remove(msg.user);
                    //msg.user.tell(new SendMessage(new MessageObject("USER_LEFT","User","User has left Chat", LocalDateTime.now().toString())),getSelf());
                })
                .match(SendMessage.class,msg->{
                    for (ActorRef user : users.keySet()){
                        user.tell(msg,getSelf());
                    }
                })
                .match(Terminated.class,t-> users.remove(t.getActor()))
                .build();
    }

    public static Props props(String name){
        return Props.create(RoomActor.class);
    }
}
