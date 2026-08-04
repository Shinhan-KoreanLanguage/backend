# Korean Language Learning Backend

외국인 대상 한국어 발음 교정 학습 서비스의 백엔드 프로젝트입니다.
Spring Boot와 JPA를 기반으로 구성되어 있습니다.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT (jjwt 0.13.0)
- OAuth2 (Google, Facebook)
- SpringDoc OpenAPI 3.0.3 (Swagger)
- Lombok
- Gradle

## Requirements

- JDK 21 이상
- MySQL 8 이상

Gradle은 프로젝트에 포함된 Wrapper(`./gradlew`)를 사용하므로 별도 설치가 필요하지 않습니다.

## Getting Started

1. `.env.example`을 복사해 `.env`를 만들고 값을 채웁니다.

```bash
cp .env.example .env
```

2. MySQL에 데이터베이스를 생성합니다.

```sql
CREATE DATABASE shinhan_capstone CHARACTER SET utf8mb4;
```

3. 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

- 서버: `http://localhost:8080`
- API 문서(Swagger): `http://localhost:8080/swagger-ui.html`

## Project Structure

```
src/main/java/com/daehanforeigner/capstone/
├── domain/
│   ├── admin/
│   │   ├── controller/
│   │   ├── dto/
│   │   └── service/
│   ├── user/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   ├── social_account/
│   ├── learning_content/
│   ├── content_category/
│   ├── pronunciation_attempt/
│   ├── standard_pronunciation/
│   ├── phoneme_score/
│   ├── audio_file/
│   ├── feedback/
│   ├── wrong_answer/
│   ├── game_result/
│   ├── game_field_table/
│   └── ranking/
└── global/
    ├── config/
    ├── dto/
    ├── entity/
    ├── exception/
    ├── jwt/
    ├── oauth/
    ├── rsdata/
    ├── security/
    └── storage/
```

- `domain`: 도메인 단위 패키지. 각 도메인 안에서 `controller` / `service` / `repository` / `dto` / `entity`로 분리합니다.
- `global`: 여러 도메인이 공통으로 사용하는 설정과 인프라 코드
    - `config`: Security, Web 등 설정 클래스
    - `dto`: 공통 응답 DTO (페이징 등)
    - `entity`: 공통 엔티티 (생성일시·수정일시)
    - `exception`: 에러 코드와 전역 예외 처리
    - `jwt`: 토큰 발급·검증, 인증 필터
    - `oauth`: 소셜 로그인 클라이언트
    - `rsdata`: 공통 응답 포맷
    - `security`: 인증·인가 실패 핸들러
    - `storage`: 파일 저장

## API Response Format

모든 API는 `RsData` 포맷으로 응답합니다.

**성공**

```json
{
  "success": true,
  "data": { },
  "error": null,
  "timestamp": "2026-08-04T12:00:00"
}
```

**실패**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "CONTENT_NOT_FOUND",
    "message": "존재하지 않는 학습 콘텐츠입니다."
  },
  "timestamp": "2026-08-04T12:00:00"
}
```

- 에러는 `ErrorCode` enum에 정의하며, HTTP 상태 코드 · 코드 문자열 · 메시지를 함께 관리합니다.
- 비즈니스 예외는 `CustomException`으로 던지고, `GlobalExceptionHandler`가 위 포맷으로 변환합니다.

## Authentication

- JWT 기반 인증 (Access Token / Refresh Token)
- 인증이 필요한 요청은 `Authorization: Bearer {accessToken}` 헤더 사용
- 권한 분리: `USER` / `ADMIN`
    - `/api/v1/auth/**`: 인증 없이 접근 가능
    - `/api/v1/admin/**`: `ADMIN` 권한 필요
    - 그 외: 인증 필요

## Team Convention (필수!)

### 브랜치

```
Feat/#{이슈번호}
```

### 커밋 메시지

```
[Feat/#{이슈번호}] {타입}: {작업 내용}
```

예시

```
[Feat/#10] Feat: 관리자 학습 콘텐츠 일괄 삭제 구현
[Feat/#10] Fix: 파라미터 누락 시 500 -> 400 에러로 응답하도록 수정
[Feat/#10] Chore: 페이지 크기 상한 설정
```

타입: `Feat` / `Fix` / `Refactor` / `Chore` / `Docs` / `Test`

### 코드 규칙

- 계층 책임을 분리합니다. Controller는 요청·응답 변환만, 비즈니스 로직은 Service, DB 접근은 Repository가 담당합니다.
- 엔티티를 요청·응답에 직접 사용하지 않고 DTO로 변환합니다.
    - 엔티티 → DTO는 DTO의 `from()`, DTO → 엔티티는 `toEntity()`를 사용합니다.
- 엔티티에 `@Setter`를 열지 않고, 의도가 드러나는 메서드로 상태를 변경합니다. (`updateProfile()`, `withdraw()` 등)
- DTO 검증은 `@Valid`와 검증 어노테이션으로 처리하고, 메시지는 사용자에게 그대로 보여도 되는 문구로 작성합니다.
- 예외는 `CustomException` + `ErrorCode`로 던집니다. 내부 예외 메시지를 응답에 노출하지 않습니다.
- `@PathVariable` / `@RequestParam`은 이름을 명시합니다.

```java
@PathVariable("contentId") Long contentId
@RequestParam(value = "keyword", required = false) String keyword
```

- 조회 전용 서비스는 `@Transactional(readOnly = true)`를 기본으로 두고, 변경 메서드에만 `@Transactional`을 붙입니다.
- 목록 조회는 필터·페이징을 DB에서 처리합니다. 전체 조회 후 애플리케이션에서 거르지 않습니다.
- 민감 정보(비밀번호, 리프레시 토큰)는 응답 DTO에 포함하지 않습니다.
- 환경변수와 비밀값은 `.env`로 관리하며 커밋하지 않습니다.