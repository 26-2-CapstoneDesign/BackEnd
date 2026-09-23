# 기능 상세 기록 양식
언어: summary_ko, 기능 목표·설명·수행 결과·오류·남은 작업·인수인계는 한국어로 작성한다. 키·상태·ID·경로·코드·원문 증거는 보존한다.
등록 조건: 범위 승인 후 docs/features/<feature_id>.md로 복사하고 실제 관찰·승인된 사실을 채운다.
값 규칙: 키를 그대로 사용한다. null=미확인·미설정, []=등록 항목 없음. NOT_APPLICABLE에는 이유가 필요하다.
중단: implementation_status를 유지하고 PAUSED / WAITING_APPROVAL / BLOCKED와 차단 원인·resume_when·latest_worklog를 기록한다.
진행도: acceptance_passed는 현재 근거가 있는 MET 개수, acceptance_total은 승인된 비어 있지 않은 적용 기준 수다. 추정하지 않는다.
구현 완료 IMPLEMENTED: 승인된 적용 기준이 모두 MET, 필수 검증 PASSED, 미해결 차단 없음. 사람 검토가 남으면 IN_REVIEW다.
작업 종료 DONE: IMPLEMENTED + review_status=APPROVED + 범위에 포함된 통합·배포 완료. 기능의 사람 검토를 임의 면제하지 않는다.
```yaml
schema_version: 3.1.0
template: true
record_type: feature
summary_ko: null # 현재 구현 범위·상태·남은 일을 한국어로 작성
feature_id: null
name: null
human_owner: null
actor_id: null
participants: []
approval_refs: []
updated_at: null
source_revision: null
feature_file: null
latest_worklog: null
implementation_status: UNVERIFIED
work_status: QUEUED
acceptance_passed: null
acceptance_total: null
verification_status: NOT_RUN
review_status: NOT_REQUESTED
integration_status: UNVERIFIED
deployment_status: UNVERIFIED
missing_reasons: {}
not_applicable_reasons: {}
goal: null
scope: []
excluded_scope: []
dependency_refs: []
unknowns: []
acceptance_definition_status: UNDECIDED
acceptance_approval_refs: []
acceptance_criteria:
  - ac_id: null
    condition: null
    expected: null
    status: UNVERIFIED
    evidence_refs: []
    source_revision: null
components:
  - path: null
    symbol: null
    role: null
    implementation_status: UNVERIFIED
    remaining_work: []
    task_refs: []
connections:
  - from: {path: null, symbol: null}
    to: {path: null, symbol: null, external_boundary: null}
    payload: null
    contract_ref: null
    purpose: null
    verification_evidence: []
verified_flow: null
verification_scope: UNVERIFIED
required_checks: []
check_evidence: []
exception_coverage: []
blockers: []
resume_when: []
next_action: null
remaining_work: []
completion_requires: {review_status: APPROVED, integration_status: null, deployment_status: null}
review_evidence: []
integration_evidence: []
deployment_evidence: []
completion_event_ref: null
limitations: []
handoff: null
```
허용값 acceptance_definition_status: UNDECIDED | UNVERIFIED | APPROVED. 허용값 AC status: UNVERIFIED | MET | UNMET.
허용값 verification_scope: UNVERIFIED | STATIC_ONLY | MOCK_ONLY | REAL_INTEGRATION | MIXED. 실제 확인 범위를 check_evidence에 한국어로 설명한다.
조건 completion_requires에 null 존재: 종료 조건이 미확인이다. DONE 전에 확인하며 null을 면제로 해석하지 않는다.
조건 승인 범위에서 통합·배포 제외: completion_requires와 해당 상태에 NOT_APPLICABLE, 이유, approval_refs를 기록한다.
조건 충족 기준의 근거가 사라짐: 최신 상태·진행도를 갱신하고 CORRECTION을 추가한다. 과거 완료 사건은 지우지 않는다.
