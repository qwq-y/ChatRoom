package com.chat.controller;

import com.chat.entity.Participants;
import com.chat.entity.User;
import com.chat.service.ParticipantsService;
import com.chat.service.UserService;
import java.util.List;
import javax.print.DocFlavor.READER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/participants")
public class ParticipantsController {

  private final ParticipantsService participantsService;

  @Autowired
  public ParticipantsController(ParticipantsService participantsService) {
    this.participantsService = participantsService;
  }

  @GetMapping("/inRoom")
  public List<Long> getUserAccountInRoom(
      @RequestParam Long roomNumber
  ) {
    return participantsService.getUserAccountInRoom(roomNumber);
  }

}
