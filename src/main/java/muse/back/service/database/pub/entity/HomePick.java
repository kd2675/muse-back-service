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
@Table(name = "home_pick")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomePick extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "home_pick_id")
    private Long homePickId;

    @Column(name = "artwork_id", nullable = false)
    private Long artworkId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
