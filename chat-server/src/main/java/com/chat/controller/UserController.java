package com.chat.controller;

import com.chat.entity.User;
import com.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  // 注册
  @PostMapping("/register")
  public User register(
      @RequestParam Long account,
      @RequestParam String name,
      @RequestParam String password
  ) {
    return userService.register(account, name, password);
  }

  // 登录
  @GetMapping("/login")
  public User login(
      @RequestParam Long account,
      @RequestParam String password
  ) {
    return userService.login(account, password);
  }

}
