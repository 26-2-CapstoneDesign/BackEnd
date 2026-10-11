# WBS 10.03 시험후기 좋아요 API

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 인증 회원의 시험후기 좋아요 등록·취소 API를 기존 명령 계층과 community_post_like 복합 PK를 사용해 멱등적으로 구현했다. 자동 테스트와 assemble은 통과했고 실제 MySQL 쓰기 통합 검증은 수행하지 않았다.
feature_id: WBS-10-03-REVIEW-LIKE
name: 시험후기 좋아요 API
human_owner: current_user
actor_id: codex
participants: [current_user, codex]
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-11T13:03:00Z'
source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035 (+ 미커밋 10.03 변경)
feature_file: docs/features/WBS-10-03-REVIEW-LIKE.md
latest_worklog: docs/worklogs/WBS-10-03-REVIEW-LIKE__codex__20261011T125740Z.md
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
  deployment_status: 사용자가 commit·push·배포를 요청하지 않았다.
goal: 로그인 사용자가 EXAM_REVIEW 게시글의 좋아요를 멱등적으로 등록·취소하고 현재 DB row 수를 응답받는다.
scope:
  - PUT /api/v1/community/posts/{postId}/likes/me
  - DELETE /api/v1/community/posts/{postId}/likes/me
  - LoginMember 기반 회원 식별
  - EXAM_REVIEW 게시글 PESSIMISTIC_WRITE 잠금
  - community_post_like 조건부 INSERT·DELETE 및 현재 COUNT 조회
  - liked와 likeCount 직접 응답
excluded_scope:
  - 좋아요 Entity·복합 ID Entity와 DB·Flyway 변경
  - 목록·상세 likeCount 쿼리 및 DTO 변경
  - 상세 likedByMe와 좋아요 게시글 목록 API
  - 09.01~09.03 계약 변경
dependency_refs:
  - WBS-09-01-REVIEW-QUERY
  - WBS-09-02-VERSION-CONFLICT
  - WBS-09-03-REVIEW-DELETE
unknowns: []
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 사용자 요청
acceptance_criteria:
  - {ac_id: AC-01, condition: 미좋아요 상태에서 PUT, expected: row 1건 생성·liked true·현재 likeCount 반환, status: MET, evidence_refs: [CommunityPostLikeServiceTest.addsFirstLikeAndReturnsCurrentCountInRepositoryOrder], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-02, condition: 이미 좋아요한 상태에서 PUT, expected: 중복 생성 없이 liked true·count 유지, status: MET, evidence_refs: [CommunityPostLikeServiceTest.keepsRepeatedPutIdempotentWithoutDuplicateInsert], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-03, condition: 좋아요 상태에서 DELETE, expected: 해당 row 삭제·liked false·현재 likeCount 반환, status: MET, evidence_refs: [CommunityPostLikeServiceTest.removesExistingLikeAndReturnsCurrentCountInRepositoryOrder], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-04, condition: 미좋아요 상태에서 DELETE, expected: 오류 없이 liked false·count 유지, status: MET, evidence_refs: [CommunityPostLikeServiceTest.keepsRepeatedDeleteIdempotentWithoutDeleteQuery], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-05, condition: 게시글 조회, expected: postId와 EXAM_REVIEW 조건으로 배타 잠금하며 미존재·다른 유형은 POST_NOT_FOUND, status: MET, evidence_refs: [CommunityPostLikeServiceTest.returnsPostNotFoundBeforeLikeQueriesWhenTargetIsMissing, CommunityPostLikeServiceTest.treatsPostOutsideExamReviewScopeAsNotFound], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-06, condition: 인증 회원 요청, expected: LoginMember.memberId만 사용하고 요청 body 없이 처리, status: MET, evidence_refs: [CommunityPostCommandControllerTest.addsAndRemovesLikeWithAuthenticatedMemberAndCsrf], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-07, condition: 비로그인 또는 CSRF 누락 요청, expected: 기존 Security 정책의 401 또는 403 응답, status: MET, evidence_refs: [CommunityPostCommandControllerTest.rejectsUnauthenticatedLikeCommands, CommunityPostCommandControllerTest.rejectsLikeCommandsWithoutCsrf], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-08, condition: 좋아요 처리 트랜잭션, expected: 잠금·존재 확인·조건부 DML·COUNT를 한 트랜잭션에서 수행, status: MET, evidence_refs: [CommunityPostLikeServiceTest.addsFirstLikeAndReturnsCurrentCountInRepositoryOrder, CommunityPostLikeServiceTest.usesExistingTransactionalServiceBoundary], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-09, condition: 기존 조회 API, expected: community_post_like row 변화가 기존 목록·상세 likeCount에 반영되고 해당 쿼리는 변경하지 않음, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest.likeCommandsUseCompositeKeyParametersAndDatabaseCount, CommunityPostQueryRepositoryTest], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
  - {ac_id: AC-10, condition: 회귀 및 빌드 검증, expected: 전체 test·assemble·git diff --check 통과, status: MET, evidence_refs: [docs/worklogs/WBS-10-03-REVIEW-LIKE__codex__20261011T125740Z.md#EVT-VERIFY-002], source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035}
components:
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: addMyLike/removeMyLike, role: LoginMember를 Service에 전달하고 DTO를 직접 반환, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-10-03-REVIEW-LIKE]}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: addLike/removeLike, role: EXAM_REVIEW 잠금·멱등 처리·현재 count 반환, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-10-03-REVIEW-LIKE]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: community_post_like commands/counts, role: 복합키 조건 native 조회·INSERT·DELETE·COUNT, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-10-03-REVIEW-LIKE]}
  - {path: src/main/java/com/youthguide/api/review/dto/CommunityPostLikeResponse.java, symbol: CommunityPostLikeResponse, role: liked와 likeCount 직접 응답, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-10-03-REVIEW-LIKE]}
