# WBS 09.03 시험후기 삭제·연관 처리 작업 기록

```yaml
schema_version: 3.1.0
template: false
record_type: session
summary_ko: 시험후기 게시글을 잠금 조회하고 작성자·요청 버전을 검증한 뒤 좋아요·metric·신고·게시글 순서로 한 트랜잭션에서 삭제하도록 구현했다. 자동 테스트와 assemble은 통과했고 실제 DB 쓰기 검증은 공용 데이터 보호를 위해 수행하지 않았다.
task_id: WBS-09-03-REVIEW-DELETE
session_id: WBS-09-03-REVIEW-DELETE__codex__20261010T165857Z
kind: FEATURE
feature_id: WBS-09-03-REVIEW-DELETE
human_owner: current_user
actor_id: codex
tool: Codex
actual_model: GPT-5
started_at: '2026-10-10T16:58:57Z'
recorded_at: '2026-10-10T16:58:57Z'
timezone: UTC
source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4
existing_edits:
  - src/main/resources/application-local.yaml
rules_version: 3.1.0
read_files:
  - AI_RULES.md
  - docs/CURRENT_STATE.md
  - docs/FEATURE_STATUS.md
  - docs/NAMING.md
  - docs/TERMS.md
  - docs/DECISIONS.md
  - docs/features/WBS-09-02-VERSION-CONFLICT.md
  - docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md
  - src/main/java/com/youthguide/api/review
  - src/main/java/com/youthguide/api/global/exception
  - src/main/java/com/youthguide/api/global/config/SecurityConfig.java
goal: 인증된 작성자가 예상 버전을 제시해 시험후기와 좋아요·metric·신고 연관 행을 안전하게 물리 삭제한다.
scope:
  - DELETE /api/v1/community/posts/{postId}
  - LoginMember 작성자 확인
  - X-Expected-Post-Version 필수 헤더
  - PESSIMISTIC_WRITE 잠금과 명시적 version 비교
  - community_post_like, community_post_metric, community_post_report, community_post 순서 삭제
  - 공통 POST_NOT_FOUND, ACCESS_DENIED, VERSION_CONFLICT, INVALID_INPUT 응답
approval_refs:
  - 현재 대화의 사용자 요청
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_APPLICABLE
latest_event_id: EVT-IMPLEMENTED-003
previous_log: docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md
remaining_work:
  - 사람 검토
  - 격리된 MySQL에서 실제 삭제·롤백 통합 검증
resume_when: []
next_action: 사용자 검토 후 필요하면 격리된 MySQL 테스트 환경에서 실제 삭제·롤백을 검증한다.
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 commit·push·배포를 요청하지 않았다.
```

