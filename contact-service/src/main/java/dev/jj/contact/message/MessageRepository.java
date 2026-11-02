package dev.jj.contact.message;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface MessageRepository extends JpaRepository<ContactMessage, UUID> {

    /** Messaggi da notificare ora: non ancora notificati, tentativi non esauriti, backoff scaduto. */
    @Query("""
            select m from ContactMessage m
            where m.notifiedAt is null and m.notifyAttempts < :maxAttempts
              and (m.nextAttemptAt is null or m.nextAttemptAt <= :now)
            order by m.createdAt""")
    List<ContactMessage> findDueForNotification(int maxAttempts, Instant now, Limit limit);

    long countByIpHashAndCreatedAtAfter(String ipHash, Instant after);

    long countByCreatedAtAfter(Instant after);

    /** DELETE diretto in SQL (una query), invece di caricare e cancellare le entity una a una. */
    @Modifying
    @Transactional
    @Query("delete from ContactMessage m where m.createdAt < :before")
    int deleteCreatedBefore(Instant before);
}
