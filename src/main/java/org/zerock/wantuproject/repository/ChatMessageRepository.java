package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.wantuproject.entity.ChatMessage;
import org.zerock.wantuproject.entity.ChatRoom;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

//    // 특정 채팅방의 메시지를 시간 순으로 가져오기
//    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    // roomId 타입을 String에서 Long으로 변경
    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomRoomid(Long roomId);

    // 특정 채팅방에 있는 모든 메시지를 삭제하는 쿼리
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage m WHERE m.chatRoom.roomid = :roomId")
    void deleteByChatRoomRoomid(@Param("roomId") Long roomId);



}
