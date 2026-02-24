package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

@Entity
@Table(name = "museum")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Museum extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "museum_id")
    private Long museumId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;

    public Museum(
            Long artistId,
            String name,
            String description,
            boolean isPublic,
            boolean isFeatured
    ) {
        this.artistId = artistId;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.isFeatured = isFeatured;
    }

    public void updateByOwner(String name, String description, boolean isPublic) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    public void updateFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public void updateVisibility(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
