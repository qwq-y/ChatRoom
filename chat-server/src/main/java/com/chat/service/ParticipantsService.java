package com.chat.service;

import com.chat.entity.Participants;
import com.chat.entity.User;
import com.chat.repository.ParticipantsRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantsService {

  public final ParticipantsRepository participantsRepository;

  @Autowired
  public ParticipantsService(ParticipantsRepository participantsRepository) {
    this.participantsRepository = participantsRepository;
  }

  public List<Long> getUserAccountInRoom(Long roomNumber) {
    List<Participants> participantsList = participantsRepository.findByRoomNumber(roomNumber);
    List<Long> userAccountList = participantsList.stream()
        .map(Participants::getUserAccount)
        .collect(Collectors.toList());
    return userAccountList;
  }
}
