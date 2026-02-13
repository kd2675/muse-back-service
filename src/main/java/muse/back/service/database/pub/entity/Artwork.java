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
@Table(name = "artwork")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Artwork extends CommonDateEntity {

    @Id
    @Column(name = "artwork_id")
    private Long artworkId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "artist", length = 100, nullable = false)
    private String artist;

    @Column(name = "category_key", length = 50)
    private String categoryKey;

    @Column(name = "category_label", length = 50)
    private String categoryLabel;

    @Column(name = "description")
    private String description;

    @Column(name = "camera", length = 200)
    private String camera;

    @Column(name = "lens", length = 200)
    private String lens;

    @Column(name = "focal_length", length = 50)
    private String focalLength;

    @Column(name = "aperture", length = 50)
    private String aperture;

    @Column(name = "shutter_speed", length = 50)
    private String shutterSpeed;

    @Column(name = "iso", length = 50)
    private String iso;

    @Column(name = "color_from", length = 20)
    private String colorFrom;

    @Column(name = "color_to", length = 20)
    private String colorTo;
}
