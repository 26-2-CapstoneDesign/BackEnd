# WBS 12.04 시험후기 댓글 작성·조회·수정·삭제

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 시험후기 댓글과 유튜브식 1단계 대댓글, 구조화된 reply 대상, 혼합 삭제, commentCount 및 baseline 0 기준 V1 Flyway migration을 구현하고 자동 테스트를 통과했다. 실제 MySQL migration·FK·잠금 검증은 공용 DB 미접속 원칙에 따라 수행하지 않았다.
feature_id: WBS-12-04-REVIEW-COMMENT
name: 시험후기 댓글 작성·조회·수정·삭제
human_owner: current_user
actor_id: codex
participants: [current_user, codex]
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-15T01:52:14Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
feature_file: docs/features/WBS-12-04-REVIEW-COMMENT.md
latest_worklog: docs/worklogs/WBS-12-04-REVIEW-COMMENT__codex__20261012T134019Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 14
acceptance_total: 14
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_DEPLOYED
missing_reasons: {}
not_applicable_reasons: {}
goal: EXAM_REVIEW 상세에서 댓글과 1단계 대댓글을 조회하고 인증 회원이 본인 댓글을 작성·수정·삭제하도록 한다.
scope:
  - GET /api/v1/community/posts/{postId}/comments
  - POST /api/v1/community/posts/{postId}/comments
  - POST /api/v1/community/posts/{postId}/comments/{commentId}/replies
  - PUT /api/v1/community/posts/{postId}/comments/{commentId}
  - DELETE /api/v1/community/posts/{postId}/comments/{commentId}
  - scalar FK CommunityPostComment Entity와 @Version
  - 최상위 댓글 기준 pagination과 대댓글 일괄 조회
  - parent_comment_id 루트 평탄화와 reply_to_comment_id 기반 구조화된 답글 대상
  - 대댓글 1단계 무결성 방어와 혼합 삭제·tombstone 정리
  - 게시글 commentCount와 게시글 삭제 연계
  - LoginMember·CSRF·공통 예외·EXAM_REVIEW 재사용
excluded_scope:
  - Flyway baseline/version 임의 결정과 공용 DB 적용
  - application-local.yaml 및 비밀값 변경
  - 09.01~10.03 계약의 댓글 연계 외 변경
  - 2단계 이상 대댓글과 댓글 좋아요·신고·첨부
dependency_refs:
  - WBS-09-01-REVIEW-QUERY
  - WBS-09-02-VERSION-CONFLICT
  - WBS-09-03-REVIEW-DELETE
  - WBS-10-03-REVIEW-LIKE
  - src/main/java/com/youthguide/api/global/security/LoginMember.java
  - src/main/java/com/youthguide/api/global/exception
