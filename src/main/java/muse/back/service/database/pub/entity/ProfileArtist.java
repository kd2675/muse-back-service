package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

@Entity
@Table(name = "profile_artist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileArtist extends CommonDateEntity {

    @Id
    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "tagline", length = 255)
    private String tagline;

    @Column(name = "profile_color", length = 20)
    private String profileColor;

    public ProfileArtist(Long artistId, Long userId, String name, String tagline, String profileColor) {
        this.artistId = artistId;
        this.userId = userId;
        this.name = name;
        this.tagline = tagline;
        this.profileColor = profileColor;
    }
}
