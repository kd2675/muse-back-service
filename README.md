# muse-back-service

Muse 도메인 백엔드 서비스입니다. Contest, Gallery, Home, Overview, Profile과 결제·알림·탐색·작가 관계 기능을 제공하며 `muse-front-service`의 주요 API를 담당합니다.

## 역할

- 공개 Muse API 제공
- 사용자 개인 갤러리(My Museum) 관리
- 콘테스트 참가, 초안 복구, 투표, 랭킹, 심사 결과 공개
- Toss Payments 기반 출품권 주문·승인·취소·웹훅 재검증
- 전시 큐레이션, 예약 공개, 팔로우, 북마크, 관람 기록, 알림, 통합 탐색
- 관리자용 콘테스트/갤러리 운영 API 제공
- `auth-common-core` 기반 사용자 컨텍스트 사용

## 주요 패키지

- `feature/contest`
- `feature/gallery`
- `feature/home`
- `feature/overview`
- `feature/profile`
- `feature/artwork`
- `common`
- `database`

## 포트

| Profile | Port |
|---|---:|
| `local` | `20280` |
| `dev` | `20280` |
| `prod` | `10280` |
| `test` | `30280` |

## 주요 API 영역

- `/api/muse/v1/home`
- `/api/muse/v1/overview`
- `/api/muse/v1/contests`
- `/api/muse/v1/contests/{id}/results`
- `/api/muse/v1/me/contests`
- `/api/muse/v1/me/contests/{id}/draft`
- `/api/muse/v1/me/entries`
- `/api/muse/v1/me/payments`
- `/api/muse/v1/me/notifications`
- `/api/muse/v1/me/gallery`
- `/api/muse/v1/discovery/search`
- `/api/muse/v1/artists`
- `/api/muse/v1/profile`
- `/api/muse/v1/me/museums`
- `/api/muse/v1/gallery/museums`
- `/api/muse/v1/admin/contests`
- `/api/muse/v1/admin/gallery/museums`

## 실행

```bash
./gradlew :muse-back-service:bootRun
./gradlew :muse-back-service:bootRun --args='--spring.profiles.active=local'
./gradlew :muse-back-service:bootRun --args='--spring.profiles.active=dev'
./gradlew :muse-back-service:bootRun --args='--spring.profiles.active=prod'
```

## 빌드 / 테스트

```bash
./gradlew :muse-back-service:compileJava
./gradlew :muse-back-service:test
```

## Related Docs

- `AGENTS.md`
- `AGENTS_MUSE_CONTEST_UNIFIED.md`

현재 테스트는 서비스 단위 테스트와 HTTP 메서드/검증/권한 통합 테스트를 포함합니다. Gateway를 포함한 로그인 E2E, 실제 Toss 테스트키 승인·웹훅, 파일 저장소 장애 복구는 배포 환경에서 별도 검증해야 합니다.

## 설정 포인트

- Java 21
- Spring Boot 4.0.2
- MySQL + JPA
- OpenFeign + Eureka client
- Caffeine cache
- Actuator 활성화
- 기본 프로필은 `local`
- `.env` 또는 `muse-back-service/.env`에서 DB 계정값을 읽습니다
- 결제 활성화에는 `MUSE_TOSS_ENABLED=true`, `MUSE_TOSS_CLIENT_KEY`, `MUSE_TOSS_SECRET_KEY`가 필요합니다. 테스트키와 라이브키를 환경별로 분리합니다.

## 운영 메모

- `spring.servlet.multipart`가 100MB까지 열려 있어 이미지 업로드 연동을 전제로 합니다.
- 이미지 URL 처리는 보통 `image-back-server`와 함께 사용합니다.
- 클라이언트는 보통 Gateway(`cloud-back-server`) 경유 호출을 전제로 합니다.
- 기존 운영 DB는 코드 배포 전에 `src/main/resources/db/migration/2026-08-12-muse-contest-integrity.sql`과 `2026-08-12-muse-product-expansion.sql`을 순서대로 한 번 적용해야 합니다.
- 일반 결제 웹훅은 수신 본문을 신뢰하지 않고 Toss 결제 조회 API로 주문번호·금액·상태를 다시 확인합니다.
- 출품권 직접 지급 API는 테스트 프로필에서만 활성화되며 운영 결제는 저장된 주문 금액 검증을 통과해야 합니다.
- 로컬에서 마이그레이션 전 DB를 조회만 할 때는 `MUSE_IMAGE_CLEANUP_ENABLED=false`가 기본값이며, 운영에서는 정리 outbox를 활성화합니다.
