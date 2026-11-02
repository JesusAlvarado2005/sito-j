package dev.jj.profile.profile;

import jakarta.persistence.*;

@Entity
@Table(name = "social_link")
class SocialLink {

    @Id
    Long id;
    String type;
    String url;
    String label;
    int sortOrder;
}
