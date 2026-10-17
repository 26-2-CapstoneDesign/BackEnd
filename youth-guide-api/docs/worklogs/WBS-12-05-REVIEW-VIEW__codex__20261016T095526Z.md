# WBS 12.05 시험후기 조회수 작업 기록

```yaml
schema_version: 3.1.0
template: false
record_type: session
summary_ko: 로그인 사용자의 시험후기 상세 조회마다 기존 community_post_metric 조회수를 원자적으로 1 증가하도록 구현했다. 로컬 loopback 전용 opt-in UPSERT 테스트를 분리했고 기본 자동 테스트와 assemble은 통과했으며 실제 MySQL 쓰기·동시 요청 검증은 실행하지 않았다.
task_id: WBS-12-05-REVIEW-VIEW
session_id: WBS-12-05-REVIEW-VIEW__codex__20261016T095526Z
kind: FEATURE
feature_id: WBS-12-05-REVIEW-VIEW
human_owner: current_user
actor_id: codex
tool: Codex
actual_model: GPT-5
started_at: '2026-10-16T09:44:42Z'
recorded_at: '2026-10-16T15:12:33Z'
timezone: UTC
source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae (+ 미커밋 12.05 변경)
existing_edits: []
rules_version: 3.1.0
read_files:
  - AI_RULES.md
  - PROJECT_CONTEXT.md
  - README_AI.md
  - docs/CURRENT_STATE.md
  - docs/FEATURE_STATUS.md
  - docs/DECISIONS.md
  - docs/NAMING.md
  - docs/TERMS.md
  - docs/features/_TEMPLATE.md
  - docs/worklogs/_TEMPLATE.md
  - docs/features/WBS-09-01-REVIEW-QUERY.md
  - docs/features/WBS-09-03-REVIEW-DELETE.md
  - docs/features/WBS-10-03-REVIEW-LIKE.md
  - docs/features/WBS-12-04-REVIEW-COMMENT.md
  - src/main/java/com/youthguide/api/review
  - src/test/java/com/youthguide/api/review
goal: 공개 상세 API를 유지하면서 로그인 사용자의 유효한 EXAM_REVIEW 상세 조회마다 viewCount를 원자적으로 1 증가한다.
scope:
  - 기존 GET /api/v1/community/posts/{postId}의 optional LoginMember 연결
  - community_post_metric 원자적 UPSERT
  - EXAM_REVIEW 조건과 POST_NOT_FOUND 재사용
  - 자동 테스트와 문서 기록
approval_refs:
  - 현재 대화의 사용자 요청
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_APPLICABLE
latest_event_id: EVT-VERIFY-003
previous_log: docs/worklogs/WBS-12-04-REVIEW-COMMENT__codex__20261012T134019Z.md
remaining_work:
  - 사람 코드 검토
  - 로컬 또는 격리된 MySQL에서 실제 UPSERT·동시 증가 검증
resume_when: []
next_action: 사용자 검토 후 로컬 MySQL에서 metric 행 유무와 동시 요청을 포함한 쓰기 통합 검증 여부를 결정한다.
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 commit·push·merge·배포를 금지했다.
```

