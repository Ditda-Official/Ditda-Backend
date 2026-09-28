# 코드 작성 규칙

Ditda-Backend의 코드를 작성할 때 따르는 규칙입니다.

이슈·브랜치·커밋·PR 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고해주세요.

---

## 목차

1. [코드 스타일](#1-코드-스타일)
2. [패키지와 레이어](#2-패키지와-레이어)
3. [API](#3-api)
4. [DTO와 엔티티](#4-dto와-엔티티)
5. [DB 마이그레이션](#5-db-마이그레이션)
6. [이벤트와 비동기 처리](#6-이벤트와-비동기-처리)
7. [로그와 메트릭](#7-로그와-메트릭)

---

## 1. 코드 스타일

### 1.1 포맷

네이버 Java 코딩 컨벤션 기반의 Checkstyle 규칙([`config/checkstyle/`](config/checkstyle/))을 씁니다.
`./gradlew build`에서 함께 검사되며, 경고가 한 건이라도 있으면 빌드가 실패합니다.

**IntelliJ 설정**

1. `Settings → Editor → Code Style → Java → ⚙️ → Import Scheme`에서 `config/intellij/intellij-formatter.xml`을 불러옵니다.
2. CheckStyle-IDEA 플러그인에 `config/checkstyle/checkstyle-rules.xml`을 등록하고,
   `suppressionFile` 속성 값을 `$PROJECT_DIR$/config/checkstyle/checkstyle-suppressions.xml`로 지정합니다.

검사 제외가 꼭 필요하면 `// @checkstyle:off` ~ `// @checkstyle:on`으로 감싸고, PR에 이유를 적습니다.

### 1.2 네이밍

**클래스** — 역할을 접미사로 드러냅니다.

| 접미사                    | 역할               | 예시                             |
|------------------------|------------------|--------------------------------|
| `Controller`           | HTTP 요청 처리       | `InstructorRevisionController` |
| `Facade`               | 여러 Service 조합    | `InstructorRevisionFacade`     |
| `Service`              | 비즈니스 로직          | `InstructorRevisionService`    |
| `Repository`           | 데이터 접근           | `CommissionRepository`         |
| `Mapper`               | 빈이 필요한 응답 변환     | `RevisionMapper`               |
| `Scheduler`            | `@Scheduled` 진입점 | `CommissionScheduler`          |
| `Event`                | 도메인 이벤트          | `RevisionRequestedEvent`       |
| `ErrorCode`            | 에러 코드 enum       | `RevisionErrorCode`            |
| `Request` / `Response` | 요청·응답 DTO        | `RevisionCreateRequest`        |
| `Properties`           | 설정 바인딩           | `WatermarkProperties`          |

**메서드**

| 용도    | 접두사                             | 예시                                           |
|-------|---------------------------------|----------------------------------------------|
| 조회    | `get`, `find`                   | `getOwnedCommission()`, `findThumbnail()`    |
| 생성    | `create`                        | `NotificationOutbox.create()`                |
| 수정    | `update` 또는 의미 있는 동사            | `cancel()`, `complete()`, `markSent()`       |
| 삭제    | `delete`                        | `deleteExpiredRefreshTokens()`               |
| 검증    | `validate` — 실패 시 예외, 반환값 없음    | `validateRevisable()`                        |
| 상태 확인 | `is`, `has`, `can` — boolean 반환 | `isCancelled()`, `isRevisionLimitExceeded()` |

`Optional`을 반환하는 조회는 이름에서 드러나게 합니다 (`findByUsernameIfExists()`).

**변수** — camelCase, boolean은 `is`/`has`/`can`, 컬렉션은 복수형(`applications`)을 씁니다.

---

## 2. 패키지와 레이어

**도메인 단위로 패키지를 나눕니다** (`domain/<도메인>/`). 하위 패키지는 필요한 것만 만들고, 이름은 [1.2 네이밍](#12-네이밍)의
클래스 역할과 맞춥니다.
특정 도메인에만 쓰이는 설정은 그 도메인의 `config/`에, 여러 도메인이 함께 쓰는 설정은 `global/config`에 둡니다.

```
Controller → (Facade) → Service → Repository
```

| 레이어            | 하는 일                                  |
|----------------|---------------------------------------|
| **Controller** | 요청 바인딩·검증, 인증 주체 추출, `ApiResponse` 래핑 |
| **Facade**     | 여러 Service 조합, 도메인 이벤트 발행, 응답 조립      |
| **Service**    | 한 도메인의 비즈니스 로직                        |
| **Repository** | 데이터 접근                                |

- **Facade는 여러 Service를 묶어야 할 때만 만듭니다.** Service 하나로 끝나는 요청은 Controller가 Service를 바로 호출합니다.
- Facade는 `@Component`, Service는 `@Service`를 붙입니다.
- 응답 변환은 엔티티 값만으로 되면 DTO의 정적 팩토리 `from()`을, S3 URL 생성처럼 빈이 필요하면 `@Component` Mapper를 씁니다.

---

## 3. API

### 3.1 URI

```
/api/v1/{주체}/{리소스}/{식별자}/{하위 리소스 또는 동작}
```

| 주체            | 접근 권한             | 예시                                                        |
|---------------|-------------------|-----------------------------------------------------------|
| `instructors` | `INSTRUCTOR`      | `GET /api/v1/instructors/commissions`                     |
| `designers`   | `DESIGNER`        | `POST /api/v1/designers/commissions/{commissionId}/apply` |
| `admin`       | `ADMIN`           | `GET /api/v1/admin/designers/{designerId}/portfolios`     |
| (없음)          | 사용자 공용            | `GET /api/v1/commissions/{commissionId}`                  |
| `internal`    | 서버 간 호출, 별도 인증 필터 | `POST /api/v1/internal/watermarks/callback`               |

- 리소스는 **복수형 명사**, 경로는 **kebab-case** (`draft-submissions`, `presigned-url`)
- 경로 변수는 `{commissionId}`처럼 `camelCase + Id`
- 상태를 바꾸는 동작은 **POST + 동사 하위 경로** (`/apply`, `/cancel`, `/select`, `/finalize`)

### 3.2 응답 형식

모든 응답은 [`ApiResponse`](src/main/java/ditda/backend/global/apipayload/response/ApiResponse.java)로 감쌉니다.

```jsonc
// 성공
{
  "success": true,
  "code": "SUCCESS",
  "message": "시안 상세 정보 조회 성공",
  "result": { ... },
  "error": null,
  "timestamp": "2026-09-28 14:03:21"
}

// 실패
{
  "success": false,
  "code": "REVISION_409_02",
  "message": "수정 횟수를 모두 사용했습니다.",
  "result": null,
  "error": "수정 횟수를 모두 사용했습니다.",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "timestamp": "2026-09-28 14:03:21"
}
```

- 성공 응답은 `ApiResponse.onSuccess(...)`로 만듭니다.
- 실패 응답은 직접 만들지 않습니다. 예외를 던지면 [
  `ExceptionAdvice`](src/main/java/ditda/backend/global/apipayload/exception/handler/ExceptionAdvice.java)가 변환합니다.
- `traceId`는 실패 응답에만 들어갑니다.

### 3.3 에러 코드

**형식**: `{도메인}_{HTTP 상태}_{순번 2자리}` (예: `REVISION_400_01`, `REVISION_409_02`)

- 순번은 (도메인, HTTP 상태) 조합마다 `01`부터 매깁니다.
- 도메인 에러는 `domain/<도메인>/exception/<도메인>ErrorCode`에, 여러 도메인이 함께 쓰는 에러는 [
  `GeneralErrorCode`](src/main/java/ditda/backend/global/apipayload/code/GeneralErrorCode.java)에 둡니다.
- 메시지는 사용자에게 그대로 보이므로 한국어로 씁니다.

**예외 종류**

| 상황                      | 던지는 예외                                              | 메시지                |
|-------------------------|-----------------------------------------------------|--------------------|
| 비즈니스 규칙 위반 (사용자가 알아야 함) | `GeneralException(XxxErrorCode.YYY)`                | ErrorCode의 한국어 메시지 |
| 있을 수 없는 상태 (프로그래밍 오류)   | `IllegalStateException`, `IllegalArgumentException` | 영어 산문 + 값          |

### 3.4 Swagger

- 컨트롤러에는 `@Tag`, 메서드에는 `@Operation(summary, description)`을 붙입니다.
- `description` 앞에 화면 이름을 붙입니다. (예: `description = "**[수정]** 시안 수정 요청을 생성합니다."`)
- 호출 순서가 있는 API는 `description`에 순서를 적습니다.
- 요청·응답 DTO의 필드에는 `@Schema(description, example)`을 붙입니다.

---

## 4. DTO와 엔티티

### 4.1 DTO

- 요청·응답 모두 **record**로 작성합니다. 이름은 `{기능}Request`, `{기능}Response`, 위치는 `dto/request`, `dto/response`입니다.
- 검증 메시지는 한국어로 쓰고, 중첩 DTO와 컬렉션 요소는 `@Valid`로 검증을 전파합니다.
- 응답은 정적 팩토리 `from()`으로 만듭니다. 빈이 필요하면 Mapper를 씁니다 ([2](#2-패키지와-레이어) 참고).

### 4.2 엔티티

| 항목        | 규칙                                                                                                          |
|-----------|-------------------------------------------------------------------------------------------------------------|
| 클래스 어노테이션 | `@Getter`, `@Builder`, `@AllArgsConstructor(access = PRIVATE)`, `@NoArgsConstructor(access = PROTECTED)` 조합 |
| 공통 컬럼     | `BaseEntity`를 상속해 `created_at`, `updated_at` 자동 관리                                                          |
| setter    | 금지. 상태 변경은 `cancel()`, `markSent()`처럼 의미가 드러나는 메서드로                                                         |
| 생성        | `create`로 시작하는 정적 팩토리 (`create()`, `createFirstRound()`)                                                    |
| 비즈니스 규칙   | 엔티티 안에 둡니다 (`validateRevisable()`, `isRevisionLimitExceeded()`)                                             |
| PK·FK 타입  | `Long`                                                                                                      |
| 연관관계      | `@ManyToOne(fetch = FetchType.LAZY)` 단방향만. `@OneToMany`는 쓰지 않음                                              |
| enum      | `@Enumerated(EnumType.STRING)`                                                                              |
| 기본값       | `@Builder.Default` (DB `DEFAULT`는 쓰지 않음 — [5.2](#52-스키마-규칙) 참고)                                             |
| 민감정보      | 암호화해서 저장                                                                                                    |

---

## 5. DB 마이그레이션

스키마는 **Flyway 마이그레이션 파일이 유일한 원본**입니다.
엔티티를 바꿨다면 마이그레이션 파일도 반드시 추가합니다.

### 5.1 파일

**위치**: `src/main/resources/db/migration/`

**파일명**: `V{yyMMddHHmmss}__{영문_snake_case_설명}.sql`

```
V260715212830__add_type_to_notification_outboxes.sql
V260716013618__add_watermark_retry_count.sql
```

- `V`는 대문자, 버전과 설명 사이는 언더스코어 **두 개**
- 버전은 **작성 시각**입니다. `out-of-order: true`와 함께 쓰므로 브랜치가 머지되는 순서와 상관없이 적용됩니다.
- **이미 머지된 파일은 수정하지 않습니다.** 되돌리거나 고칠 때는 새 파일을 추가합니다.

**파일 상단에는 헤더 주석**을 둡니다.

```sql
-- ===========================================================================
-- Description: commission_draft_files에 워터마크 재처리 횟수 컬럼 추가
--
-- Background:
--  - 워터마크 재처리 스케줄러 도입으로 파일별 재시도 횟수 추적이 필요함.
--
-- Note:
--  - 기본값의 단일 진실은 엔티티(@Builder.Default)이며 DB DEFAULT 절은 사용하지 않음.
-- ===========================================================================
```

### 5.2 스키마 규칙

| 항목        | 규칙                                                                                |
|-----------|-----------------------------------------------------------------------------------|
| enum      | MySQL `ENUM` 대신 `VARCHAR(n)`                                                      |
| 날짜·시각     | `DATETIME(6)`                                                                     |
| boolean   | `BIT(1)`                                                                          |
| 공통 컬럼     | `created_at DATETIME(6) NOT NULL`, `updated_at DATETIME(6) NOT NULL`을 테이블마다 직접 선언 |
| 기본값       | 값이 있는 `DEFAULT` 금지. nullable 컬럼의 `DEFAULT NULL`만 허용. 기본값은 엔티티의 `@Builder.Default` |
| UNIQUE 이름 | `uk_<테이블>_<컬럼들>`                                                                  |
| INDEX 이름  | `idx_<테이블>_<컬럼들>`                                                                 |
| FK 이름     | `fk_<테이블>_<FK 컬럼>`                                                                |
| FK 인덱스    | FK 컬럼이 복합 UNIQUE의 첫 컬럼이면 별도 인덱스 생략(재사용). 아니면 `idx_`로 명시                           |

제약·인덱스 이름을 명시하는 이유는 Hibernate가 만드는 난수 이름(`UK8a7s6d…`)으로는 에러 로그에서 어떤 제약이 깨졌는지 알 수 없기 때문입니다.
엔티티의 `@UniqueConstraint`, `@Index`에도 같은 이름을 씁니다.

**기본값을 DB에 두지 않는 이유** — 기본값이 엔티티와 DB 두 곳에 있으면 한쪽만 바뀌는 순간 어느 쪽이 맞는지 알 수 없습니다. 엔티티 한 곳에만 둡니다.

---

## 6. 이벤트와 비동기 처리

### 6.1 이벤트와 리스너

이벤트는 **record**로 만들고, 이름은 사건 이름 + `Event`입니다 (예: `RevisionRequestedEvent`).

리스너는 **발행한 쪽과 같은 커밋에 묶여야 하는지**로 고릅니다.

| 리스너가 하는 일       | 방식                                                             | 예시                                                      |
|-----------------|----------------------------------------------------------------|---------------------------------------------------------|
| 같은 DB에 쓰기       | `@EventListener`                                               | `RevisionRequestedNotifier`, `PayoutSettlementListener` |
| 커밋 이후 외부 시스템 호출 | `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` | `DraftWatermarkListener` (Lambda 호출)                    |

### 6.2 메일 알림 추가

Outbox에 적재하면 스케줄러가 RabbitMQ로
발행하고, [Ditda-Notification-Worker](https://github.com/Ditda-Official/Ditda-Notification-Worker)가 발송합니다.

1. [`NotificationType`](src/main/java/ditda/backend/global/notification/NotificationType.java)에 상수를 추가합니다.
2. Notifier에서 Outbox에 적재합니다.
3. 워커 레포의 `NotificationType`에 같은 이름의 상수를 추가하고, 메일 제목과 템플릿을 지정합니다.

### 6.3 스케줄러와 배치

- `@Scheduled`에는 `zone = "Asia/Seoul"`을 명시하고, Scheduler 클래스는 Service에 위임만 합니다.
- 여러 건을 처리할 때는 한 건씩 예외를 격리해, 한 건의 실패가 나머지를 멈추지 않게 합니다.
- Blue/Green 전환 중에는 두 인스턴스가 함께 떠 있습니다. 처리 직전에 상태를 다시 확인하거나 조건부 UPDATE로 선점한 뒤 처리합니다.

---

## 7. 로그와 메트릭

### 7.1 형식

`"<영어 평서문>. key1={}, key2={}"` 형식으로 씁니다.

```
// ❌
log.info("처리 완료: " + commissionId);
log.error("[ERROR] 락 획득 실패 >>> " + key);

// ✅
log.info("Final deadline processed. commissionId={}, cancelled={}", commissionId, cancelled);
log.warn("Failed to invoke watermark Lambda. draftFileId={}, key={}", draftFileId, originalKey, exception);
```

- 값은 문자열에 이어 붙이지 않고 `{}` 자리표시자로 넘깁니다. 운영 로그가 JSON으로 수집되어 LogQL에서 필드로 추출됩니다.
- 예외 객체는 마지막 인자로 넘깁니다. 대응하는 `{}`가 없으면 스택트레이스로 출력됩니다.
- 예외는 처리하는 곳에서 한 번만 남깁니다.

### 7.2 레벨

**"누가 조치해야 하는가"** 로 고릅니다.

| 레벨      | 기준                        | 예시                          |
|---------|---------------------------|-----------------------------|
| `ERROR` | 사람이 개입해야 함. 자동 복구 경로가 없음  | 재시도를 모두 쓴 영구 실패, 처리되지 않은 예외 |
| `WARN`  | 비정상이지만 자동 복구되거나, 클라이언트 잘못 | 재시도 예정인 실패, 락 획득 실패, 잘못된 요청 |
| `INFO`  | 주요 비즈니스 사건과 상태 전이         | 외주 매칭, 수정 요청, 배치 종료         |
| `DEBUG` | 쓰지 않습니다                   |                             |

같은 실패라도 **다음에 자동으로 다시 시도되면 WARN, 더 이상 시도되지 않으면 ERROR**입니다.

### 7.3 남길 값

- 상관관계 키(`commissionId`, `draftFileId`, `outboxId`)는 반드시 남깁니다.
- 비밀번호, 토큰, 계좌번호, 엔티티나 DTO 전체는 남기지 않습니다.

| 값                       | 처리                                                  |
|-------------------------|-----------------------------------------------------|
| 이메일                     | `LogMasker.maskEmail(email)` → `ab****@example.com` |
| 외부에서 들어온 문자열 (S3 key 등) | `LogMasker.sanitize(value)` → 제어문자 제거, 100자 초과분 절단  |

### 7.4 메트릭

- 알림이 필요한 비즈니스 실패는 로그 문구가 아니라 메트릭으로 신호를 보냅니다.
- 태그 값에는 개수가 정해진 것만 씁니다. 

