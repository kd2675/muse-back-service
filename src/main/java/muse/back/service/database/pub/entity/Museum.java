package muse.back.service.database.pub.entity;

import java.time.LocalDateTime;

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

    @Column(name = "publish_status", length = 20, nullable = false)
    private String publishStatus;

    @Column(name = "cover_artwork_id")
    private Long coverArtworkId;

    @Column(name = "opening_at")
    private LocalDateTime openingAt;

    @Column(name = "curator_note", length = 2000)
    private String curatorNote;

    @Column(name = "layout_preset", length = 30, nullable = false)
    private String layoutPreset;

    @Column(name = "lighting_preset", length = 30, nullable = false)
    private String lightingPreset;

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
        this.publishStatus = isPublic ? "PUBLISHED" : "DRAFT";
        this.layoutPreset = "SALON";
        this.lightingPreset = "WARM";
    }

    public void updateMetadata(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void updateFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public void updateVisibility(boolean isPublic) {
        this.isPublic = isPublic;
        this.publishStatus = isPublic ? "PUBLISHED" : "DRAFT";
    }

    public boolean isContentAvailableAt(LocalDateTime now) {
        if (!isPublic) {
            return false;
        }
        if ("PUBLISHED".equals(publishStatus)) {
            return true;
        }
        return "SCHEDULED".equals(publishStatus)
                && openingAt != null
                && !now.isBefore(openingAt);
    }

    public void clearCoverArtworkIf(Long museumArtworkId) {
        if (museumArtworkId != null && museumArtworkId.equals(coverArtworkId)) {
            this.coverArtworkId = null;
        }
    }

    public void updateCuration(
            String publishStatus,
            Long coverArtworkId,
            LocalDateTime openingAt,
            String curatorNote,
            String layoutPreset,
            String lightingPreset
    ) {
        this.publishStatus = publishStatus;
        this.isPublic = "PUBLISHED".equals(publishStatus) || "SCHEDULED".equals(publishStatus);
        this.coverArtworkId = coverArtworkId;
        this.openingAt = openingAt;
        this.curatorNote = curatorNote;
        this.layoutPreset = layoutPreset;
        this.lightingPreset = lightingPreset;
    }
}
