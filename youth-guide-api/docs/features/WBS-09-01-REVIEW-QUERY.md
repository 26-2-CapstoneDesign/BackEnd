# WBS 09.01 시험후기 목록·검색·상세 조회

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 시험후기 목록·검색·상세 조회 API를 구현했다. post_kind는 EXAM_REVIEW로 내부 고정하며 성공 시 DTO를 직접 반환하고, 현재 SecurityConfig에 따라 인증이 필요하다.
feature_id: WBS-09-01-REVIEW-QUERY
name: 시험후기 목록·검색·상세 조회 API
human_owner: current_user
actor_id: codex
participants: [current_user, codex]
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-10T10:20:57Z'
source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
feature_file: docs/features/WBS-09-01-REVIEW-QUERY.md
latest_worklog: docs/worklogs/WBS-09-01-REVIEW-QUERY__codex__20261010T101546Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 7
acceptance_total: 7
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_DEPLOYED
missing_reasons: {}
not_applicable_reasons: {}
goal: 로컬 MySQL의 시험후기 데이터를 목록·검색·상세 API로 조회한다.
scope:
  - GET /api/v1/community/posts의 q, 0-based page, 기본 size 20
  - GET /api/v1/community/posts/{postId}
  - title, body, certificate.display_name 부분일치 검색과 LIKE wildcard escape
  - created_at DESC, post_id DESC 정렬
  - 작성자 nickname, 자격증명, 조회수, 좋아요 수 반환
  - 성공 DTO 직접 반환과 상세 미존재 공통 404
excluded_scope:
  - 작성·수정·삭제
  - 댓글 및 댓글 수
  - 좋아요 토글과 신고
  - 상세 조회수 증가
  - 이미지·첨부·임시저장
  - DB·Flyway·SecurityConfig 변경
  - Member·Certificate 연관관계 리팩터링
dependency_refs:
  - src/main/java/com/youthguide/api/global/exception
  - src/main/java/com/youthguide/api/global/response/ErrorResponse.java
  - src/main/java/com/youthguide/api/global/config/SecurityConfig.java
unknowns: []
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 사용자 요청
acceptance_criteria:
  - ac_id: AC-01
    condition: 목록 요청에 q/page/size가 전달된다.
    expected: 0-based 페이지와 기본 size 20으로 CommunityPostPageResponse를 직접 반환한다.
    status: MET
    evidence_refs: [CommunityPostQueryControllerTest]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
  - ac_id: AC-02
    condition: 목록과 상세를 조회한다.
    expected: 클라이언트 입력 없이 EXAM_REVIEW만 Repository에 전달한다.
    status: MET
    evidence_refs: [CommunityPostQueryServiceTest]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
  - ac_id: AC-03
    condition: 검색과 페이징을 수행한다.
    expected: 검색 wildcard를 escape하고 승인된 검색 컬럼·정렬·count query를 유지한다.
    status: MET
    evidence_refs: [LikePatternEscaperTest, CommunityPostQueryRepositoryTest]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
  - ac_id: AC-04
    condition: 상세 게시글이 존재하지 않는다.
    expected: POST_NOT_FOUND 공통 ErrorResponse와 HTTP 404를 반환한다.
    status: MET
    evidence_refs: [CommunityPostQueryServiceTest, CommunityPostQueryControllerTest]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
  - ac_id: AC-05
    condition: 조회 Controller를 호출한다.
    expected: LoginMember에 의존하지 않고 SecurityConfig의 기존 인증 정책은 변경하지 않는다.
    status: MET
    evidence_refs: [CommunityPostQueryController.java, SecurityConfig.java]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
  - ac_id: AC-06
    condition: 목록·상세 응답 데이터를 조회한다.
    expected: Member와 Certificate는 scalar FK 및 native JOIN을 유지하고 metric 누락은 0, 좋아요 행 없음은 0으로 반환한다.
    status: MET
    evidence_refs: [CommunityPostQueryRepository.java, CommunityPostQueryRepositoryTest]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
  - ac_id: AC-07
    condition: 09.01 구현을 인수인계한다.
    expected: 기능 문서와 worklog에 범위·제한·검증 결과를 기록한다.
    status: MET
    evidence_refs: [docs/features/WBS-09-01-REVIEW-QUERY.md, docs/worklogs/WBS-09-01-REVIEW-QUERY__codex__20261010T101546Z.md]
    source_revision: 9471bc2f288e8ce344348ee5b61d8233fa584c78
