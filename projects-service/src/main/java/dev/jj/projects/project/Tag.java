package dev.jj.projects.project;

import jakarta.persistence.*;

@Entity
@Table(name = "tag")
class Tag {

    @Id
    Long id;
    String name;
    String slug;
}
