package dev.jj.contact.message;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface MessageRepository extends JpaRepository<ContactMessage, UUID> {

    /** Messaggi per cui l'email di notifica non è ancora partita, dal più vecchio. */
    List<ContactMessage> findByNotifiedAtIsNullOrderByCreatedAtAsc(Limit limit);
}
