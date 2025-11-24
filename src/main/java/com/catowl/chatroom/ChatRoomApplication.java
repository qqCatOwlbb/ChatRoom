package com.catowl.chatroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class ChatRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatRoomApplication.class, args);
    }

}
