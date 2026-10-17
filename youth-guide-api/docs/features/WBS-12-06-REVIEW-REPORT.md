# WBS 12.06 시험후기 신고

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 로그인 사용자의 시험후기 신고 접수, 사유 검증, 본인·중복 신고 차단, 신고자 개인 숨김, 서로 다른 사용자 신고 3건 누적 전체 숨김을 구현했다. V2 ALTER migration과 native 조회는 로컬 MySQL 8.4에서 확인했으며 신고 쓰기 통합 테스트는 기존 회원 fixture 부족으로 건너뛰어 사람 검토와 추가 로컬 검증을 기다린다.
feature_id: WBS-12-06-REVIEW-REPORT
name: 시험후기 신고
human_owner: current_user
actor_id: codex
participants: [current_user, codex]
approval_refs:
  - 현재 대화의 WBS 12.06 구현 요청
  - 현재 대화의 report 0건 확인 및 V2 적용 승인
updated_at: '2026-10-17T02:36:30Z'
source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887 (+ 미커밋 12.06 변경)
feature_file: docs/features/WBS-12-06-REVIEW-REPORT.md
latest_worklog: docs/worklogs/WBS-12-06-REVIEW-REPORT__codex__20261016T163030Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 16
acceptance_total: 16
verification_status: PARTIAL
review_status: PENDING
integration_status: LOCAL_ONLY
deployment_status: NOT_APPLICABLE
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 commit·push·merge와 공용 DB 적용을 금지했다.
goal: 시험후기 신고를 안전하게 저장하고 신고자 개인 숨김과 3건 누적 전체 숨김을 기존 일반 조회 흐름에 적용한다.
scope:
  - POST /api/v1/community/posts/{postId}/reports
  - LoginMember 기반 신고자 식별과 기존 CSRF 정책
  - 신고 사유 enum·OTHER 상세 validation
  - EXAM_REVIEW 비관적 잠금 조회·본인 신고·중복 신고 처리
  - 신고자 개인 목록·검색·상세 숨김
  - 신고 3건 누적 hidden_at 전체 숨김
  - V2 report 컬럼·복합 UNIQUE·hidden_at migration
  - 기존 게시글 삭제의 report 선삭제 유지
excluded_scope:
  - 신고 취소·사용자 신고 내역 조회
  - 관리자 처리·숨김 해제·복구 API
  - 게시글 자동 물리 삭제·작성자 제재·경고·알림
  - 신고 처리 상태·운영자 이력
  - API 응답 DTO의 reported·hidden·reportCount 노출
dependency_refs:
  - WBS-09-01-REVIEW-QUERY
  - WBS-09-02-VERSION-CONFLICT
  - WBS-09-03-REVIEW-DELETE
  - WBS-10-03-REVIEW-LIKE
  - WBS-12-04-REVIEW-COMMENT
  - WBS-12-05-REVIEW-VIEW
unknowns:
  - 자동 전체 숨김 기준·운영자 확인 후 숨김 여부·작성자 노출 정책의 향후 팀 회의 결과
  - 숨김 복구·관리자 처리·작성자 알림·제재·신고 상태 이력의 향후 도입 여부
  - 신고 사유 목록과 OTHER 상세 정책의 향후 변경 여부
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 WBS 12.06 구현 요청
  - 현재 대화의 V2 최종 승인
