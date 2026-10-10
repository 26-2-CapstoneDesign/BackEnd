# WBS 09.02 시험후기 작성·수정·버전 충돌 API

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: LoginMember와 EXAM_REVIEW를 내부 적용한 시험후기 작성·수정 API를 구현했다. Certificate 존재, 작성자, 요청 version과 JPA @Version 충돌을 공통 오류 계약으로 처리한다.
feature_id: WBS-09-02-VERSION-CONFLICT
name: 시험후기 작성·수정·버전 충돌 API
human_owner: current_user
actor_id: codex
participants:
  - current_user
  - codex
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-10T11:48:29Z'
source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
feature_file: docs/features/WBS-09-02-VERSION-CONFLICT.md
latest_worklog: docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 15
acceptance_total: 15
verification_status: PASSED
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_APPLICABLE
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 커밋·push·배포를 요청하지 않았다.
goal: 인증된 회원이 유효한 자격증을 선택해 시험후기를 작성·수정하고 동시 수정 충돌을 안전하게 처리한다.
scope:
  - POST /api/v1/community/posts
  - PUT /api/v1/community/posts/{postId}
  - LoginMember 기반 authorMemberId
  - EXAM_REVIEW 내부 고정
  - Certificate 존재 검증
  - 요청 version과 CommunityPost.lockVersion 비교
  - POST_NOT_FOUND, CERTIFICATE_NOT_FOUND, ACCESS_DENIED, VERSION_CONFLICT 공통 오류
  - 일치 시 기존 수정 흐름 유지
  - @Version 실제 경쟁 예외 409 매핑
  - 기존 SecurityConfig 인증·CSRF 유지
excluded_scope:
  - 게시글 삭제
  - 댓글·좋아요 토글·신고·조회수 증가
  - 이미지·첨부·임시저장
  - DB·Flyway·SecurityConfig 변경
  - Member·Certificate 연관관계 리팩터링
dependency_refs:
  - src/main/java/com/youthguide/api/review/domain/CommunityPost.java
  - src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java
  - src/main/java/com/youthguide/api/global/security/LoginMember.java
  - src/main/java/com/youthguide/api/certificate/repository/CertificateRepository.java
  - src/main/java/com/youthguide/api/global/exception
unknowns:
  - 숫자 문자열 certificateId 또는 version이 Long 범위를 넘는 경우의 최종 공통 INVALID_INPUT 정책
acceptance_definition_status: APPROVED
acceptance_approval_refs:
  - 현재 대화의 사용자 요청
