package com.pingpong.chat;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.pingpong.chat.Actors.RoomManagerActor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

}