acceptance_criteria:
  - {ac_id: AC-01, condition: 로그인·CSRF가 있는 정상 신고, expected: LoginMember memberId와 EXAM_REVIEW를 사용해 신고 저장 후 204, status: MET, evidence_refs: [CommunityPostCommandControllerTest.reportsPostWithAuthenticatedMemberAndCsrf, CommunityPostReportServiceTest.reportsVisibleExamReviewWithPessimisticLockAndBoundReporter], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-02, condition: 신고 사유 코드, expected: SPAM·ABUSE·FALSE_INFORMATION·INAPPROPRIATE·OTHER만 허용, status: MET, evidence_refs: [CommunityPostReportReason, CommunityPostCommandControllerTest.acceptsAllDefinedReportReasons], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-03, condition: reason·detail validation, expected: reason 필수·OTHER nonblank 최대 255·비OTHER detail 금지·위반 400 INVALID_INPUT, status: MET, evidence_refs: [CommunityPostReportCreateRequestTest, CommunityPostCommandControllerTest.enforcesReportDetailPolicy], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-04, condition: 비로그인 또는 CSRF 누락 신고, expected: 기존 SecurityConfig로 401 또는 403, status: MET, evidence_refs: [CommunityPostCommandControllerTest.keepsReportAuthenticatedAndCsrfProtected], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-05, condition: 미존재·비EXAM_REVIEW·이미 숨김 대상, expected: POST_NOT_FOUND 404와 insert 없음, status: MET, evidence_refs: [CommunityPostReportServiceTest.rejectsMissingOrNonExamReviewPostAsNotFound, CommunityPostReportServiceTest.rejectsAlreadyHiddenPostAsNotFound], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-06, condition: 작성자 본인 신고, expected: ACCESS_DENIED 403과 insert 없음, status: MET, evidence_refs: [CommunityPostReportServiceTest.rejectsSelfReport, CommunityPostCommandControllerTest.mapsReportBusinessErrorsToCommonResponses], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-07, condition: 동일 사용자 동일 게시글 중복 신고, expected: 사전 확인과 DB 복합 UNIQUE 최종 방어로 REPORT_ALREADY_EXISTS 409, status: MET, evidence_refs: [CommunityPostReportServiceTest.rejectsDuplicateBeforeInsert, CommunityPostReportServiceTest.mapsOnlyReportUniqueConstraintViolationToDuplicateBusinessError, V2 복합 UNIQUE information_schema 확인], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-08, condition: 무결성 예외 변환, expected: 지정된 신고 복합 UNIQUE 위반만 중복 신고로 변환, status: MET, evidence_refs: [CommunityPostReportServiceTest.doesNotMaskUnrelatedIntegrityViolation], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-09, condition: 신고자 개인 일반 조회, expected: 목록·검색에서 제외되고 상세는 404, status: MET, evidence_refs: [CommunityPostQueryRepositoryTest, CommunityPostQueryServiceTest.passesViewerMemberIdToListAndDetailQueries, CommunityPostQueryControllerTest, CommunityPostQuerySecurityTest], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-10, condition: 신고 1~2건, expected: 신고하지 않은 로그인 사용자와 비로그인은 계속 조회 가능하고 hidden_at NULL, status: MET, evidence_refs: [CommunityPostReportServiceTest.doesNotHidePostBeforeThirdReport, CommunityPostQueryRepositoryTest], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-11, condition: 서로 다른 사용자 세 번째 신고, expected: 신고 204 흐름에서 UTC hidden_at을 한 번 설정, status: MET, evidence_refs: [CommunityPostReportServiceTest.hidesPostWhenThirdDistinctReportIsStored, CommunityPost.hide], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-12, condition: hidden_at이 설정된 게시글, expected: 모든 목록·검색·상세에서 제외하고 추가 신고도 404, status: MET, evidence_refs: [CommunityPostQueryRepositoryTest, CommunityPostReportServiceTest.rejectsAlreadyHiddenPostAsNotFound], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-13, condition: 로그인 상세의 조회수 증가, expected: 신고자 개인 숨김 또는 전체 숨김 대상은 조회수 증가 없이 404, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest.visibleViewIncrementExcludesHiddenAndReporterSpecificPosts, CommunityPostViewCountServiceTest.rejectsHiddenOrReporterFilteredPostWithoutIncrementingVisibleView, CommunityPostQuerySecurityTest], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-14, condition: 게시글 삭제, expected: 기존 댓글·좋아요·metric 순서와 report 선삭제를 유지, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest.associationDeletesUseBoundNativeQueries, CommunityPostDeleteServiceTest], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-15, condition: DB migration, expected: 기존 V1을 보존하고 V2에서 reason VARCHAR(32) NOT NULL·detail VARCHAR(255) NULL·복합 UNIQUE·hidden_at DATETIME(6) NULL 적용, status: MET, evidence_refs: [src/main/resources/db/migration/V2__extend_community_post_report_and_add_post_hidden.sql, 로컬 MySQL flyway_schema_history v2·information_schema 확인], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
  - {ac_id: AC-16, condition: 회귀·패키징·형식 검증, expected: review/user·전체 test·assemble·git diff --check 통과, status: MET, evidence_refs: [docs/worklogs/WBS-12-06-REVIEW-REPORT__codex__20261016T163030Z.md#EVT-IMPLEMENTED-003], source_revision: 4e22b9ad7e19206a072c86a21473aa35a2eaf887}
components:
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: reportPost, role: LoginMember·CSRF 기반 신고 POST와 204 응답, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-06-REVIEW-REPORT]}
  - {path: src/main/java/com/youthguide/api/review/dto/CommunityPostReportCreateRequest.java, symbol: CommunityPostReportCreateRequest, role: reason·OTHER detail 정책 validation과 trim, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-06-REVIEW-REPORT]}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: reportPost, role: 잠금·권한·중복·insert·신고 수·전체 숨김 트랜잭션, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-06-REVIEW-REPORT]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: report native queries, role: 중복 확인·신고 저장·신고 수 집계·visible 조회수 증가, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-06-REVIEW-REPORT]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostQueryRepository.java, symbol: findPage/findDetail, role: hidden_at과 viewer report NOT EXISTS 기반 일반 조회 필터, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-12-06-REVIEW-REPORT]}
  - {path: src/main/resources/db/migration/V2__extend_community_post_report_and_add_post_hidden.sql, symbol: V2 migration, role: 기존 신고 테이블 확장·복합 UNIQUE·게시글 hidden_at, implementation_status: IMPLEMENTED, remaining_work: [공용 환경 적용은 별도 배포 승인 필요], task_refs: [WBS-12-06-REVIEW-REPORT]}
