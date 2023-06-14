package com.chat.controller;

import com.chat.entity.ChatRoom;
import com.chat.service.ChatRoomService;
import com.chat.service.ParticipantsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;
  private final ParticipantsService participantsService;

  @Autowired
  public ChatRoomController(ChatRoomService chatRoomService, ParticipantsService participantsService) {
    this.chatRoomService = chatRoomService;
    this.participantsService = participantsService;
  }

//  // 预定会议室
//  @PostMapping("/reserve")
//  public ChatRoom reserveMeetingRoom(
//      @RequestParam Long number,
//      @RequestParam String key,
//      @RequestParam Long leaderAccount) {
//    ChatRoom chatRoom = chatRoomService.creatChatRoom(number, key, leaderAccount);
//    return chatRoom;
//  }

  // 进入会议室
  @PostMapping("/enter")
  public ResponseEntity<String> enterChatRoom(
//      @RequestParam Long number,
//      @RequestParam String key,
      @RequestParam Long userAccount
  ) {
//    ChatRoom chatRoom = chatRoomService.verifyChatRoom(number, key);
//    if (chatRoom != null) {
      return participantsService.enterRoom(userAccount);
//    }
//    return chatRoom;
  }

  // 离开会议室
  @DeleteMapping("/leave")
  public ResponseEntity<String> leaveChatRoom(
//      @RequestParam Long number,
      @RequestParam Long userAccount
  ) {
    return participantsService.leaveRoom(userAccount);
  }

//  // 获取会议室里的用户列表
//  @GetMapping("/inRoom")
//  public List<Long> getUserAccountInRoom(
//      @RequestParam Long roomNumber
//  ) {
//    return participantsService.getUserAccountInRoom(roomNumber);
//  }
//
//  // 获取用户所在会议室
//  @GetMapping("/ofUser")
//  public Long getRoomNumberOfUser(@RequestParam Long userAccount) {
//    return participantsService.getRoomNumberOfUser(userAccount);
//  }

}