unknowns:
  - 실제 MySQL에서 V1 migration, native IN collection binding, self FK 삭제 순서와 잠금의 통합 검증 결과
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 사용자 요청
acceptance_criteria:
  - {ac_id: AC-01, condition: 댓글 목록 조회, expected: 최상위 댓글을 0-based page로 조회하고 해당 page의 대댓글을 한 번에 조회해 오래된 순으로 중첩 반환, status: MET, evidence_refs: [CommunityPostCommentQueryServiceTest, CommunityPostCommentRepositoryTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-02, condition: 비로그인·로그인 댓글 조회, expected: 비로그인은 mine=false이고 로그인 사용자는 본인 댓글만 mine=true, status: MET, evidence_refs: [CommunityPostCommentControllerTest, CommunityPostCommentQueryServiceTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-03, condition: 최상위 댓글 작성, expected: LoginMember와 EXAM_REVIEW 잠금 확인 후 scalar ID 댓글 생성, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest, CommunityPostCommentControllerTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-04, condition: 대댓글 작성, expected: 최상위 대상은 parent=root·replyTo=null로 저장하고 대댓글 대상은 parent=root·replyTo=실제 대상으로 평탄화해 DB 깊이를 1단계로 유지, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest, CommunityPostCommentControllerTest, CommunityPostCommentQueryServiceTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-05, condition: 댓글 body 입력, expected: 공백을 거부하고 255자는 허용하며 256자는 INVALID_INPUT 400, status: MET, evidence_refs: [CommunityPostCommentControllerTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-06, condition: 본인 댓글 수정, expected: 명시적 version 확인 후 body와 UTC updatedAt 변경, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest, CommunityPostCommentControllerTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-07, condition: 댓글 삭제, expected: 대댓글 없는 부모와 대댓글은 물리 삭제하고 대댓글 있는 부모는 원문 제거·deletedAt 설정, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-08, condition: tombstone의 마지막 대댓글 삭제, expected: 남은 대댓글이 없으면 tombstone 부모도 물리 삭제, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-09, condition: 게시글 삭제, expected: 대댓글→최상위 댓글→기존 연관 행→게시글 순서로 한 트랜잭션에서 삭제, status: MET, evidence_refs: [CommunityPostDeleteServiceTest, CommunityPostCommandRepositoryTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-10, condition: 목록·상세 게시글 조회, expected: deletedAt이 없는 최상위 댓글과 대댓글 전체 수를 commentCount로 반환, status: MET, evidence_refs: [CommunityPostQueryServiceTest, CommunityPostQueryRepositoryTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-11, condition: 권한·대상·깊이 오류, expected: 기존 POST_NOT_FOUND·ACCESS_DENIED·INVALID_INPUT·VERSION_CONFLICT와 신규 COMMENT_NOT_FOUND를 사용하고 COMMENT_DEPTH_EXCEEDED는 비정상 depth 2 이상 데이터의 무결성 방어에만 사용, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest, CommunityPostCommentControllerTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-12, condition: 댓글 write 경쟁, expected: 게시글 PESSIMISTIC_WRITE 후 댓글 사전 version과 JPA @Version을 적용하고 게시글 version은 변경하지 않음, status: MET, evidence_refs: [CommunityPostCommentCommandServiceTest, CommunityPostCommentRepositoryTest, CommunityPostCommentControllerTest], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-13, condition: 자동 회귀 검증, expected: 전체 test와 review test 통과, status: MET, evidence_refs: [docs/worklogs/WBS-12-04-REVIEW-COMMENT__codex__20261012T134019Z.md#EVT-VERIFY-004], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
  - {ac_id: AC-14, condition: 신규 DB 구조, expected: baseline 0과 팀 convention에 맞춘 V1__create_community_post_comment.sql 제공, status: MET, evidence_refs: [src/main/resources/db/migration/V1__create_community_post_comment.sql, docs/worklogs/WBS-12-04-REVIEW-COMMENT__codex__20261012T134019Z.md#EVT-IMPLEMENTED-008], source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24}
components:
  - {path: src/main/java/com/youthguide/api/review/domain/CommunityPostComment.java, symbol: CommunityPostComment, role: parent root·nullable reply target scalar FK·UTC timestamp·deletedAt·@Version 댓글 Entity, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL 타입 정합성 검증], task_refs: [WBS-12-04-REVIEW-COMMENT]}
  - {path: src/main/resources/db/migration/V1__create_community_post_comment.sql, symbol: community_post_comment, role: baseline 0 다음 V1 댓글 테이블·self FK·조회 인덱스 migration, implementation_status: IMPLEMENTED, remaining_work: [로컬 MySQL 적용 검증], task_refs: [WBS-12-04-REVIEW-COMMENT]}
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommentController.java, symbol: CommunityPostCommentController, role: 공개 GET과 LoginMember·CSRF write API 연결, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-04-REVIEW-COMMENT]}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentQueryService.java, symbol: getComments, role: thread page와 replies 일괄 조회·tombstone·mine DTO 변환, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL 조회 검증], task_refs: [WBS-12-04-REVIEW-COMMENT]}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentCommandService.java, symbol: create/update/delete, role: 게시글 잠금·루트 평탄화·실제 reply 대상·작성자·version·혼합 삭제 제어, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL 경쟁·삭제 검증], task_refs: [WBS-12-04-REVIEW-COMMENT]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommentQueryRepository.java, symbol: findTopLevelPage/findRepliesByParentCommentIds, role: 최상위 page와 replies 및 현재 reply 대상 nickname 조회 native SQL, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL 실행 검증], task_refs: [WBS-12-04-REVIEW-COMMENT]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommentCommandRepository.java, symbol: lock/save/delete/count, role: 댓글 PESSIMISTIC_WRITE와 JPA version write, implementation_status: IMPLEMENTED, remaining_work: [실제 MySQL 실행 검증], task_refs: [WBS-12-04-REVIEW-COMMENT]}
connections:
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommentController.java, symbol: getComments}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentQueryService.java, symbol: getComments, external_boundary: HTTP}, payload: postId·page·size·nullable viewerMemberId, contract_ref: 댓글 목록 GET, purpose: 공개 thread 조회와 mine 계산, verification_evidence: [CommunityPostCommentControllerTest, CommunityPostCommentQueryServiceTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommentController.java, symbol: create/update/delete}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentCommandService.java, symbol: create/update/delete, external_boundary: HTTP}, payload: postId·commentId·LoginMember.memberId·body·version, contract_ref: 댓글 write API, purpose: 인증 사용자 댓글 변경, verification_evidence: [CommunityPostCommentControllerTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentCommandService.java, symbol: lockExamReviewPost}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: findForUpdateByPostIdAndPostKind, external_boundary: MySQL}, payload: postId·EXAM_REVIEW, contract_ref: 게시글 삭제 경쟁 방지, purpose: 댓글 write와 게시글 삭제 직렬화, verification_evidence: [CommunityPostCommentCommandServiceTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: deletePostRepliesByPostId/deletePostCommentsByPostId, external_boundary: MySQL}, payload: postId, contract_ref: self FK 자식 우선 삭제, purpose: 게시글 삭제 시 댓글 물리 삭제, verification_evidence: [CommunityPostDeleteServiceTest, CommunityPostCommandRepositoryTest]}
verified_flow: 공개 GET → EXAM_REVIEW 존재 확인 → 최상위 page → 해당 parent replies와 현재 reply 대상 nickname 일괄 조회 → 중첩 DTO 직접 반환; 인증·CSRF write → EXAM_REVIEW 게시글 잠금 → reply 대상과 root 잠금 → parent=root·replyTo=실제 대상 평탄화 → JPA @Version 변경 또는 혼합 삭제
verification_scope: MIXED
required_checks:
  - review 관련 자동 테스트
  - 전체 자동 테스트
  - assemble
  - git diff --check
