# muse-back-service

Muse 도메인 백엔드 서비스입니다. Contest, Gallery, Home, Overview, Profile 기능을 제공하며 `muse-front-service`의 주요 API를 담당합니다.

## 역할

- 공개 Muse API 제공
- 사용자 개인 갤러리(My Museum) 관리
- 콘테스트 참가, 투표, 랭킹, entry credit 구매 처리
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
- `/api/muse/v1/me/contests`
- `/api/muse/v1/me/entries`
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

현재 테스트는 `ContestServiceTest`, `HomeServiceTest`, `ProfileServiceTest`, `contextLoads` 중심이라 회귀 범위는 제한적입니다.

## 설정 포인트

- Java 21
- Spring Boot 4.0.2
- MySQL + JPA
- OpenFeign + Eureka client
- Caffeine cache
- Actuator 활성화
- 기본 프로필은 `local`
- `.env` 또는 `muse-back-service/.env`에서 DB 계정값을 읽습니다

## 운영 메모

- `spring.servlet.multipart`가 100MB까지 열려 있어 이미지 업로드 연동을 전제로 합니다.
- 이미지 URL 처리는 보통 `image-back-server`와 함께 사용합니다.
- 클라이언트는 보통 Gateway(`cloud-back-server`) 경유 호출을 전제로 합니다.
