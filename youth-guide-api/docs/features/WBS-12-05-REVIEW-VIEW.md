# WBS 12.05 시험후기 조회수

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 공개 시험후기 상세 API를 유지하면서 로그인 사용자의 유효한 상세 요청마다 기존 community_post_metric 조회수를 원자적으로 1 증가하도록 구현했다. 로컬 loopback 전용 opt-in UPSERT 테스트를 분리했고 기본 자동 테스트와 assemble은 통과했으며 실제 쓰기·동시 요청 실행 검증은 남아 있다.
feature_id: WBS-12-05-REVIEW-VIEW
name: 시험후기 조회수
human_owner: current_user
actor_id: codex
participants: [current_user, codex]
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-16T15:12:33Z'
source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae (+ 미커밋 12.05 변경)
feature_file: docs/features/WBS-12-05-REVIEW-VIEW.md
latest_worklog: docs/worklogs/WBS-12-05-REVIEW-VIEW__codex__20261016T095526Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 10
acceptance_total: 10
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_APPLICABLE
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 commit·push·merge·배포를 금지했다.
goal: 비로그인 공개 상세를 유지하고 로그인 사용자의 EXAM_REVIEW 상세 요청마다 기존 metric 조회수를 안전하게 1 증가한다.
scope:
  - GET /api/v1/community/posts/{postId}
  - nullable LoginMember 기반 로그인 여부 판별
  - community_post_metric 행 생성 또는 view_count 원자 증가
  - EXAM_REVIEW 내부 고정과 기존 POST_NOT_FOUND
  - 기존 목록·상세 viewCount 조회 및 DTO 유지
excluded_scope:
  - 사용자별 1회 제한과 조회 이력 저장
  - community_post_view 테이블·Entity·Repository
  - API 요청·응답과 SecurityConfig 변경
  - DB·Flyway·삭제 순서 변경
dependency_refs:
  - WBS-09-01-REVIEW-QUERY
  - WBS-09-03-REVIEW-DELETE
  - WBS-10-03-REVIEW-LIKE
  - WBS-12-04-REVIEW-COMMENT
  - src/main/java/com/youthguide/api/global/security/LoginMember.java
unknowns:
  - 로컬 MySQL 8.4 opt-in 테스트의 실제 실행 결과와 동시 요청 누적 결과
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 사용자 요청
acceptance_criteria:
  - {ac_id: AC-01, condition: 비로그인 상세 조회, expected: 상세 200과 조회수 증가 명령 미호출, status: MET, evidence_refs: [CommunityPostQueryControllerTest, CommunityPostQuerySecurityTest.allowsAnonymousDetailRequest], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-02, condition: 같은 로그인 사용자의 반복 상세 조회, expected: 요청 횟수만큼 매번 증가 명령 실행, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.incrementsViewCountForEveryRepeatedAuthenticatedDetailRequest, CommunityPostViewCountServiceTest.incrementsAgainForEveryRepeatedRequest], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-03, condition: 다른 로그인 사용자와 작성자 본인 조회, expected: 사용자 식별값과 무관하게 요청마다 증가, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.incrementsViewCountForDifferentAuthenticatedMembersIncludingAuthor], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-04, condition: 미존재 또는 다른 postKind, expected: metric 생성·상세 조회 없이 POST_NOT_FOUND, status: MET, evidence_refs: [CommunityPostViewCountServiceTest.rejectsMissingOrNonExamReviewWithoutCreatingMetric, CommunityPostQuerySecurityTest.returnsNotFoundBeforeDetailQueryWhenAuthenticatedTargetIsInvalid], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-05, condition: metric 행이 없는 유효 게시글, expected: INSERT SELECT로 view_count 1 행 생성, status: MET, evidence_refs: [CommunityPostViewCountServiceTest.incrementsViewCountWithoutPessimisticPostLock, CommunityPostCommandRepositoryTest.viewCountIncrementUsesAtomicUpsertAndPostKindGuard], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-06, condition: 기존 양수 metric과 동시 증가, expected: 별도 비관적 락 없이 DB의 view_count = view_count + 1 원자 연산 사용, status: MET, evidence_refs: [CommunityPostViewCountServiceTest.acceptsExistingMetricRowUpdateResult, CommunityPostCommandRepositoryTest.viewCountIncrementUsesAtomicUpsertAndPostKindGuard], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-07, condition: 로그인 상세 응답, expected: 증가 명령 뒤 기존 상세 조회가 실행되어 기존 viewCount 필드로 응답, status: MET, evidence_refs: [CommunityPostQuerySecurityTest.incrementsViewCountForEveryRepeatedAuthenticatedDetailRequest], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-08, condition: 목록·검색 조회, expected: 로그인 여부와 관계없이 조회수 증가 없음, status: MET, evidence_refs: [CommunityPostQueryControllerTest, CommunityPostQuerySecurityTest.keepsAuthenticatedListAndSearchFromIncrementingViewCount], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-09, condition: 기존 계약과 연관 기능, expected: DTO·SecurityConfig·DB·Flyway·삭제 흐름 변경 없음, status: MET, evidence_refs: [git diff --check], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
  - {ac_id: AC-10, condition: 자동 회귀와 패키징, expected: 대상·review·전체 test와 assemble·git diff --check 통과, status: MET, evidence_refs: [docs/worklogs/WBS-12-05-REVIEW-VIEW__codex__20261016T095526Z.md#EVT-IMPLEMENTED-002], source_revision: a03cc5c3f26ba5bff26a3a935f53ed3e01e0d3ae}
components:
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPost, role: nullable LoginMember 확인 후 인증 요청만 증가시키고 상세 조회, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-05-REVIEW-VIEW]}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: incrementViewCount, role: EXAM_REVIEW metric 증가와 영향 행 0건 POST_NOT_FOUND 처리, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL 영향 행 수 검증], task_refs: [WBS-12-05-REVIEW-VIEW]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: incrementPostViewCount, role: 게시글 조건부 INSERT SELECT ON DUPLICATE KEY 원자 증가, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL UPSERT·동시성 검증], task_refs: [WBS-12-05-REVIEW-VIEW]}
  - {path: src/test/java/com/youthguide/api/review/repository/CommunityPostViewCountMySqlIntegrationTest.java, symbol: CommunityPostViewCountMySqlIntegrationTest, role: 환경변수·loopback URL·rollback으로 제한한 로컬 MySQL UPSERT opt-in 검증, implementation_status: IMPLEMENTED, remaining_work: [로컬 환경에서 명시적 실행], task_refs: [WBS-12-05-REVIEW-VIEW]}
