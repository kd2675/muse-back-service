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
@Table(name = "artwork_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtworkAsset extends CommonDateEntity {

    @Id
    @Column(name = "artwork_id")
    private Long artworkId;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    public ArtworkAsset(Long artworkId, String fileName) {
        this.artworkId = artworkId;
        this.fileName = fileName;
    }
}
