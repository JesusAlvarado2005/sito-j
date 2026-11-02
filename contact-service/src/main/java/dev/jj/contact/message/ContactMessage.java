package dev.jj.contact.message;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contact_message")
class ContactMessage {

    @Id
    UUID id;
    String name;
    String email;
    String subject;
    String body;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 2)
    String lang;
    String ipHash;

    @Enumerated(EnumType.STRING)
    MessageStatus status;
    Instant notifiedAt;
    int notifyAttempts;
    Instant nextAttemptAt;
    Instant createdAt;
}