connections:
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPost}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: incrementViewCount, external_boundary: HTTP}, payload: postId와 nullable LoginMember 존재 여부, contract_ref: GET /api/v1/community/posts/{postId}, purpose: 인증 상세 요청만 증가, verification_evidence: [CommunityPostQuerySecurityTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: incrementViewCount}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: incrementPostViewCount, external_boundary: MySQL}, payload: postId·EXAM_REVIEW, contract_ref: community_post_metric post_id PK, purpose: 유효 대상의 metric 원자 증가, verification_evidence: [CommunityPostViewCountServiceTest, CommunityPostCommandRepositoryTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPost}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostQueryService.java, symbol: getPost, external_boundary: MySQL}, payload: postId, contract_ref: 기존 CommunityPostDetailResponse, purpose: 증가 후 현재 viewCount 상세 응답, verification_evidence: [CommunityPostQuerySecurityTest]}
verified_flow: 공개 GET → nullable LoginMember 확인 → 인증이면 EXAM_REVIEW 조건 metric UPSERT 트랜잭션 커밋 → 기존 상세 native 조회 → 기존 viewCount 필드 직접 반환; 비로그인이면 기존 상세 조회만 실행
verification_scope: MIXED
required_checks:
  - 12.05 대상 자동 테스트
  - review 관련 자동 테스트
  - 전체 자동 테스트
  - assemble
  - git diff --check
check_evidence:
  - 12.05 대상 5개 클래스 28건 중 26건 통과, 로컬 쓰기 opt-in 2건 건너뜀, 실패·오류 0건
  - review 테스트 134건 중 122건 통과, MySQL opt-in 12건 건너뜀, 실패·오류 0건
  - 전체 테스트 170건 중 158건 통과, MySQL opt-in 12건 건너뜀, 실패·오류 0건
  - assemble BUILD SUCCESSFUL
  - git diff --check 통과
exception_coverage:
  - POST_NOT_FOUND 404
  - 비로그인 공개 상세 유지
blockers: []
resume_when: []
next_action: 사용자 코드 검토 후 로컬 또는 격리된 MySQL에서 metric 행 유무와 동시 요청을 검증한다.
remaining_work:
  - 실제 MySQL native UPSERT·동시 요청 통합 검증
  - 사람 코드 검토
completion_requires: {review_status: APPROVED, integration_status: null, deployment_status: NOT_APPLICABLE}
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-12-05-REVIEW-VIEW__codex__20261016T095526Z.md#EVT-IMPLEMENTED-002
limitations:
  - 공용 DB 보호 원칙에 따라 실제 MySQL INSERT·UPDATE와 동시 요청을 실행하지 않았다.
  - 전체 test의 기존 contextLoads는 로컬 MySQL 컨텍스트를 열었지만 12.05 UPSERT DML은 실행하지 않았고, MySQL opt-in 12건은 건너뛰었다.
  - 조회수 쓰기 opt-in 테스트는 RUN_MYSQL_VIEW_COUNT_WRITE_TESTS=true, loopback JDBC URL, 테스트 트랜잭션 rollback 조건을 모두 적용했지만 이번 작업에서는 실행하지 않았다.
  - Repository 테스트는 SQL annotation 구조를, Service·Controller 테스트는 분기·반복·호출 순서를 보장한다.
handoff: 로그인 사용자당 1회 조회 이력 정책은 폐기되었고 로그인 상세 요청마다 증가가 최종 승인 정책이다. application-local.yaml, SecurityConfig, DTO, DB·Flyway, V1 migration과 기존 삭제 흐름을 변경하지 않았다.
```
