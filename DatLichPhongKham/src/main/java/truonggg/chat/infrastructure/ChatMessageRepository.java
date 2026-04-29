package truonggg.chat.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import truonggg.chat.domain.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

	/**
	 * Lấy toàn bộ tin nhắn giữa 2 user (cả hai chiều), sắp xếp theo thời gian.
	 */
	@Query("SELECT m FROM ChatMessage m " +
			"WHERE (m.senderUserId = :user1 AND m.receiverUserId = :user2) " +
			"   OR (m.senderUserId = :user2 AND m.receiverUserId = :user1) " +
			"ORDER BY m.sentAt ASC")
	List<ChatMessage> findConversation(@Param("user1") Integer user1, @Param("user2") Integer user2);

	/**
	 * Lấy danh sách userId khác đã từng có tin nhắn (gửi hoặc nhận) với user hiện tại,
	 * sắp xếp theo thời điểm tin nhắn GẦN NHẤT (mới nhất trước).
	 */
    @Query("""
SELECT
CASE
    WHEN m.senderUserId = :currentUserId
        THEN m.receiverUserId
    ELSE m.senderUserId
END AS partnerId
FROM ChatMessage m
WHERE m.senderUserId = :currentUserId
   OR m.receiverUserId = :currentUserId
GROUP BY
CASE
    WHEN m.senderUserId = :currentUserId
        THEN m.receiverUserId
    ELSE m.senderUserId
END
ORDER BY MAX(m.sentAt) DESC
""")
    List<Integer> findConversationPartnerIds(Integer currentUserId);

}