```yaml
template: false
record_type: event
summary_ko: 승인된 최종 정책과 현재 12.04 기반 브랜치, 기존 metric 조회·삭제·공개 상세 흐름을 확인했다.
event_id: EVT-START-001
event_type: START
actor_id: codex
recorded_at: '2026-10-16T09:55:26Z'
occurred_at: '2026-10-16T09:44:42Z'
source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae
rule_refs:
  - {rule_id: C05, rule_text: "MAY make small reversible internal edits ONLY within approved scope, preserving public behavior/contracts and others' edits.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - feature-12-05-review-view 브랜치와 clean working tree를 확인했다.
  - 사용자별 1회 정책 폐기와 로그인 상세 요청마다 증가하는 최종 정책을 적용 기준으로 확인했다.
  - 기존 community_post_metric, EXAM_REVIEW, 공개 상세, metric 삭제 흐름을 재사용하기로 했다.
changes: []
checks: []
unperformed_checks:
  - {command: 실제 MySQL UPSERT·동시 증가 통합 검증, reason_ko: 구현과 비DB 자동 검증 후 별도 로컬 환경에서만 수행하기로 한 범위}
state_before: {implementation_status: NOT_STARTED, work_status: QUEUED, review_status: NOT_REQUESTED}
state_after: {implementation_status: IMPLEMENTING, work_status: ACTIVE, review_status: PENDING}
remaining_work:
  - 코드·테스트 구현
  - 검증 및 상태 문서 갱신
next_action: 기존 Controller·CommandService·CommandRepository를 최소 범위로 확장한다.
state_updates: []
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: optional 인증과 원자적 metric UPSERT를 연결하고 대상·review·전체 회귀 테스트와 assemble을 통과해 사람 검토 상태로 전환했다.
event_id: EVT-IMPLEMENTED-002
event_type: IMPLEMENTED
actor_id: codex
recorded_at: '2026-10-16T09:55:26Z'
occurred_at: '2026-10-16T09:55:26Z'
source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae (+ 미커밋 12.05 변경)
rule_refs:
  - {rule_id: C04, rule_text: "MUST NOT invent successful results.", source_path: AI_RULES.md}
  - {rule_id: C17, rule_text: "ON IMPLEMENTED/COMPLETE: record feature/output, acceptance evidence, file-symbol edges, payload/contract, verified flow, exceptions, limitations, review and handoff.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - 인증 상세 요청에만 viewCount 증가 명령을 호출하고 그 뒤 기존 상세 조회를 실행하도록 연결했다.
  - EXAM_REVIEW 게시글을 조건으로 metric 행이 없으면 1을 생성하고 있으면 DB에서 view_count + 1을 수행하는 native UPSERT를 추가했다.
  - 미존재·다른 유형은 영향 행 0건으로 판단해 기존 POST_NOT_FOUND를 반환하도록 했다.
  - 공개 상세, 목록·검색, 기존 DTO·삭제·SecurityConfig·V1 migration을 보존했다.
changes:
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPost, operation: MODIFY, reason: optional LoginMember에 따른 인증 상세 증가 연결}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: incrementViewCount, operation: MODIFY, reason: EXAM_REVIEW 증가와 POST_NOT_FOUND 처리}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: incrementPostViewCount, operation: MODIFY, reason: metric 원자적 UPSERT}
  - {path: src/test/java/com/youthguide/api/review/service/CommunityPostViewCountServiceTest.java, symbol: CommunityPostViewCountServiceTest, operation: ADD, reason: 증가·반복·미존재 분기 검증}
  - {path: src/test/java/com/youthguide/api/review/controller/CommunityPostQueryControllerTest.java, symbol: CommunityPostQueryControllerTest, operation: MODIFY, reason: 비로그인 기존 흐름 회귀 검증}
  - {path: src/test/java/com/youthguide/api/review/controller/CommunityPostQuerySecurityTest.java, symbol: CommunityPostQuerySecurityTest, operation: MODIFY, reason: optional 인증과 요청별 증가·호출 순서 검증}
  - {path: src/test/java/com/youthguide/api/review/repository/CommunityPostCommandRepositoryTest.java, symbol: viewCountIncrementUsesAtomicUpsertAndPostKindGuard, operation: MODIFY, reason: native SQL 구조 검증}
checks:
  - {command: ".\\gradlew.bat test --tests 대상 4개 클래스", environment: actual local branch, input: Controller·Security·Service·Repository 12.05 관련 테스트, expected: 전체 통과, observed: 26 tests passed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: ".\\gradlew.bat test --tests com.youthguide.api.review.*", environment: actual local branch, input: review 회귀 테스트, expected: 비DB 테스트 통과, observed: 132 tests 중 122 passed·MySQL opt-in 10 skipped·0 failed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: .\\gradlew.bat test, environment: actual local branch, input: 전체 회귀 테스트, expected: 전체 자동 테스트 통과, observed: 168 tests 중 158 passed·MySQL opt-in 10 skipped·0 failed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: .\\gradlew.bat assemble, environment: actual local branch, input: main source, expected: 패키징 성공, observed: BUILD SUCCESSFUL, result: PASSED, evidence_ref: Gradle output}
  - {command: git diff --check, environment: actual local branch, input: 추적 변경 파일, expected: whitespace 오류 없음, observed: 출력 없음, result: PASSED, evidence_ref: terminal output}
unperformed_checks:
  - {command: 실제 MySQL native UPSERT·동시 상세 요청 통합 검증, reason_ko: 공용 DB 접근·수정을 금지했고 현재 MySQL opt-in 10건은 조회 Repository 검증만 제공하므로 로컬 격리 환경에서 별도 수행해야 함}
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: NOT_RUN}
state_after: {implementation_status: IMPLEMENTED, work_status: IN_REVIEW, verification_status: PARTIAL}
remaining_work:
  - 사람 코드 검토
  - 로컬 또는 격리된 MySQL 실제 UPSERT·동시 증가 검증
next_action: 사용자에게 변경·검증·미검증 범위를 보고한다.
state_updates:
  - {path: docs/features/WBS-12-05-REVIEW-VIEW.md, result: APPLIED, evidence_ref: acceptance_criteria}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-12-05-REVIEW-VIEW entry}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-12-05-REVIEW-VIEW active task}
missing_reasons: {}
completion:
  feature_id: WBS-12-05-REVIEW-VIEW
  outputs:
    - 기존 GET /api/v1/community/posts/{postId}의 로그인 요청별 조회수 증가
    - community_post_metric 원자적 UPSERT
    - 기존 CommunityPostDetailResponse.viewCount 직접 응답
  delivered_behavior: 비로그인은 기존 상세만 조회하고 로그인 사용자는 동일 사용자·작성자 여부와 관계없이 유효한 EXAM_REVIEW 상세 요청마다 조회수가 1 증가한다.
  scope:
    - WBS 12.05 시험후기 조회수
  excluded_scope:
    - 사용자별 조회 이력과 신규 테이블·Entity·Repository
    - DB·Flyway·API DTO·SecurityConfig·삭제 흐름 변경
  acceptance_results:
    - {ac_id: AC-01, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.allowsAnonymousDetailRequest]}
    - {ac_id: AC-02, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.incrementsViewCountForEveryRepeatedAuthenticatedDetailRequest]}
    - {ac_id: AC-03, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.incrementsViewCountForDifferentAuthenticatedMembersIncludingAuthor]}
    - {ac_id: AC-04, status: MET, evidence_refs: [CommunityPostViewCountServiceTest.rejectsMissingOrNonExamReviewWithoutCreatingMetric]}
    - {ac_id: AC-05, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest.viewCountIncrementUsesAtomicUpsertAndPostKindGuard]}
    - {ac_id: AC-06, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.incrementsViewCountForEveryRepeatedAuthenticatedDetailRequest]}
    - {ac_id: AC-07, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.keepsAuthenticatedListAndSearchFromIncrementingViewCount]}
    - {ac_id: AC-08, status: MET, evidence_refs: [git diff --check]}
    - {ac_id: AC-09, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest.viewCountIncrementUsesAtomicUpsertAndPostKindGuard]}
    - {ac_id: AC-10, status: MET, evidence_refs: [EVT-IMPLEMENTED-002]}
  source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae (+ 미커밋 12.05 변경)
  connections:
    - from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPost}
      to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: incrementViewCount, external_boundary: HTTP}
      payload: postId와 nullable LoginMember 존재 여부
      contract_ref: GET /api/v1/community/posts/{postId}
      purpose: 인증 상세 요청만 조회수 증가
      verification_evidence: [CommunityPostQuerySecurityTest]
    - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: incrementViewCount}
      to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: incrementPostViewCount, external_boundary: MySQL}
      payload: postId와 EXAM_REVIEW
      contract_ref: community_post_metric PK post_id
      purpose: 유효 대상 조건부 원자 증가
      verification_evidence: [CommunityPostViewCountServiceTest, CommunityPostCommandRepositoryTest]
  verified_flow: 공개 GET → nullable LoginMember 확인 → 인증이면 EXAM_REVIEW 조건 metric UPSERT 커밋 → 기존 상세 native 조회 → 증가된 viewCount DTO 직접 반환; 비로그인이면 기존 상세 조회만 실행
  verification_scope: MIXED
  exception_coverage:
    - 미존재·다른 postKind POST_NOT_FOUND 404
    - 비로그인 상세 조회 허용
  limitations:
    - 실제 MySQL에서 metric 행 최초 INSERT, 기존 행 UPDATE, 동시 요청 누적은 실행하지 않았다.
    - Repository 테스트는 native SQL 구조를, Service·Controller 테스트는 분기와 호출 순서를 검증한다.
  remaining_work:
    - 사람 코드 검토
    - 로컬 또는 격리된 MySQL 실제 쓰기·동시성 통합 검증
  review_status: PENDING
  review_evidence: []
  integration_status: BRANCH_ONLY
  deployment_status: NOT_APPLICABLE
  handoff: application-local.yaml, SecurityConfig, DTO, DB·Flyway, V1 migration과 기존 삭제 순서를 변경하지 않았다.
```

