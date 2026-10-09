# WBS 09.02 시험후기 버전 충돌 처리

```yaml
schema_version: 3.1.0
template: false
record_type: feature
summary_ko: 시험후기 수정 요청의 version과 현재 엔티티 lockVersion이 다르면 변경을 차단하고 내부 VERSION_CONFLICT 상태를 반환한다. 공통 예외와 HTTP 매핑은 후속 작업으로 남아 있다.
feature_id: WBS-09-02-VERSION-CONFLICT
name: 시험후기 수정 버전 충돌 처리
human_owner: current_user
actor_id: codex
participants:
  - current_user
  - codex
approval_refs:
  - 현재 대화의 사용자 요청
updated_at: '2026-10-09T18:10:10Z'
source_revision: 27be09cadcc26199b945b62b48ce94419f71d0a0
feature_file: docs/features/WBS-09-02-VERSION-CONFLICT.md
latest_worklog: docs/worklogs/TASK-09-02-VERSION-CONFLICT__codex__20261009T180800Z.md
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
acceptance_passed: 6
acceptance_total: 6
verification_status: PASSED
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_APPLICABLE
missing_reasons: {}
not_applicable_reasons:
  deployment_status: 사용자가 커밋·push·배포를 요청하지 않았다.
goal: 요청 버전 불일치 시 시험후기 내용과 수정 시각이 변경되지 않도록 차단한다.
scope:
  - 요청 version과 CommunityPost.lockVersion 비교
  - VERSION_CONFLICT 내부 결과 상태
  - 일치 시 기존 수정 흐름 유지
  - @Version 매핑 유지 검증
excluded_scope:
  - 전역 ErrorCode와 예외
  - Controller와 HTTP 409 매핑
  - flush 또는 commit 시 optimistic locking 예외 처리
  - Member, Certificate, Security, DB 및 Flyway 변경
dependency_refs:
  - src/main/java/com/youthguide/api/review/domain/CommunityPost.java
  - src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java
unknowns:
  - 공통 예외 구조에서 VERSION_CONFLICT를 표현할 최종 ErrorCode와 HTTP 매핑
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
connections:
  - from: {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: updatePost}
    to: {path: src/main/java/com/youthguide/api/review/domain/CommunityPost.java, symbol: getLockVersion, external_boundary: null}
    payload: requestedVersion와 currentVersion
    contract_ref: 현재 대화의 사용자 요청
    purpose: 명시적 수정 요청 버전 검증
    verification_evidence:
      - CommunityPostUpdateServiceTest
verified_flow: 작성자 확인 후 요청 version과 lockVersion을 비교하고, 불일치 시 updateContent 호출 전에 VERSION_CONFLICT를 반환하며, 일치 시 기존 updateContent를 실행한다.
verification_scope: MOCK_ONLY
required_checks:
  - 신규 버전 충돌 단위 테스트
  - 기존 09.01 및 09.02 review 테스트
  - assemble
check_evidence:
  - review 테스트 36개 중 DB 통합 테스트 10개 제외, 26개 실행·통과, 실패 0개
  - Gradle assemble BUILD SUCCESSFUL
exception_coverage:
  - 명시적 요청 버전 불일치는 내부 VERSION_CONFLICT 상태로 구분한다.
blockers: []
resume_when: []
next_action: 사람 검토 후 공통 예외 구조가 준비되면 내부 상태와 HTTP 오류 응답을 연결한다.
remaining_work:
  - ObjectOptimisticLockingFailureException 등 flush/commit 시점 예외를 공통 예외 구조에 매핑
  - Controller에서 내부 VERSION_CONFLICT 상태를 공통 오류 응답으로 변환
completion_requires:
  review_status: APPROVED
  integration_status: null
  deployment_status: NOT_APPLICABLE
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: docs/worklogs/TASK-09-02-VERSION-CONFLICT__codex__20261009T180800Z.md#EVT-IMPLEMENTED-003
limitations:
  - 실제 DB의 동시 UPDATE 경쟁과 flush 시점 예외는 단위 테스트 범위에 포함되지 않았다.
handoff: 공통 예외 구조 도입 후 VERSION_CONFLICT와 JPA optimistic locking 예외를 동일한 외부 계약으로 연결한다.
```