acceptance_criteria:
  - {ac_id: AC-01, condition: 요청 version과 현재 version 일치, expected: 기존 수정 로직 정상 실행, status: MET, evidence_refs: [CommunityPostUpdateServiceTest.updatesOwnedPostAndCarriesRequestedAndCurrentVersions]}
  - {ac_id: AC-02, condition: 요청 version과 현재 version 불일치, expected: VERSION_CONFLICT 내부 상태 반환, status: MET, evidence_refs: [CommunityPostUpdateServiceTest.detectsVersionConflictWithoutChangingPost]}
  - {ac_id: AC-03, condition: version 불일치, expected: certificateId와 title과 body 유지, status: MET, evidence_refs: [CommunityPostUpdateServiceTest.detectsVersionConflictWithoutChangingPost]}
  - {ac_id: AC-04, condition: version 불일치, expected: updatedAt 유지, status: MET, evidence_refs: [CommunityPostUpdateServiceTest.detectsVersionConflictWithoutChangingPost]}
  - {ac_id: AC-05, condition: 정상 수정, expected: 기존 수정 기능 유지, status: MET, evidence_refs: [CommunityPostUpdateServiceTest.updatesOwnedPostAndCarriesRequestedAndCurrentVersions]}
  - {ac_id: AC-06, condition: 엔티티 버전 필드, expected: jakarta.persistence.Version 매핑 유지, status: MET, evidence_refs: [CommunityPostUpdateServiceTest.keepsJpaVersionMapping]}
  - {ac_id: AC-07, condition: 인증 회원이 작성 요청, expected: LoginMember memberId와 EXAM_REVIEW로 게시글 생성, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostCommandServiceTest]}
  - {ac_id: AC-08, condition: 작성 또는 수정 certificateId 미존재, expected: CERTIFICATE_NOT_FOUND 404 및 저장·변경 차단, status: MET, evidence_refs: [CommunityPostCommandServiceTest, CommunityPostUpdateServiceTest, CommunityPostCommandControllerTest]}
  - {ac_id: AC-09, condition: 작성 title 또는 body validation 실패, expected: INVALID_INPUT 400 및 Service 미호출, status: MET, evidence_refs: [CommunityPostCreateRequestTest, CommunityPostCommandControllerTest]}
  - {ac_id: AC-10, condition: 인증 회원이 수정 요청, expected: 로그인 memberId와 EXAM_REVIEW로 certificateId·title·body 수정, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostUpdateServiceTest]}
  - {ac_id: AC-11, condition: 수정 대상 미존재, expected: POST_NOT_FOUND 404, status: MET, evidence_refs: [CommunityPostCommandControllerTest]}
  - {ac_id: AC-12, condition: 수정 작성자 불일치, expected: ACCESS_DENIED 403 및 무변경, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostUpdateServiceTest]}
  - {ac_id: AC-13, condition: 요청 또는 JPA 갱신 version 충돌, expected: VERSION_CONFLICT 409, status: MET, evidence_refs: [CommunityPostCommandControllerTest, CommunityPostUpdateServiceTest]}
  - {ac_id: AC-14, condition: POST 또는 PUT 보안 요청, expected: 비로그인 거부·CSRF 누락 거부·인증과 CSRF 정상 처리, status: MET, evidence_refs: [CommunityPostCommandControllerTest]}
  - {ac_id: AC-15, condition: 09.01·09.02 회귀와 패키징, expected: DB 비의존 review 테스트 및 assemble 통과, status: MET, evidence_refs: [docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md#EVT-VERIFY-002]}
components:
  - path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java
    symbol: updatePost
    role: 작성자 확인 후 요청 버전과 현재 버전을 비교하고 불일치 시 변경 전에 반환한다.
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [TASK-09-02-VERSION-CONFLICT]
  - path: src/test/java/com/youthguide/api/review/service/CommunityPostUpdateServiceTest.java
    symbol: detectsVersionConflictWithoutChangingPost
    role: 충돌 상태와 엔티티 무변경을 검증한다.
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [TASK-09-02-VERSION-CONFLICT]
  - path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java
    symbol: CommunityPostCommandController
    role: LoginMember와 요청 DTO를 작성·수정 Service에 연결하고 내부 수정 상태를 공통 오류로 변환한다.
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [WBS-09-02-REVIEW-WRITE-UPDATE]
  - path: src/main/java/com/youthguide/api/global/exception/GlobalExceptionHandler.java
    symbol: handleOptimisticLocking
    role: Spring ORM optimistic locking 예외를 VERSION_CONFLICT 409로 변환한다.
    implementation_status: IMPLEMENTED
    remaining_work: []
    task_refs: [WBS-09-02-REVIEW-WRITE-UPDATE]
connections:
  - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: updatePost}
    to: {path: src/main/java/com/youthguide/api/review/domain/CommunityPost.java, symbol: getLockVersion, external_boundary: null}
    payload: requestedVersion와 currentVersion
    contract_ref: 현재 대화의 사용자 요청
    purpose: 명시적 수정 요청 버전 검증
    verification_evidence:
      - CommunityPostUpdateServiceTest
  - from: {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommandController.java, symbol: createPost/updatePost}
    to: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: createPost/updatePost, external_boundary: null}
    payload: LoginMember.memberId와 validation된 작성·수정 요청
    contract_ref: POST/PUT /api/v1/community/posts
    purpose: 클라이언트 작성자·postKind 입력 없이 명령 수행
    verification_evidence:
      - CommunityPostCommandControllerTest
  - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: validateCertificateExists}
    to: {path: src/main/java/com/youthguide/api/certificate/repository/CertificateRepository.java, symbol: existsById, external_boundary: MySQL}
    payload: certificateId
    contract_ref: Certificate scalar FK 존재 검증
    purpose: 유효한 자격증 참조 보장
    verification_evidence:
      - CommunityPostCommandServiceTest
      - CommunityPostUpdateServiceTest
verified_flow: SecurityConfig 인증·CSRF → LoginMember memberId → EXAM_REVIEW·Certificate·작성자·version 검증 → CommunityPost 생성 또는 UTC updatedAt 변경 → JPA @Version 확인 → 직접 성공 DTO 또는 공통 오류 응답
verification_scope: MIXED
required_checks:
  - 작성·수정 Service 단위 테스트
  - Controller와 실제 SecurityConfig WebMvc 테스트
  - 기존 09.01·09.02 review 전체 테스트
  - user·review DB 비의존 회귀 테스트
  - assemble
check_evidence:
  - Controller/Security 및 작성·수정 Service 테스트 BUILD SUCCESSFUL
  - review 50건 중 40건 실행·통과, MySQL opt-in 10건 건너뜀, 실패 0건
  - user·review 74건 중 64건 실행·통과, MySQL opt-in 10건 건너뜀, 실패 0건
  - assemble BUILD SUCCESSFUL
exception_coverage:
  - POST_NOT_FOUND 404
  - CERTIFICATE_NOT_FOUND 404
  - ACCESS_DENIED 403
  - 요청 version 불일치 또는 ObjectOptimisticLockingFailureException은 VERSION_CONFLICT 409
blockers: []
resume_when: []
next_action: 사용자 검토 후 필요하면 기존 opt-in 환경에서 실제 MySQL 동시 수정 경쟁을 검증한다.
remaining_work:
  - 사람 검토
  - 실제 MySQL 동시 수정 경쟁 검증
completion_requires:
  review_status: APPROVED
  integration_status: null
  deployment_status: NOT_APPLICABLE
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md#EVT-IMPLEMENTED-003
limitations:
  - 실제 MySQL의 두 트랜잭션 경쟁은 수행하지 않고 실제 Spring JPA 변환 예외 타입을 WebMvc에서 모의했다.
  - certificateId와 version의 Long 범위 초과 오류 매핑은 후속 공통 validation 정책이 필요하다.
handoff: SecurityConfig, DB 스키마, Member·Certificate 엔티티 관계를 변경하지 않았으며 성공 응답은 postId 문자열 DTO를 직접 반환한다.
```
