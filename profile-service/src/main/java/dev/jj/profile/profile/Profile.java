package dev.jj.profile.profile;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.MapKeyJdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Profilo personale: una sola riga (id = 1). I testi stanno in profile_translation. */
@Entity
@Table(name = "profile")
class Profile {

    @Id
    Short id;
    String fullName;
    String publicEmail;
    String location;
    String avatarUrl;
    Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "profile_translation", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "lang", length = 2)
    @MapKeyJdbcTypeCode(SqlTypes.CHAR)
    Map<String, Text> translations = new HashMap<>();

    @Embeddable
    record Text(String headline, String bioMd, String cvUrl) {
    }
}
