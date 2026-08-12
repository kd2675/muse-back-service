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

import java.time.LocalDateTime;

@Entity
@Table(name = "museum_view_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MuseumViewHistory extends CommonDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "museum_view_history_id")
    private Long museumViewHistoryId;
    @Column(name = "artist_id", nullable = false)
    private Long artistId;
    @Column(name = "museum_id", nullable = false)
    private Long museumId;
    @Column(name = "last_artwork_id")
    private Long lastArtworkId;
    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public MuseumViewHistory(Long artistId, Long museumId, Long lastArtworkId, int progressPercent) {
        this.artistId = artistId;
        this.museumId = museumId;
        record(lastArtworkId, progressPercent);
    }

    public void record(Long lastArtworkId, int progressPercent) {
        this.lastArtworkId = lastArtworkId;
        this.progressPercent = progressPercent;
        this.viewedAt = LocalDateTime.now();
    }
}
