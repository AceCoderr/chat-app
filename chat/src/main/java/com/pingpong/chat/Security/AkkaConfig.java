package com.pingpong.chat.Security;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.chat.Actors.RoomManagerActor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaConfig {
    @Bean
    public ActorSystem actorSystem(){
        return ActorSystem.create("Chat-system");
    }

    @Bean
    public ActorRef roomManager(ActorSystem actorSystem) {
        return actorSystem.actorOf(RoomManagerActor.props(), "roomManager");
    }

    @Bean
    public ObjectMapper JsonSerializer(){
        return new ObjectMapper();
    }
}