```yaml
template: false
record_type: event
summary_ko: 삭제 API의 Controller·Service·Repository 구조와 공통 오류 매핑을 구현하고 자동 테스트를 실행했다.
event_id: EVT-VERIFY-002
event_type: VERIFY
actor_id: codex
recorded_at: '2026-10-10T17:03:27Z'
occurred_at: '2026-10-10T17:00:51Z'
source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4 (+ 미커밋 09.03 변경)
rule_refs:
  - {rule_id: C04, rule_text: "MUST NOT invent successful results.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - PESSIMISTIC_WRITE 잠금 조회와 EXAM_REVIEW 조건을 추가했다.
  - 좋아요·metric·신고를 먼저 삭제하고 마지막에 CommunityPost를 삭제하도록 구현했다.
  - 누락·형식 오류 version 헤더를 공통 INVALID_INPUT 400으로 연결했다.
  - Repository 호출 순서와 예외 전파·트랜잭션 경계를 단위 테스트로 확인했다.
changes:
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: findForUpdateByPostIdAndPostKind/deletePost*ByPostId, operation: MODIFY, reason: 잠금 조회와 RESTRICT 자식 행 선삭제}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost, operation: MODIFY, reason: 존재·작성자·version 검증과 삭제 순서 제어}
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: deletePost, operation: MODIFY, reason: DELETE API와 LoginMember·헤더 연결}
  - {path: src/main/java/com/youthguide/api/global/exception/GlobalExceptionHandler.java, symbol: handleBadRequest, operation: MODIFY, reason: 헤더 누락·형식 오류 공통 400 처리}
checks:
  - {command: ".\\gradlew.bat test --tests com.youthguide.api.review.service.CommunityPostDeleteServiceTest --tests com.youthguide.api.review.repository.CommunityPostCommandRepositoryTest --tests com.youthguide.api.review.controller.CommunityPostCommandControllerTest", environment: actual local branch, input: 신규 삭제 기능 테스트, expected: 전체 통과, observed: BUILD SUCCESSFUL, result: PASSED, evidence_ref: build/test-results/test}
  - {command: ".\\gradlew.bat test --tests com.youthguide.api.review.*", environment: actual local branch, input: review 회귀 테스트, expected: DB 비의존 테스트 통과, observed: 64 tests, 54 passed, 10 MySQL opt-in skipped, 0 failed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: ".\\gradlew.bat test --tests com.youthguide.api.review.* --tests com.youthguide.api.user.*", environment: actual local branch, input: user와 review 회귀 테스트, expected: DB 비의존 테스트 통과, observed: 88 tests, 78 passed, 10 MySQL opt-in skipped, 0 failed, result: PASSED, evidence_ref: build/test-results/test}
  - {command: ".\\gradlew.bat assemble", environment: actual local branch, input: main source, expected: 패키징 성공, observed: BUILD SUCCESSFUL, result: PASSED, evidence_ref: Gradle output}
  - {command: git diff --check, environment: actual local branch, input: 추적 변경 파일, expected: whitespace 오류 없음, observed: 출력 없음, result: PASSED, evidence_ref: terminal output}
unperformed_checks:
  - {command: 실제 MySQL DELETE 후 재조회 및 롤백, reason_ko: 기존 테스트 런타임에 H2·Testcontainers가 없고 공용 DB 변경이 금지되어 수행하지 않음}
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: NOT_RUN}
state_after: {implementation_status: IMPLEMENTED, work_status: IN_REVIEW, verification_status: PARTIAL}
remaining_work:
  - 사람 검토
  - 격리 DB 실제 쓰기 통합 검증
next_action: 기능·상태 문서를 완료 상태로 갱신한다.
state_updates:
  - {path: docs/features/WBS-09-03-REVIEW-DELETE.md, result: NOT_APPLIED, evidence_ref: null}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 승인된 09.03 삭제 API 구현과 필수 비DB 검증을 완료했으며 사람 검토를 기다린다.
event_id: EVT-IMPLEMENTED-003
event_type: IMPLEMENTED
actor_id: codex
recorded_at: '2026-10-10T17:03:27Z'
occurred_at: '2026-10-10T17:03:27Z'
source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4 (+ 미커밋 09.03 변경)
rule_refs:
  - {rule_id: C17, rule_text: "ON IMPLEMENTED/COMPLETE: record feature/output, acceptance evidence, file-symbol edges, payload/contract, verified flow, exceptions, limitations, review and handoff.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - 09.03 feature 문서와 상태 문서에 구현·검증 결과를 반영했다.
  - 기존 application-local.yaml 변경을 수정하지 않았다.
changes:
  - {path: docs/features/WBS-09-03-REVIEW-DELETE.md, symbol: feature record, operation: MODIFY, reason: 구현과 검증 근거 기록}
  - {path: docs/FEATURE_STATUS.md, symbol: WBS-09-03-REVIEW-DELETE, operation: MODIFY, reason: 구현 완료·검토 대기 상태 반영}
  - {path: docs/CURRENT_STATE.md, symbol: WBS-09-03-REVIEW-DELETE, operation: MODIFY, reason: 최신 작업 상태 반영}
checks: []
unperformed_checks:
  - {command: commit/push/merge, reason_ko: 사용자 지시로 수행 금지}
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE, review_status: PENDING}
state_after: {implementation_status: IMPLEMENTED, work_status: IN_REVIEW, review_status: PENDING}
remaining_work:
  - 사람 검토
  - 격리 DB 실제 쓰기 통합 검증
next_action: 변경 파일과 검증 한계를 사용자에게 보고한다.
state_updates:
  - {path: docs/features/WBS-09-03-REVIEW-DELETE.md, result: APPLIED, evidence_ref: acceptance_criteria}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-09-03-REVIEW-DELETE entry}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-09-03-REVIEW-DELETE active task}
missing_reasons: {}
completion:
  feature_id: WBS-09-03-REVIEW-DELETE
  outputs:
    - DELETE /api/v1/community/posts/{postId}
    - X-Expected-Post-Version과 LoginMember 검증
    - PESSIMISTIC_WRITE 잠금 및 자식 우선 물리 삭제
  delivered_behavior: 인증된 작성자가 현재 version을 제시하면 좋아요·metric·신고와 시험후기 게시글이 한 트랜잭션에서 삭제된다.
  scope:
    - WBS 09.03 삭제·연관 처리
  excluded_scope:
    - DB·Flyway·SecurityConfig·application 설정 변경
    - 09.01 리뷰 변경 및 다른 WBS 기능
  acceptance_results:
    - {ac_id: AC-01, status: MET, evidence_refs: [CommunityPostCommandControllerTest.deletesOwnedPostWithExpectedVersion]}
    - {ac_id: AC-02, status: MET, evidence_refs: [CommunityPostDeleteServiceTest.deletesAssociationsThenOwnedPostWhenVersionMatches]}
    - {ac_id: AC-03, status: MET, evidence_refs: [CommunityPostCommandControllerTest.mapsDeleteStatusesToCommonErrors]}
    - {ac_id: AC-04, status: MET, evidence_refs: [CommunityPostCommandControllerTest.mapsDeleteStatusesToCommonErrors]}
    - {ac_id: AC-05, status: MET, evidence_refs: [CommunityPostCommandControllerTest.mapsDeleteStatusesToCommonErrors]}
    - {ac_id: AC-06, status: MET, evidence_refs: [CommunityPostCommandControllerTest.rejectsMissingExpectedVersionHeader]}
    - {ac_id: AC-07, status: MET, evidence_refs: [CommunityPostCommandControllerTest.rejectsMalformedExpectedVersionHeader]}
    - {ac_id: AC-08, status: MET, evidence_refs: [CommunityPostDeleteServiceTest.deletesAssociationsThenOwnedPostWhenVersionMatches]}
    - {ac_id: AC-09, status: MET, evidence_refs: [CommunityPostDeleteServiceTest.deletesAssociationsThenOwnedPostWhenVersionMatches]}
    - {ac_id: AC-10, status: MET, evidence_refs: [CommunityPostDeleteServiceTest.deletesAssociationsThenOwnedPostWhenVersionMatches]}
    - {ac_id: AC-11, status: MET, evidence_refs: [CommunityPostDeleteServiceTest.propagatesFailureBeforeParentDeleteForTransactionRollback]}
    - {ac_id: AC-12, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest.deleteLookupUsesPessimisticWriteLockAndPostKind]}
  source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4 (+ 미커밋 09.03 변경)
  connections:
    - from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: deletePost}
      to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost, external_boundary: HTTP}
      payload: postId, LoginMember.memberId, X-Expected-Post-Version
      contract_ref: DELETE /api/v1/community/posts/{postId}
      purpose: 인증 작성자의 version 기반 삭제
      verification_evidence: [CommunityPostCommandControllerTest]
    - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost}
      to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: findForUpdate/delete relation/delete post, external_boundary: MySQL}
      payload: postId와 EXAM_REVIEW
      contract_ref: RESTRICT 자식 우선 삭제
      purpose: 경쟁 삽입 차단과 FK 안전 삭제
      verification_evidence: [CommunityPostDeleteServiceTest, CommunityPostCommandRepositoryTest]
  verified_flow: SecurityConfig 인증·CSRF → LoginMember → PESSIMISTIC_WRITE 게시글 조회 → 작성자 → version → like → metric → report → post 삭제 → 204
  verification_scope: MIXED
  exception_coverage:
    - 미존재 POST_NOT_FOUND 404
    - 작성자 불일치 ACCESS_DENIED 403
    - 요청 또는 JPA version 충돌 VERSION_CONFLICT 409
    - 헤더 누락·형식 오류 INVALID_INPUT 400
  limitations:
    - 실제 MySQL DELETE·재조회·롤백은 공용 DB 변경 금지와 격리 DB 의존성 부재로 실행하지 않았다.
  remaining_work:
    - 사람 검토
    - 격리된 MySQL 실제 쓰기 통합 검증
  review_status: PENDING
  review_evidence: []
  integration_status: BRANCH_ONLY
  deployment_status: NOT_APPLICABLE
  handoff: application-local.yaml과 DB·Flyway·SecurityConfig는 변경하지 않았으며 09.01 최신 리뷰 커밋도 병합하지 않았다.
```

