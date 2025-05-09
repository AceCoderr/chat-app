package com.pingpong.chat.Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ActorRef roomManager;
    private final ActorSystem actorSystem;
    private final Map<String,ActorRef> sessions = new ConcurrentHashMap<>();

    @Autowired
    public ChatWebSocketHandler(ActorRef roomManager, ActorSystem actorSystem) {
        this.roomManager = roomManager;
        this.actorSystem = actorSystem;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection Est");
        ActorRef clientActor = actorSystem.actorOf(ChatClientActor.props(session,roomManager));
        sessions.put(session.getId(), clientActor);
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ActorRef actor = sessions.get(session.getId());
        if (actor != null){
            actor.tell(new ChatClientActor.IncomingMessage(message.getPayload()),ActorRef.noSender());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ActorRef actor = sessions.remove(session.getId());
        String path = session.getUri().getPath();
        String roomId = null;
        // If the roomId is in the path as part of a segment like "roomId=xyz"
        if (path.contains("roomId=")) {
            String[] pathSegments = path.split("/");
            for (String segment : pathSegments) {
                if (segment.startsWith("roomId=")) {
                    roomId = segment.substring("roomId=".length());
                }
            }
        }
        if (actor != null){
            roomManager.tell(new RoomManagerActor.LeaveRoom(roomId,actor),ActorRef.noSender());
            actor.tell(PoisonPill.getInstance(),ActorRef.noSender());
        }
    }
}
