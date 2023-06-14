package com.chat.service;

import com.chat.entity.User;
import com.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  public final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User register(Long account, String name, String password) {
    try {
      User user = new User();
      user.setAccount(account);
      user.setName(name);
      user.setPassword(password);
      userRepository.save(user);
      return user;
    } catch (Exception e) {
      return null;
    }
  }

  public User login(Long account, String password) {
    try {
      User user = userRepository.findByAccount(account);
      assert password.equals(user.getPassword());
      return user;
    } catch (Exception e) {
      return null;
    }
  }

  public String getUserNameByAccount(Long account) {
    User user = userRepository.findByAccount(account);
    return user.getName();
  }

}
