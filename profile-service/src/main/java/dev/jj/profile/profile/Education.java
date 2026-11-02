package dev.jj.profile.profile;

import jakarta.persistence.*;
import org.hibernate.annotations.MapKeyJdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "education")
class Education {

    @Id
    Long id;
    String institution;
    LocalDate startDate;
    LocalDate endDate;
    int sortOrder;

    @ElementCollection
    @CollectionTable(name = "education_translation", joinColumns = @JoinColumn(name = "education_id"))
    @MapKeyColumn(name = "lang", length = 2)
    @MapKeyJdbcTypeCode(SqlTypes.CHAR)
    Map<String, Text> translations = new HashMap<>();

    @Embeddable
    record Text(String degree, String descriptionMd) {
    }
}
