package com.chat.service;

import com.chat.entity.Participants;
import com.chat.entity.User;
import com.chat.repository.ParticipantsRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ParticipantsService {

  public final ParticipantsRepository participantsRepository;

  @Autowired
  public ParticipantsService(ParticipantsRepository participantsRepository) {
    this.participantsRepository = participantsRepository;
  }

  // 获取会议室里的用户列表
  public List<Long> getUserAccountInRoom(Long roomNumber) {
    List<Participants> participantsList = participantsRepository.findByRoomNumber(roomNumber);
    List<Long> userAccountList = participantsList.stream()
        .map(Participants::getUserAccount)
        .collect(Collectors.toList());
    return userAccountList;
  }

  // 进入会议室时增加表项
  public ResponseEntity<String> enterRoom(Long userAccount, Long roomNumber) {
    participantsRepository.insertParticipants(userAccount, roomNumber);
    return ResponseEntity.ok(userAccount + "entered");
  }

  // 离开会议室时删除表项
  public ResponseEntity<String> leaveRoom(Long userAccount, Long roomNumber) {
    participantsRepository.deleteParticipantsByUserAccountAndRoomNumber(userAccount, roomNumber);
    return ResponseEntity.ok(userAccount + "left");
  }

  // 根据用户账户查找其所在的会议号码
  public Long getRoomNumberOfUser(Long userAccount) {
    return participantsRepository.findRoomNumberByUserAccount(userAccount);
  }

}
