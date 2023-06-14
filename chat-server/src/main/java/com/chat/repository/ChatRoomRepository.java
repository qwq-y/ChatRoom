package com.chat.repository;

import com.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  ChatRoom findByNumber(Long number);

  @Query(value = "select leader_account from chat_room where number = :number", nativeQuery = true)
  Long findLeaderAccountByNumber(@Param("number") Long number);

  @Query(value = "insert into chat_room (number, key, leader_account) "
      + "values (:number , :key, :account)", nativeQuery = true)
  ChatRoom createChatRoomByNumberAndKeyAndLeaderAccount(
      @Param("number") Long number,
      @Param("key") String key,
      @Param("account") Long leaderAccount);
}
