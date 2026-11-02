package dev.jj.profile.profile;

import jakarta.persistence.*;

@Entity
@Table(name = "skill")
class Skill {

    @Id
    Long id;
    String name;
    Short level;
    int sortOrder;
}
