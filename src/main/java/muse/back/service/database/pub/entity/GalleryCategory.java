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
@Table(name = "gallery_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GalleryCategory extends CommonDateEntity {

    @Id
    @Column(name = "category_key", length = 50)
    private String categoryKey;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @Column(name = "color_from", length = 20)
    private String colorFrom;

    @Column(name = "color_to", length = 20)
    private String colorTo;
}
