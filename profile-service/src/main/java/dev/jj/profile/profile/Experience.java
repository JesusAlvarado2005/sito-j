package dev.jj.profile.profile;

import jakarta.persistence.*;
import org.hibernate.annotations.MapKeyJdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "experience")
class Experience {

    @Id
    Long id;
    String company;
    String companyUrl;
    String location;
    LocalDate startDate;
    LocalDate endDate;
    int sortOrder;

    @ElementCollection
    @CollectionTable(name = "experience_translation", joinColumns = @JoinColumn(name = "experience_id"))
    @MapKeyColumn(name = "lang", length = 2)
    @MapKeyJdbcTypeCode(SqlTypes.CHAR)
    Map<String, Text> translations = new HashMap<>();

    @Embeddable
    record Text(String role, String descriptionMd) {
    }
}
