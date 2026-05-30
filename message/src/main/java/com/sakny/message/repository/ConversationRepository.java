package com.sakny.message.repository;

import com.sakny.message.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find a conversation between two users (order-independent).
     * Participants are always stored with the lower ID first.
     */
    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.participantOne.id = :minId AND c.participantTwo.id = :maxId)
    """)
    Optional<Conversation> findByParticipants(
        @Param("minId") Long minId,
        @Param("maxId") Long maxId
    );

    /**
     * All conversations for a user, ordered by the latest message timestamp.
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        LEFT JOIN FETCH c.participantOne
        LEFT JOIN FETCH c.participantTwo
        WHERE c.participantOne.id = :userId OR c.participantTwo.id = :userId
        ORDER BY c.updatedAt DESC
    """)
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    /**
     * Paginated variant — does not use JOIN FETCH to avoid HHH90003004 in-memory pagination warning.
     */
    @Query(value = """
        SELECT c FROM Conversation c
        WHERE c.participantOne.id = :userId OR c.participantTwo.id = :userId
        ORDER BY c.updatedAt DESC
    """)
    Page<Conversation> findAllByUserIdPageable(@Param("userId") Long userId, Pageable pageable);
}