```yaml
template: false
record_type: event
summary_ko: 브랜치와 기존 변경을 확인하고 삭제 API·권한·버전·연관 삭제 정책의 승인 범위 안에서 구현을 시작했다.
event_id: EVT-START-001
event_type: START
actor_id: codex
recorded_at: '2026-10-10T16:58:57Z'
occurred_at: '2026-10-10T16:58:57Z'
source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4
rule_refs:
  - {rule_id: C05, rule_text: "MAY make small reversible internal edits ONLY within approved scope, preserving public behavior/contracts and others' edits.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - feature-09-03-review-delete 브랜치와 기존 application-local.yaml 변경을 확인했다.
  - 09.02의 LoginMember, EXAM_REVIEW, 공통 예외와 결과 상태 패턴을 확인했다.
  - 부모 게시글 잠금 후 자식 행을 먼저 삭제하고 마지막에 게시글을 삭제하는 최소 구조를 선정했다.
changes:
  - {path: docs/features/WBS-09-03-REVIEW-DELETE.md, symbol: feature record, operation: ADD, reason: 승인된 09.03 계약 등록}
checks: []
unperformed_checks:
  - {command: review 테스트, reason_ko: 구현 후 실행 예정}
  - {command: user + review 회귀 테스트, reason_ko: 구현 후 실행 예정}
  - {command: assemble, reason_ko: 구현 후 실행 예정}
state_before: {implementation_status: NOT_STARTED, work_status: QUEUED, review_status: NOT_REQUESTED}
state_after: {implementation_status: IMPLEMENTING, work_status: ACTIVE, review_status: PENDING}
remaining_work:
  - 코드와 테스트 구현
  - 검증 및 완료 기록
next_action: Repository 잠금·삭제 쿼리와 Service 처리 순서를 구현한다.
state_updates:
  - {path: docs/features/WBS-09-03-REVIEW-DELETE.md, result: NOT_APPLIED, evidence_ref: null}
missing_reasons: {}
```