```yaml
template: false
record_type: event
summary_ko: 실제 MySQL 쓰기 검증을 공용 DB와 분리하기 위해 명시적 opt-in·loopback URL 확인·rollback 조건의 테스트를 추가하고 최종 회귀를 다시 통과했다.
event_id: EVT-VERIFY-003
event_type: VERIFY
actor_id: codex
recorded_at: '2026-10-16T15:12:33Z'
occurred_at: '2026-10-16T15:12:33Z'
source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae (+ 미커밋 12.05 변경)
rule_refs:
  - {rule_id: C04, rule_text: "MUST NOT invent successful results.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - RUN_MYSQL_VIEW_COUNT_WRITE_TESTS=true일 때만 후보가 되는 별도 MySQL 쓰기 테스트를 추가했다.
  - JDBC URL이 localhost·127.0.0.1·[::1]이 아니면 DML 전에 테스트를 중단하도록 했다.
  - metric 삭제·최초 INSERT·기존 행 증가를 테스트 트랜잭션 rollback 범위에 두었다.
  - 환경변수를 설정하지 않은 기본 자동 테스트에서는 해당 2건이 건너뛰는 것을 확인했다.
changes:
  - {path: src/test/java/com/youthguide/api/review/repository/CommunityPostViewCountMySqlIntegrationTest.java, symbol: CommunityPostViewCountMySqlIntegrationTest, operation: ADD, reason: 공용 DB와 분리된 로컬 MySQL UPSERT 검증 경로 제공}
checks:
  - {command: ".\\gradlew.bat test --tests 12.05 대상 5개 클래스", environment: actual local branch, input: 기본 환경변수 미설정, expected: 비DB 테스트 통과·쓰기 opt-in 건너뜀, observed: 28 tests 중 26 passed·2 skipped·0 failed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: ".\\gradlew.bat test --tests com.youthguide.api.review.*", environment: actual local branch, input: review 회귀 테스트, expected: 비DB 테스트 통과·MySQL opt-in 건너뜀, observed: 134 tests 중 122 passed·12 skipped·0 failed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: .\\gradlew.bat test, environment: actual local branch, input: 전체 회귀 테스트, expected: 전체 자동 테스트 통과, observed: 170 tests 중 158 passed·12 skipped·0 failed, result: PASSED, evidence_ref: build/test-results/test}
unperformed_checks:
  - {command: "$env:RUN_MYSQL_VIEW_COUNT_WRITE_TESTS='true'; .\\gradlew.bat test --tests com.youthguide.api.review.repository.CommunityPostViewCountMySqlIntegrationTest", reason_ko: 실제 local MySQL DML은 사용자 환경에서 명시적으로 실행하도록 분리했고 이번 작업에서는 실행하지 않음}
  - {command: 로컬 MySQL 대상 병렬 상세 요청, reason_ko: 실제 DB 상태를 변경하는 동시성 검증은 이번 자동 실행 범위에서 제외됨}
state_before: {implementation_status: IMPLEMENTED, work_status: IN_REVIEW, verification_status: PARTIAL}
state_after: {implementation_status: IMPLEMENTED, work_status: IN_REVIEW, verification_status: PARTIAL}
remaining_work:
  - 사람 코드 검토
  - 로컬 MySQL opt-in 테스트와 병렬 상세 요청 실행
next_action: 사용자에게 opt-in 실행 조건과 수동 확인 항목을 인계한다.
state_updates:
  - {path: docs/features/WBS-12-05-REVIEW-VIEW.md, result: APPLIED, evidence_ref: check_evidence}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-12-05-REVIEW-VIEW entry}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-12-05-REVIEW-VIEW active task}
missing_reasons: {}
```