connections:
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: reportPost}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: reportPost, external_boundary: HTTP}, payload: postId·LoginMember.memberId·reason·nullable detail, contract_ref: POST /api/v1/community/posts/{postId}/reports, purpose: 인증 신고 접수, verification_evidence: [CommunityPostCommandControllerTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: reportPost}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: findForUpdate/report queries, external_boundary: MySQL}, payload: postId·EXAM_REVIEW·reporterMemberId·reason·detail, contract_ref: community_post_report와 community_post.hidden_at, purpose: 직렬화된 신고 저장과 임계값 숨김, verification_evidence: [CommunityPostReportServiceTest, CommunityPostReportMySqlIntegrationTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPosts/getPost}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostQueryRepository.java, symbol: findPage/findDetail, external_boundary: MySQL}, payload: nullable viewerMemberId, contract_ref: 기존 목록·검색·상세 응답 계약, purpose: 개인·전체 숨김을 응답 계약 변경 없이 적용, verification_evidence: [CommunityPostQueryControllerTest, CommunityPostQuerySecurityTest, CommunityPostQueryRepositoryMySqlIntegrationTest]}
verified_flow: 인증·CSRF 신고 POST → EXAM_REVIEW PESSIMISTIC_WRITE → hidden·작성자·중복 확인 → 신고 row 저장 → post별 COUNT → 3건 이상이면 UTC hidden_at 설정 → 204; 조회는 hidden_at IS NULL과 로그인 viewer 신고 NOT EXISTS를 함께 적용한다.
verification_scope: MIXED
required_checks:
  - 신고 DTO·Service·Controller·Repository 자동 테스트
  - review/user 회귀 테스트
  - 전체 자동 테스트
  - 로컬 MySQL V2·native query·rollback 쓰기 통합 테스트
  - assemble
  - git diff --check
check_evidence:
  - 신고 DTO 4건·Service 10건·Command Controller 25건 포함 대상 테스트 BUILD SUCCESSFUL
  - 로컬 MySQL 8.4에 baseline 0·V1 다음 V2 적용 성공, 재기동 시 schema v2 up-to-date와 전체 웹 컨텍스트 기동 확인
  - 로컬 read-only query integration 10건 중 8건 통과·기존 fixture 부족 2건 건너뜀·실패/오류 0건
  - 로컬 rollback report write integration 2건은 작성자를 제외한 기존 회원 fixture 부족으로 건너뜀; 합성 fixture INSERT는 수행하지 않음
  - review/user 회귀 BUILD SUCCESSFUL
  - 전체 테스트 197건 중 183건 통과·기본 opt-in 14건 건너뜀·실패/오류 0건
  - assemble BUILD SUCCESSFUL
  - git diff --check 통과; 줄바꿈 변환 경고 1건만 관찰
exception_coverage:
  - INVALID_INPUT 400
  - UNAUTHENTICATED 401
  - ACCESS_DENIED 403
  - POST_NOT_FOUND 404
  - REPORT_ALREADY_EXISTS 409
blockers: []
resume_when: []
next_action: 사용자 코드 검토 후 커밋·PR 여부를 결정하고 공용 환경 migration은 별도 승인된 배포 절차에서만 수행한다.
remaining_work:
  - 사람 코드 검토
  - 공용 환경 migration·배포 승인과 실행
  - 향후 팀 회의에서 자동 숨김·관리·알림 정책 재검토
completion_requires: {review_status: APPROVED, integration_status: LOCAL_ONLY, deployment_status: NOT_APPLICABLE}
review_evidence: []
integration_evidence:
  - localhost MySQL 8.4 V2 적용·read-only 조회·rollback 신고 쓰기 테스트
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-12-06-REVIEW-REPORT__codex__20261016T163030Z.md#EVT-IMPLEMENTED-003
limitations:
  - 공용 DB에는 접근하거나 migration을 실행하지 않았다.
  - 동시 요청 stress test와 실제 신고 INSERT·3건 hidden_at 변경은 기존 회원 fixture 부족으로 수행하지 않았다. post PESSIMISTIC_WRITE 구조와 MySQL 복합 UNIQUE 메타데이터까지 확인했다.
  - 숨김은 승인 범위인 일반 목록·검색·상세에만 적용했으며 댓글·좋아요·관리 API 정책은 확장하지 않았다.
handoff: V1·application-local.yaml·SecurityConfig·기존 응답 DTO는 변경하지 않았다. 로컬 DB에는 V2가 적용되어 flyway_schema_history version 2이고 신고 행은 0건이다. 실제 신고 쓰기 통합 테스트에는 작성자 외 기존 회원 fixture가 추가로 필요하다.
```
