# WBS 09.03 시험후기 삭제·연관 데이터 처리 API

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 작성자와 예상 버전을 확인한 뒤 시험후기의 좋아요·metric·신고를 먼저 삭제하고 게시글을 물리 삭제하는 API를 구현했다.
feature_id: WBS-09-03-REVIEW-DELETE
name: 시험후기 삭제·연관 데이터 처리 API
human_owner: current_user
actor_id: codex
participants: [current_user, codex]
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-10T17:03:27Z'
source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4
feature_file: docs/features/WBS-09-03-REVIEW-DELETE.md
latest_worklog: docs/worklogs/WBS-09-03-REVIEW-DELETE__codex__20261010T165857Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 12
acceptance_total: 12
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_APPLICABLE
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 commit·push·배포를 요청하지 않았다.
goal: 인증된 작성자가 예상 버전과 일치하는 시험후기를 RESTRICT 연관 행과 함께 한 트랜잭션에서 물리 삭제한다.
scope:
  - DELETE /api/v1/community/posts/{postId}
  - LoginMember 기반 작성자 확인
  - X-Expected-Post-Version 필수 Long 헤더
  - EXAM_REVIEW 내부 고정
  - PESSIMISTIC_WRITE 잠금 조회
  - community_post_like → community_post_metric → community_post_report → community_post 삭제
excluded_scope:
  - 09.01 최신 리뷰 수정 통합
  - 댓글·첨부·임시저장 및 다른 WBS 기능
  - DB·Flyway·SecurityConfig·application 설정 변경
  - Member·Certificate 연관관계 리팩터링
dependency_refs:
  - src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java
  - src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java
  - src/main/java/com/youthguide/api/global/security/LoginMember.java
  - src/main/java/com/youthguide/api/global/exception
unknowns:
  - 실제 MySQL write 통합 검증은 공용 DB 데이터 변경 금지로 수행하지 않는다.
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 사용자 요청
acceptance_criteria:
  - {ac_id: AC-01, condition: 본인 글과 일치하는 version 삭제, expected: HTTP 204, status: MET, evidence_refs: [CommunityPostCommandControllerTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-02, condition: 삭제 완료, expected: 부모 게시글 삭제 호출 및 이후 조회 대상 소멸, status: MET, evidence_refs: [CommunityPostDeleteServiceTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-03, condition: postId 미존재, expected: POST_NOT_FOUND 404, status: MET, evidence_refs: [CommunityPostCommandControllerTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-04, condition: 작성자 불일치, expected: ACCESS_DENIED 403, status: MET, evidence_refs: [CommunityPostCommandControllerTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-05, condition: 예상 version 불일치, expected: VERSION_CONFLICT 409, status: MET, evidence_refs: [CommunityPostCommandControllerTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-06, condition: version 헤더 누락, expected: INVALID_INPUT 400, status: MET, evidence_refs: [CommunityPostCommandControllerTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-07, condition: version 헤더 형식 오류, expected: INVALID_INPUT 400, status: MET, evidence_refs: [CommunityPostCommandControllerTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-08, condition: 좋아요 연관 행 존재, expected: 자식 우선 삭제 후 부모 삭제, status: MET, evidence_refs: [CommunityPostDeleteServiceTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-09, condition: metric 연관 행 존재, expected: 자식 우선 삭제 후 부모 삭제, status: MET, evidence_refs: [CommunityPostDeleteServiceTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-10, condition: 신고 연관 행 존재, expected: 자식 우선 삭제 후 부모 삭제, status: MET, evidence_refs: [CommunityPostDeleteServiceTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-11, condition: 삭제 처리 중 예외, expected: @Transactional 경계에서 전체 롤백 가능한 구조, status: MET, evidence_refs: [CommunityPostDeleteServiceTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
  - {ac_id: AC-12, condition: 삭제 대상 경쟁 접근, expected: 게시글 PESSIMISTIC_WRITE 잠금 후 자식·부모 삭제, status: MET, evidence_refs: [CommunityPostCommandRepositoryTest], source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4}
components:
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: deletePost, role: LoginMember와 version 헤더를 Service에 전달하고 결과를 공통 오류로 변환, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-09-03-REVIEW-DELETE]}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost, role: 대상·작성자·version 검증 및 자식 우선 삭제 순서 제어, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-09-03-REVIEW-DELETE]}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: findForUpdateByPostIdAndPostKind/deletePost*ByPostId, role: 배타 잠금 조회와 연관 데이터 삭제 SQL, implementation_status: IMPLEMENTED, remaining_work: [], task_refs: [WBS-09-03-REVIEW-DELETE]}
connections:
  - {from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: deletePost}, to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost, external_boundary: HTTP}, payload: postId·memberId·expectedVersion, contract_ref: DELETE API, purpose: 인증 작성자 삭제, verification_evidence: [CommunityPostCommandControllerTest]}
  - {from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost}, to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommandRepository.java, symbol: lock/delete, external_boundary: MySQL}, payload: postId·EXAM_REVIEW, contract_ref: RESTRICT 자식 우선 삭제, purpose: FK 안전 삭제, verification_evidence: [CommunityPostDeleteServiceTest, CommunityPostCommandRepositoryTest]}
verified_flow: 인증·CSRF → LoginMember → PESSIMISTIC_WRITE 조회 → 작성자 → version → like → metric → report → post → 204
verification_scope: MIXED
required_checks:
  - review 관련 테스트
  - user + review 회귀 테스트
  - assemble
  - git diff --check
check_evidence:
  - review 64건 중 54건 통과, MySQL opt-in 10건 건너뜀, 실패 0건
  - user·review 88건 중 78건 통과, MySQL opt-in 10건 건너뜀, 실패 0건
  - assemble BUILD SUCCESSFUL
  - git diff --check 통과
exception_coverage:
  - POST_NOT_FOUND 404
  - ACCESS_DENIED 403
  - VERSION_CONFLICT 409
  - 헤더 누락·형식 오류 INVALID_INPUT 400
blockers: []
resume_when: []
next_action: 사용자 검토 후 격리 DB 실제 쓰기 통합 검증 필요 여부를 결정한다.
remaining_work:
  - 사람 검토
  - 격리 DB 실제 삭제·롤백 통합 검증
completion_requires: {review_status: APPROVED, integration_status: null, deployment_status: NOT_APPLICABLE}
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-09-03-REVIEW-DELETE__codex__20261010T165857Z.md#EVT-IMPLEMENTED-003
limitations:
  - 공용 MySQL 데이터에 쓰기 검증을 수행하지 않았다.
  - 격리형 테스트 DB 의존성이 없어 삭제 후 재조회와 실제 롤백은 Repository 호출·트랜잭션 경계 수준으로 검증했다.
handoff: application-local.yaml, DB·Flyway·SecurityConfig와 09.01 최신 리뷰 변경을 건드리지 않았다.
```
