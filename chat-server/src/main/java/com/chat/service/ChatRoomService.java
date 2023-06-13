package com.chat.service;

import com.chat.entity.ChatRoom;
import com.chat.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomService {

  public final ChatRoomRepository chatRoomRepository;

  @Autowired
  public ChatRoomService(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  public ChatRoom findChatRoom(Long number, String key) {
    ChatRoom chatRoom = chatRoomRepository.findByNumber(number);
    if (chatRoom != null) {
      if (key.equals(chatRoom.getKey())) {
        return chatRoom;
      }
    }
    return null;
  }

  public Long getLeaderByNumber(Long number) {
    return chatRoomRepository.findLeaderAccountByNumber(number);
  }

}
