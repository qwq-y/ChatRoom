package com.chat.repository;

import com.chat.entity.Participants;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantsRepository extends JpaRepository<Participants, Long> {

  @Query(value = "select * from participants where room_number = :number", nativeQuery = true)
  List<Participants> findByRoomNumber(@Param("number") Long roomNumber);
}
