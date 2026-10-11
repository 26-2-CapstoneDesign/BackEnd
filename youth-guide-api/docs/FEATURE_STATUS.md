# 기능별 개발 현황
언어: 기능명·진행 설명·차단 원인·다음 행동은 한국어로 작성한다. 고정 키와 상태 코드는 보존한다.
등록: 범위가 승인된 기능만 docs/features/_TEMPLATE.md를 복사해 docs/features/<feature_id>.md로 만든다.
갱신 순서: 사건 → feature_file → 해당 기능의 현황 → docs/CURRENT_STATE.md. 공용 항목을 바꾸기 전에 다시 확인한다.
중단: implementation_status를 유지하고 work_status와 latest_worklog를 별도로 갱신한다.
진행도: 승인된 비어 있지 않은 기준만 acceptance_total로 사용한다. acceptance_passed는 현재 근거가 있는 MET 개수, 미확인은 null이다.
구현 완료: 적용 기준·필수 검증 통과와 미해결 차단 없음일 때만 IMPLEMENTED. 사람 검토가 남으면 IN_REVIEW를 유지한다.
작업 종료: review_status=APPROVED와 범위에 포함된 통합·배포 조건을 충족해야 기능 DONE으로 표시한다.
```yaml
schema_version: 3.1.0
template: false
record_type: feature_index
inventory_status: UNVERIFIED
features:
  - feature_id: WBS-09-01-REVIEW-QUERY
    name: 시험후기 목록·검색·상세 조회 API
    human_owner: current_user
    actor_id: codex
    implementation_status: IMPLEMENTED
    work_status: WAITING_APPROVAL
    acceptance_passed: 9
    acceptance_total: 9
    verification_status: PARTIAL
    review_status: PENDING
    integration_status: BRANCH_ONLY
    deployment_status: NOT_DEPLOYED
    feature_file: docs/features/WBS-09-01-REVIEW-QUERY.md
    latest_worklog: docs/worklogs/WBS-09-01-PUBLIC-QUERY__codex__20261010T160825Z.md
    source_revision: 6b0001dc501529ebe0cce9deb842fb3e6d60d276 (+ 미커밋 09.01 PR 리뷰 변경)
    updated_at: '2026-10-10T16:12:16Z'
    missing_reasons: {}
  - feature_id: WBS-09-02-VERSION-CONFLICT
    name: 시험후기 작성·수정·버전 충돌 API
    human_owner: current_user
    actor_id: codex
    implementation_status: IMPLEMENTED
    work_status: IN_REVIEW
    acceptance_passed: 15
    acceptance_total: 15
    verification_status: PASSED
    review_status: PENDING
    integration_status: BRANCH_ONLY
    deployment_status: NOT_APPLICABLE
    feature_file: docs/features/WBS-09-02-VERSION-CONFLICT.md
    latest_worklog: docs/worklogs/WBS-09-02-REVIEW-WRITE-UPDATE__codex__20261010T113839Z.md
    source_revision: 96fb9e68141fe8c59b0a94266abd95dc929d8109
    updated_at: '2026-10-10T11:48:29Z'
    missing_reasons: {}
  - feature_id: WBS-09-03-REVIEW-DELETE
    name: 시험후기 삭제·연관 데이터 처리 API
    human_owner: current_user
    actor_id: codex
    implementation_status: IMPLEMENTED
    work_status: IN_REVIEW
    acceptance_passed: 12
    acceptance_total: 12
    verification_status: PARTIAL
    review_status: PENDING
    integration_status: BRANCH_ONLY
    deployment_status: NOT_APPLICABLE
    feature_file: docs/features/WBS-09-03-REVIEW-DELETE.md
    latest_worklog: docs/worklogs/WBS-09-03-REVIEW-DELETE__codex__20261010T165857Z.md
    source_revision: e98535c94077a48c7ecb9401ecb1e9d32745b0c4
    updated_at: '2026-10-10T17:03:27Z'
    missing_reasons: {}
  - feature_id: WBS-10-03-REVIEW-LIKE
    name: 시험후기 좋아요 API
    human_owner: current_user
    actor_id: codex
    implementation_status: IMPLEMENTED
    work_status: IN_REVIEW
    acceptance_passed: 10
    acceptance_total: 10
    verification_status: PARTIAL
    review_status: PENDING
    integration_status: BRANCH_ONLY
    deployment_status: NOT_APPLICABLE
    feature_file: docs/features/WBS-10-03-REVIEW-LIKE.md
    latest_worklog: docs/worklogs/WBS-10-03-REVIEW-LIKE__codex__20261011T125740Z.md
    source_revision: d8132f4eaba2bb7473757fa4f528358b77ddb035 (+ 미커밋 10.03 변경)
    updated_at: '2026-10-11T13:03:00Z'
    missing_reasons: {}
```
새 항목: 아래 필드를 feature_file에서 복사한다. 기억에 의존해 실제 상태를 다시 만들어 넣지 않는다.
```yaml
template: true
record_type: feature_index_entry
feature_id: null
name: null
human_owner: null
actor_id: null
implementation_status: UNVERIFIED
work_status: QUEUED
acceptance_passed: null
acceptance_total: null
verification_status: NOT_RUN
review_status: NOT_REQUESTED
integration_status: UNVERIFIED
deployment_status: UNVERIFIED
feature_file: null
latest_worklog: null
source_revision: null
updated_at: null
missing_reasons: {}
```
[]는 등록 항목이 없다는 뜻이다. 실제 제품의 기능 목록과 구현 여부는 UNVERIFIED로 남는다.
