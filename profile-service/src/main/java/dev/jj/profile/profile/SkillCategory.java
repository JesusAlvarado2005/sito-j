package dev.jj.profile.profile;

import jakarta.persistence.*;
import org.hibernate.annotations.MapKeyJdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "skill_category")
class SkillCategory {

    @Id
    Long id;
    String code;
    int sortOrder;

    @ElementCollection
    @CollectionTable(name = "skill_category_translation", joinColumns = @JoinColumn(name = "category_id"))
    @MapKeyColumn(name = "lang", length = 2)
    @MapKeyJdbcTypeCode(SqlTypes.CHAR)
    Map<String, Text> translations = new HashMap<>();

    @OneToMany
    @JoinColumn(name = "category_id")
    @OrderBy("sortOrder")
    List<Skill> skills = new ArrayList<>();

    @Embeddable
    record Text(String name) {
    }
}
