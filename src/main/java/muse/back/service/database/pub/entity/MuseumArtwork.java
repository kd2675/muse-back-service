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
@Table(name = "museum_artwork")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MuseumArtwork extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "museum_artwork_id")
    private Long museumArtworkId;

    @Column(name = "museum_id", nullable = false)
    private Long museumId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "image_url", length = 2048, nullable = false)
    private String imageUrl;

    @Column(name = "moderation_status", length = 20, nullable = false)
    private String moderationStatus;

    public MuseumArtwork(
            Long museumId,
            Long artistId,
            String title,
            String description,
            String fileName,
            String imageUrl,
            String moderationStatus
    ) {
        this.museumId = museumId;
        this.artistId = artistId;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.imageUrl = imageUrl;
        this.moderationStatus = moderationStatus;
    }

    public void updateModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }
}
