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
@Table(name = "museum_bookmark")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MuseumBookmark extends CommonDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "museum_bookmark_id")
    private Long museumBookmarkId;
    @Column(name = "artist_id", nullable = false)
    private Long artistId;
    @Column(name = "museum_id", nullable = false)
    private Long museumId;

    public MuseumBookmark(Long artistId, Long museumId) {
        this.artistId = artistId;
        this.museumId = museumId;
    }
}
