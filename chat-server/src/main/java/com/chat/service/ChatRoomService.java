package com.chat.service;

import com.chat.entity.ChatRoom;
import com.chat.repository.ChatRoomRepository;
import javax.print.DocFlavor.READER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class ChatRoomService {

  public final ChatRoomRepository chatRoomRepository;

  @Autowired
  public ChatRoomService(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

//  // 根据会议号和密码进入之前预定的会议室
//  public ChatRoom verifyChatRoom(Long number, String key) {
//    ChatRoom chatRoom = chatRoomRepository.findByNumber(number);
//    if (chatRoom != null) {
//      if (key.equals(chatRoom.getKey())) {
//        return chatRoom;
//      }
//    }
//    return null;
//  }

//  // 根据会议号获取会议室
//  public ChatRoom getChatRoom(Long number) {
//    return chatRoomRepository.findByNumber(number);
//  }
//
//  // 预定会议室
//  public ChatRoom creatChatRoom(Long number, String key, Long leaderAccount) {
//    chatRoomRepository.createChatRoomByNumberAndKeyAndLeaderAccount(number, key, leaderAccount);
//    ChatRoom chatRoom = chatRoomRepository.findByNumber(number);
//    try {
//      System.out.println(chatRoom);
//    } catch (Exception e) {
//      System.out.println("----------------");
//      e.printStackTrace();
//    }
//    return chatRoom;
//  }
//
//  // 获取会议室领导人
//  public Long getLeaderByNumber(Long number) {
//    return chatRoomRepository.findLeaderAccountByNumber(number);
//  }

}