connections:
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: addMyLike/removeMyLike}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: addLike/removeLike, external_boundary: HTTP}, payload: postId와 LoginMember.memberId, contract_ref: PUT·DELETE /api/v1/community/posts/{postId}/likes/me, purpose: 인증 회원의 좋아요 등록·취소, verification_evidence: [CommunityPostCommandControllerTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: addLike/removeLike}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: lock/like DML/count, external_boundary: MySQL}, payload: postId·memberId·EXAM_REVIEW, contract_ref: community_post_like 복합 PK, purpose: 동시성 직렬화와 멱등 처리, verification_evidence: [CommunityPostLikeServiceTest, CommunityPostCommandRepositoryTest]}
verified_flow: 인증·CSRF → LoginMember → PESSIMISTIC_WRITE EXAM_REVIEW 조회 → 복합키 존재 확인 → 조건부 INSERT/DELETE → 같은 트랜잭션 COUNT → liked·likeCount 200
verification_scope: MIXED
required_checks:
  - 전체 자동 테스트
  - assemble
  - git diff --check
check_evidence:
  - 신규 Service·Repository·Controller 테스트 31건 통과
  - 전체 125건 중 115건 통과, MySQL opt-in 10건 건너뜀, 실패 0건
  - assemble BUILD SUCCESSFUL
  - git diff --check 통과
exception_coverage:
  - POST_NOT_FOUND 404
  - 기존 UNAUTHENTICATED 401
  - 기존 ACCESS_DENIED 403
  - 기존 INVALID_INPUT 400
blockers: []
resume_when: []
next_action: 사용자 검토 후 격리된 MySQL에서 실제 native INSERT·DELETE·COUNT 통합 검증 필요 여부를 결정한다.
remaining_work:
  - 사람 검토
  - 격리된 MySQL 실제 쓰기 통합 검증
completion_requires: {review_status: APPROVED, integration_status: null, deployment_status: NOT_APPLICABLE}
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-10-03-REVIEW-LIKE__codex__20261011T125740Z.md#EVT-IMPLEMENTED-003
limitations:
  - 공용 DB 변경 금지에 따라 실제 MySQL INSERT·DELETE와 동시 요청 통합 검증은 수행하지 않았다.
  - Repository 테스트는 SQL annotation 구조를, Service 테스트는 Repository 호출 순서와 멱등 분기를 보장한다.
handoff: application-local.yaml, DB·Flyway·SecurityConfig와 기존 09.01~09.03 조회·명령 계약을 변경하지 않았다.
```
