# WBS 09.02 시험후기 작성·수정·버전 충돌 API 작업 기록

```yaml
schema_version: 3.1.0
template: false
record_type: session
summary_ko: 시험후기 작성·수정 API에 인증 사용자, EXAM_REVIEW, 자격증 검증과 공통 충돌 예외를 통합했다. review 및 user 회귀 테스트와 assemble이 통과했다.
task_id: WBS-09-02-REVIEW-WRITE-UPDATE
session_id: WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z
kind: FEATURE
feature_id: WBS-09-02-VERSION-CONFLICT
human_owner: current_user
actor_id: codex
tool: Codex
actual_model: GPT-5
started_at: '2026-10-10T11:38:39Z'
recorded_at: '2026-10-10T11:38:39Z'
timezone: UTC
source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
existing_edits: []
rules_version: 3.1.0
read_files:
  - AI_RULES.md
  - docs/CURRENT_STATE.md
  - docs/FEATURE_STATUS.md
  - docs/NAMING.md
  - docs/TERMS.md
  - docs/DECISIONS.md
  - docs/features/WBS-09-01-REVIEW-QUERY.md
  - docs/features/WBS-09-02-VERSION-CONFLICT.md
  - docs/worklogs/TASK-09-02-VERSION-CONFLICT__codex__20261009T180800Z.md
  - src/main/java/com/youthguide/api/review
  - src/main/java/com/youthguide/api/global
  - src/main/java/com/youthguide/api/user
  - src/main/java/com/youthguide/api/certificate
goal: 인증된 회원이 EXAM_REVIEW 시험후기를 작성·수정하고 버전 충돌을 공통 오류 계약으로 처리하도록 09.02를 완성한다.
scope:
  - POST /api/v1/community/posts
  - PUT /api/v1/community/posts/{postId}
  - LoginMember 기반 작성자 연결
  - CertificateRepository 존재 검증
  - 기존 수정 상태를 공통 오류로 변환
  - JPA optimistic locking 공통 409 매핑
  - 인증·CSRF·validation·서비스 회귀 테스트
  - 09.02 기능·상태 문서 갱신
approval_refs:
  - 현재 대화의 사용자 요청
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
verification_status: PASSED
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_DEPLOYED
latest_event_id: EVT-IMPLEMENTED-003
previous_log: docs/worklogs/TASK-09-02-VERSION-CONFLICT__codex__20261009T180800Z.md
remaining_work:
  - 사람 검토
  - 실제 MySQL 동시 수정 경쟁 검증
resume_when: []
next_action: 사용자가 구현과 API 계약을 검토하고 필요하면 기존 opt-in MySQL 환경에서 실제 동시성 검증을 수행한다.
missing_reasons: {}
not_applicable_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 지정 브랜치와 clean 상태, 최신 09.01 및 cherry-pick된 09.02 구현, Member/Auth/Certificate/공통 예외 구조를 확인하고 통합 작업을 시작했다.
event_id: EVT-START-001
event_type: START
actor_id: codex
recorded_at: '2026-10-10T11:38:39Z'
occurred_at: '2026-10-10T11:38:39Z'
source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
rule_refs:
  - {rule_id: C01, rule_text: 작업 목표·범위·적용 기준·소스 리비전·기존 수정을 구현 전에 확인한다., source_path: AI_RULES.md}
  - {rule_id: C05, rule_text: 승인 범위의 작은 변경만 수행하며 기존 계약과 팀원 코드를 보존한다., source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - feature-09-02-review-write-update와 HEAD 96fb9e6, clean 작업 트리를 확인했다.
  - 기존 factory, DTO validation, UpdateResult, 사전 version 비교와 UTC 시간 처리를 확인했다.
  - LoginMember, CertificateRepository, ErrorCode, BusinessException, GlobalExceptionHandler, SecurityConfig를 확인했다.
  - Spring Framework 7.0.9의 JpaOptimisticLockingFailureException이 ObjectOptimisticLockingFailureException을 상속하고 JPA OptimisticLockException 변환에 사용됨을 로컬 의존성 바이트코드로 확인했다.
changes:
  - {path: docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md, symbol: 작업 세션, operation: ADD, reason: AI_RULES.md 작업 기록 규칙 준수}
checks:
  - command: git status --short; git branch --show-current; git log -6 --oneline
    environment: 실제 로컬 저장소
    input: null
    expected: 요청 브랜치와 clean 작업 트리, 제시된 cherry-pick 커밋
    observed: feature-09-02-review-write-update, HEAD 96fb9e6, 변경 없음
    result: PASSED
    evidence_ref: 현재 세션 명령 출력
  - command: javap Spring Framework 7.0.9 optimistic locking classes
    environment: 로컬 Gradle 의존성 캐시
    input: JpaOptimisticLockingFailureException, ObjectOptimisticLockingFailureException, EntityManagerFactoryUtils
    expected: 실제 예외 상속과 JPA 변환 경로 확인
    observed: JpaOptimisticLockingFailureException은 ObjectOptimisticLockingFailureException의 하위 타입이며 JPA OptimisticLockException 변환에 사용됨
    result: PASSED
    evidence_ref: 현재 세션 명령 출력
unperformed_checks:
  - 구현 후 review 및 Security 테스트와 assemble을 실행한다.
state_before:
  implementation_status: IMPLEMENTING
  work_status: ACTIVE
state_after:
  implementation_status: IMPLEMENTING
  work_status: ACTIVE
remaining_work:
  - 승인된 통합 코드·테스트·문서를 구현한다.
next_action: Command Service와 Controller, 공통 예외 흐름을 최소 변경한다.
state_updates:
  - {path: docs/features/WBS-09-02-VERSION-CONFLICT.md, result: NOT_APPLIED, evidence_ref: 구현·검증 후 갱신 예정}
  - {path: docs/FEATURE_STATUS.md, result: NOT_APPLIED, evidence_ref: 구현·검증 후 갱신 예정}
  - {path: docs/CURRENT_STATE.md, result: NOT_APPLIED, evidence_ref: 구현·검증 후 갱신 예정}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 인증·CSRF가 적용된 작성·수정 Controller, 자격증 존재 검증, 공통 오류 매핑과 optimistic locking 처리의 단위·WebMvc·회귀 테스트를 완료했다.
event_id: EVT-VERIFY-002
event_type: VERIFY
actor_id: codex
recorded_at: '2026-10-10T11:48:29Z'
occurred_at: '2026-10-10T11:48:29Z'
source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
rule_refs:
  - {rule_id: C12, rule_text: 테스트 실패를 숨기기 위해 규칙이나 테스트를 약화하지 않는다., source_path: AI_RULES.md}
  - {rule_id: C15, rule_text: 검증 사건에 실행 결과와 남은 작업을 기록한다., source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - CommunityPostCommandController와 직접 응답 DTO를 추가했다.
  - LoginMember의 memberId를 작성·수정 Service에 전달하고 클라이언트 authorMemberId와 postKind 입력을 사용하지 않게 했다.
  - CertificateRepository.existsById 검증과 CERTIFICATE_NOT_FOUND를 추가했다.
  - 기존 UpdateResult 상태를 POST_NOT_FOUND, ACCESS_DENIED, VERSION_CONFLICT로 연결했다.
  - ObjectOptimisticLockingFailureException을 VERSION_CONFLICT로 매핑했다.
  - 실제 SecurityConfig를 적용한 WebMvc 테스트로 인증과 CSRF를 검증했다.
changes:
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: CommunityPostCommandController, operation: ADD, reason: 작성·수정 HTTP 및 LoginMember 연결}
  - {path: src/main/java/com/youthguide/api/review/dto/CommunityPostCommandResponse.java, symbol: CommunityPostCommandResponse, operation: ADD, reason: 성공 시 wrapper 없는 postId DTO 반환}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: createPost/updatePost, operation: MODIFY, reason: EXAM_REVIEW 내부 고정과 Certificate 존재 검증}
  - {path: src/main/java/com/youthguide/api/global/exception/ErrorCode.java, symbol: CERTIFICATE_NOT_FOUND/VERSION_CONFLICT, operation: MODIFY, reason: 공통 404·409 계약}
  - {path: src/main/java/com/youthguide/api/global/exception/GlobalExceptionHandler.java, symbol: handleOptimisticLocking, operation: MODIFY, reason: JPA 실제 경쟁 충돌을 공통 409로 변환}
  - {path: src/test/java/com/youthguide/api/review/controller/CommunityPostCommandControllerTest.java, symbol: CommunityPostCommandControllerTest, operation: ADD, reason: Controller·인증·CSRF·예외 계약 검증}
  - {path: src/test/java/com/youthguide/api/review/service/CommunityPostCommandServiceTest.java, symbol: CommunityPostCommandServiceTest, operation: MODIFY, reason: 작성의 EXAM_REVIEW·자격증 검증}
  - {path: src/test/java/com/youthguide/api/review/service/CommunityPostUpdateServiceTest.java, symbol: CommunityPostUpdateServiceTest, operation: MODIFY, reason: 수정의 EXAM_REVIEW·자격증 검증과 기존 충돌 회귀}
checks:
  - command: .\\gradlew.bat test --tests "com.youthguide.api.review.controller.CommunityPostCommandControllerTest" --tests "com.youthguide.api.review.service.CommunityPostCommandServiceTest" --tests "com.youthguide.api.review.service.CommunityPostUpdateServiceTest"
    environment: 실제 로컬 저장소, WebMvc SecurityConfig와 mock Service/Repository
    input: 작성·수정·인증·CSRF·공통 예외 시나리오
    expected: 모든 신규·수정 테스트 통과
    observed: BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: 현재 세션 Gradle 출력
  - command: .\\gradlew.bat test --tests "com.youthguide.api.review.*"
    environment: 실제 로컬 저장소, MySQL opt-in 비활성
    input: 기존 09.01과 09.02 포함 review 전체 테스트
    expected: DB 비의존 테스트 통과, opt-in 통합 테스트 제외
    observed: 50건 중 40건 실행·통과, MySQL opt-in 10건 건너뜀, 실패 0건, BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\\gradlew.bat test --tests "com.youthguide.api.user.*" --tests "com.youthguide.api.review.*"
    environment: 실제 로컬 저장소, MySQL opt-in 비활성
    input: 공통 예외 변경을 포함한 DB 비의존 회귀
    expected: user·review 테스트 통과
    observed: 74건 중 64건 실행·통과, MySQL opt-in 10건 건너뜀, 실패 0건, BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\\gradlew.bat assemble
    environment: 실제 로컬 저장소
    input: 현재 소스
    expected: 패키징 성공
    observed: BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: 현재 세션 Gradle 출력
unperformed_checks:
  - 실제 MySQL에서 두 트랜잭션이 같은 게시글을 동시에 수정하는 경쟁 테스트는 데이터 변경 금지와 opt-in 조건에 따라 실행하지 않았다.
state_before:
  implementation_status: IMPLEMENTING
  work_status: ACTIVE
state_after:
  implementation_status: IMPLEMENTED
  work_status: IN_REVIEW
remaining_work:
  - 사람 검토
  - 실제 MySQL 동시 수정 경쟁 검증
next_action: 기존 09.02 기능·상태 문서에 최종 계약과 검증 결과를 반영한다.
state_updates:
  - {path: docs/features/WBS-09-02-VERSION-CONFLICT.md, result: NOT_APPLIED, evidence_ref: 다음 기록 단계에서 갱신}
  - {path: docs/FEATURE_STATUS.md, result: NOT_APPLIED, evidence_ref: 기능 문서 갱신 후 적용}
  - {path: docs/CURRENT_STATE.md, result: NOT_APPLIED, evidence_ref: 기능 인덱스 갱신 후 적용}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 승인된 WBS 09.02 작성·수정·버전 충돌 API를 통합하고 필수 테스트와 assemble을 통과해 사람 검토 단계로 전환한다.
event_id: EVT-IMPLEMENTED-003
event_type: IMPLEMENTED
actor_id: codex
recorded_at: '2026-10-10T11:48:29Z'
occurred_at: '2026-10-10T11:48:29Z'
source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
rule_refs:
  - {rule_id: C17, rule_text: 구현 사건에 산출물·흐름·예외·제한·인수인계를 기록한다., source_path: AI_RULES.md}
  - {rule_id: C18, rule_text: 승인된 적용 기준과 필수 검증 통과 후 IMPLEMENTED로 기록하고 사람 검토 전에는 IN_REVIEW를 유지한다., source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - 승인된 09.02 계약과 테스트 근거를 기존 09.02 기능 문서에 통합한다.
changes:
  - {path: docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md, symbol: EVT-IMPLEMENTED-003, operation: MODIFY, reason: 구현 완료와 제한 사항 기록}
checks:
  - command: review·user 단위/WebMvc 테스트와 assemble
    environment: 실제 로컬 저장소
    input: null
    expected: 승인된 09.02 동작 검증과 패키징 성공
    observed: 필수 검증 성공, 실제 MySQL 경쟁 검증은 미수행으로 분리 기록
    result: PASSED
    evidence_ref: EVT-VERIFY-002
unperformed_checks:
  - 실제 MySQL 동시 수정 경쟁 검증
state_before:
  implementation_status: IMPLEMENTING
  work_status: ACTIVE
state_after:
  implementation_status: IMPLEMENTED
  work_status: IN_REVIEW
remaining_work:
  - 사람 검토
  - 실제 MySQL 동시 수정 경쟁 검증
next_action: feature_file, FEATURE_STATUS, CURRENT_STATE 순으로 갱신한다.
state_updates:
  - {path: docs/features/WBS-09-02-VERSION-CONFLICT.md, result: NOT_APPLIED, evidence_ref: 이 사건 다음에 갱신}
  - {path: docs/FEATURE_STATUS.md, result: NOT_APPLIED, evidence_ref: 기능 문서 갱신 후 적용}
  - {path: docs/CURRENT_STATE.md, result: NOT_APPLIED, evidence_ref: 기능 인덱스 갱신 후 적용}
missing_reasons: {}
completion:
  feature_id: WBS-09-02-VERSION-CONFLICT
  outputs:
    - 시험후기 작성 API
    - 시험후기 수정 API
    - LoginMember 작성자 연결
    - Certificate 존재 검증
    - 사전 version 비교와 JPA optimistic locking 공통 409
    - 인증·CSRF·validation·회귀 테스트
  delivered_behavior: 인증된 회원이 CSRF 토큰과 함께 EXAM_REVIEW를 작성·수정하며, 대상·작성자·자격증·버전 오류를 공통 오류 응답으로 받는다.
  scope:
    - POST /api/v1/community/posts
    - PUT /api/v1/community/posts/{postId}
  excluded_scope:
    - 삭제·댓글·좋아요 토글·신고·조회수 증가·첨부·임시저장
    - DB·Flyway·SecurityConfig·연관관계 변경
  acceptance_results:
    - {ac_id: AC-01, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostCommandServiceTest]}
    - {ac_id: AC-02, status: MET, evidence_refs: [CommunityPostCommandServiceTest]}
    - {ac_id: AC-03, status: MET, evidence_refs: [CommunityPostCreateRequestTest, CommunityPostCommandControllerTest]}
    - {ac_id: AC-04, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostUpdateServiceTest]}
    - {ac_id: AC-05, status: MET, evidence_refs: [CommunityPostCommandControllerTest]}
    - {ac_id: AC-06, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostUpdateServiceTest]}
    - {ac_id: AC-07, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostUpdateServiceTest]}
    - {ac_id: AC-08, status: MET, evidence_refs: [CommunityPostUpdateServiceTest]}
    - {ac_id: AC-09, status: MET, evidence_refs: [CommunityPostCommandControllerTest]}
    - {ac_id: AC-10, status: MET, evidence_refs: [CommunityPostCommandControllerTest]}
    - {ac_id: AC-11, status: MET, evidence_refs: [EVT-VERIFY-002]}
  source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
  connections:
    - from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: createPost/updatePost}
      to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: createPost/updatePost, external_boundary: HTTP}
      payload: LoginMember.memberId와 validation된 작성·수정 요청
      contract_ref: POST/PUT /api/v1/community/posts
      purpose: 인증 작성자 기반 명령 처리
      verification_evidence: [CommunityPostCommandControllerTest]
    - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: validateCertificateExists}
      to: {path: src/main/java/com/youthguide/api/certificate/repository/CertificateRepository.java, symbol: existsById, external_boundary: MySQL}
      payload: certificateId
      contract_ref: Certificate 존재 검증
      purpose: 유효한 자격증만 게시글에 저장
      verification_evidence: [CommunityPostCommandServiceTest, CommunityPostUpdateServiceTest]
  verified_flow: SecurityConfig 인증·CSRF → Controller의 LoginMember memberId → Service의 EXAM_REVIEW·Certificate 검증·사전 version 비교 → JPA @Version 갱신 또는 공통 오류 응답
  verification_scope: MIXED
  exception_coverage:
    - TARGET_NOT_FOUND → POST_NOT_FOUND 404
    - AUTHOR_MISMATCH → ACCESS_DENIED 403
    - Certificate 미존재 → CERTIFICATE_NOT_FOUND 404
    - 요청 version 불일치 또는 JPA optimistic locking 예외 → VERSION_CONFLICT 409
  limitations:
    - 실제 MySQL의 동시 UPDATE 경쟁은 실행하지 않았다.
    - 숫자 문자열이 Long 범위를 넘는 경우의 INVALID_INPUT 매핑은 아직 공통 정책이 없다.
  remaining_work:
    - 사람 검토
    - 실제 MySQL 동시 수정 경쟁 검증
  review_status: PENDING
  review_evidence: []
  integration_status: BRANCH_ONLY
  deployment_status: NOT_DEPLOYED
  handoff: SecurityConfig와 DB를 변경하지 않았으며, 성공 응답은 postId 문자열 DTO를 직접 반환한다.
```