check_evidence:
  - review 테스트 123건 중 113건 통과, 기존 MySQL opt-in 10건 건너뜀, 실패·오류 0건
  - 전체 테스트 159건 중 149건 통과, 기존 MySQL opt-in 10건 건너뜀, 실패·오류 0건
  - assemble BUILD SUCCESSFUL
  - git diff --check 통과
exception_coverage:
  - POST_NOT_FOUND 404
  - COMMENT_NOT_FOUND 404
  - COMMENT_DEPTH_EXCEEDED 400
  - INVALID_INPUT 400
  - ACCESS_DENIED 403
  - VERSION_CONFLICT 409
  - UNAUTHENTICATED 401 및 CSRF ACCESS_DENIED 403
blockers: []
resume_when: []
next_action: 사용자 코드 검토 후 로컬 또는 격리된 MySQL에서 baseline 0과 V1 migration, 조회·write·삭제·집계를 검증한다.
remaining_work:
  - 실제 MySQL native 조회·self FK·optimistic/pessimistic locking 통합 검증
  - 사람 코드 검토
completion_requires: {review_status: APPROVED, integration_status: null, deployment_status: NOT_DEPLOYED}
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-12-04-REVIEW-COMMENT__codex__20261012T134019Z.md#EVT-IMPLEMENTED-008
limitations:
  - V1 migration은 작성·패키징했지만 실제 MySQL에는 적용하지 않아 기존 PK 타입과 FK·CHECK·인덱스 실행 결과는 미검증이다.
  - 공통 application.yaml에 baseline-on-migrate를 넣지 않았으므로 로컬 적용 시 명령행에서 enabled·baseline-on-migrate·baseline-version을 명시해야 한다.
  - Repository 테스트는 SQL·annotation 구조를, Service/Controller 테스트는 분기·순서·권한·응답 계약을 검증하며 실제 FK와 DB lock은 검증하지 않는다.
  - 신규 command 성공 응답은 기존 게시글 command 스타일에 맞춰 commentId 문자열 DTO를 직접 반환한다.
handoff: application-local.yaml, 비밀값, 공용 DB, build.gradle과 기존 09.01~10.03의 댓글 연계 외 계약은 변경하지 않았다.
```
