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

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "moderation_status", length = 20, nullable = false)
    private String moderationStatus;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "room_label", length = 80)
    private String roomLabel;

    @Column(name = "focal_x", nullable = false)
    private int focalX;

    @Column(name = "focal_y", nullable = false)
    private int focalY;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "audio_transcript", length = 4000)
    private String audioTranscript;

    @Column(name = "lighting_preset", length = 30, nullable = false)
    private String lightingPreset;

    public MuseumArtwork(
            Long museumId,
            Long artistId,
            String title,
            String description,
            String fileName,
            String moderationStatus
    ) {
        this(museumId, artistId, title, description, fileName, null, moderationStatus);
    }

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
        this.sortOrder = 0;
        this.focalX = 50;
        this.focalY = 50;
        this.lightingPreset = "WARM";
    }

    public void updateModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public void updateCuration(
            String title,
            String description,
            int sortOrder,
            String roomLabel,
            int focalX,
            int focalY,
            String audioUrl,
            String audioTranscript,
            String lightingPreset
    ) {
        this.title = title;
        this.description = description;
        this.sortOrder = sortOrder;
        this.roomLabel = roomLabel;
        this.focalX = focalX;
        this.focalY = focalY;
        this.audioUrl = audioUrl;
        this.audioTranscript = audioTranscript;
        this.lightingPreset = lightingPreset;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
