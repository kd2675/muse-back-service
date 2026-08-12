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
@Table(name = "artist_follow")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistFollow extends CommonDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_follow_id")
    private Long artistFollowId;
    @Column(name = "follower_artist_id", nullable = false)
    private Long followerArtistId;
    @Column(name = "followed_artist_id", nullable = false)
    private Long followedArtistId;

    public ArtistFollow(Long followerArtistId, Long followedArtistId) {
        this.followerArtistId = followerArtistId;
        this.followedArtistId = followedArtistId;
    }
}