components:
  - path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java
    symbol: CommunityPostQueryController
    role: 목록·상세 HTTP 요청과 DTO 직접 응답
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [WBS-09-01-REVIEW-QUERY]
  - path: src/main/java/com/youthguide/api/review/service/CommunityPostQueryService.java
    symbol: CommunityPostQueryService
    role: EXAM_REVIEW 고정, 검색 처리, DTO 변환, 상세 미존재 판단
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [WBS-09-01-REVIEW-QUERY]
  - path: src/main/java/com/youthguide/api/review/repository/CommunityPostQueryRepository.java
    symbol: CommunityPostQueryRepository
    role: community_post와 연관 테이블 native JOIN 조회
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [WBS-09-01-REVIEW-QUERY]
connections:
  - from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostQueryController.java, symbol: getPosts/getPost}
    to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostQueryService.java, symbol: getPosts/getPost, external_boundary: null}
    payload: q/page/size 또는 postId
    contract_ref: HTTP API
    purpose: 조회 요청 전달
    verification_evidence: [CommunityPostQueryControllerTest]
  - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostQueryService.java, symbol: getPosts/getPost}
    to: {path: src/main/java/com/youthguide/api/review/repository/CommunityPostQueryRepository.java, symbol: findPage/findDetail, external_boundary: MySQL}
    payload: EXAM_REVIEW와 검색·페이징 또는 postId
    contract_ref: native query
    purpose: 조회 결과를 projection으로 수신
    verification_evidence: [CommunityPostQueryServiceTest, CommunityPostQueryRepositoryTest]
verified_flow: Controller → Service(EXAM_REVIEW 고정·escape·DTO 변환) → Repository(native JOIN) → 성공 DTO 직접 반환 또는 POST_NOT_FOUND 공통 오류 응답
verification_scope: MIXED
required_checks:
  - review 테스트
  - DB 비의존 user·review 회귀 테스트
  - assemble
check_evidence:
  - review 테스트 BUILD SUCCESSFUL
  - user·review 49건 중 실행된 39건 통과, MySQL opt-in 10건 건너뜀, 실패 0건
  - assemble BUILD SUCCESSFUL
  - 전체 test는 로컬 MySQL auth_gssapi_client 문제로 컨텍스트 테스트 1건 실패
exception_coverage:
  - POST_NOT_FOUND → BusinessException → GlobalExceptionHandler → HTTP 404 ErrorResponse
blockers: []
resume_when: []
next_action: 사용자 검토 후 정상 DB 인증 환경에서 선택형 MySQL 통합 테스트를 실행한다.
remaining_work:
  - 사람 검토
  - 실제 MySQL 읽기 전용 통합 검증
completion_requires:
  review_status: APPROVED
  integration_status: null
  deployment_status: null
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-09-01-REVIEW-QUERY__codex__20261010T101546Z.md#EVT-IMPLEMENTED-003
limitations:
  - 댓글 데이터와 commentCount는 반환하지 않는다.
  - 상세 조회는 조회수를 증가시키지 않는다.
  - 현재 SecurityConfig에서 /api/v1/community/posts는 permitAll 대상이 아니므로 인증이 필요하다.
  - 실제 MySQL opt-in 테스트는 현재 실행하지 않았다.
handoff: 성공 응답은 wrapper 없이 DTO를 직접 반환하며, SecurityConfig와 DB 스키마는 변경하지 않았다.
```
