package muse.back.service.database.pub.entity;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MuseumTest {

    @Test
    void isContentAvailableAt_scheduledBeforeOpening_returnsFalse() {
        Museum museum = new Museum(10L, "Night Archive", null, false, false);
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 12, 0);
        museum.updateCuration("SCHEDULED", null, now.plusHours(1), null, "SALON", "WARM");

        assertThat(museum.isContentAvailableAt(now)).isFalse();
    }

    @Test
    void isContentAvailableAt_scheduledAfterOpening_returnsTrue() {
        Museum museum = new Museum(10L, "Night Archive", null, false, false);
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 12, 0);
        museum.updateCuration("SCHEDULED", null, now.minusSeconds(1), null, "SALON", "WARM");

        assertThat(museum.isContentAvailableAt(now)).isTrue();
    }

    @Test
    void isContentAvailableAt_publishedWithFutureOpening_returnsTrue() {
        Museum museum = new Museum(10L, "Night Archive", null, false, false);
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 12, 0);
        museum.updateCuration("PUBLISHED", null, now.plusDays(1), null, "SALON", "WARM");

        assertThat(museum.isContentAvailableAt(now)).isTrue();
    }
}
