# 작업 기록 양식
언어: 제목·요약·수행 내용·오류 설명·예상 영향·승인 요청·재개 조건·완료·인수인계는 한국어로 작성한다.
보존: 키·상태 코드·ID·경로·심볼·명령어·오류 원문·인용 규칙은 그대로 둔다. 오류 원문에는 한국어 summary_ko를 함께 기록한다.
표시: ACTIVE=진행 중, PAUSED=일시 중단, WAITING_APPROVAL=승인 대기, BLOCKED=차단, IN_REVIEW=검토 대기, DONE=종료. 코드 자체는 번역하지 않는다.
작성 단위: 작업·작업자·세션별 파일. 경로: docs/worklogs/<task_id>__<actor_id>__<YYYYMMDDThhmmssZ>.md. 이름이 겹치면 접미사를 붙인다.
수정 가능: 최신 session 요약. 수정 금지: 이미 기록한 사건. 과거 내용의 정정은 CORRECTION 사건으로 추가한다.
복사 대상: session + event. 해당하는 조건부 내용만 사건 안에 합치고, 조건부 블록의 template 메타데이터는 중복 복사하지 않는다. 실제 필수 값을 채운 뒤 template=false로 기록한다.
시각: 실제 시간과 시간대를 기록한다. occurred_at은 발생 시각, recorded_at은 작성 시각이다. 모르면 null과 missing_reasons를 남긴다.
기록 내용: 수행 행동과 확인 가능한 근거를 남긴다. 내부 추론 전문은 기록하지 않는다. 비밀값은 원문을 복사하지 않고 가림 범위와 이유를 남긴다.
```yaml
schema_version: 3.1.0
template: true
record_type: session
summary_ko: null # 현재 결과·상태·남은 일을 한국어 1~3문장으로 작성
task_id: null
session_id: null
kind: null
feature_id: null
human_owner: null
actor_id: null
tool: null
actual_model: null
started_at: null
recorded_at: null
timezone: null
source_revision: null
existing_edits: []
rules_version: 3.1.0
read_files: []
goal: null
scope: []
approval_refs: []
implementation_status: UNVERIFIED
work_status: QUEUED
verification_status: NOT_RUN
review_status: NOT_REQUESTED
integration_status: UNVERIFIED
deployment_status: UNVERIFIED
latest_event_id: null
previous_log: null
remaining_work: []
resume_when: []
next_action: null
missing_reasons: {}
not_applicable_reasons: {}
```
사건 추가: 시작·중단·재개·검증·완료 등 중요한 지점마다 추가한다. event_id는 파일 안에서 고유하다. kind=FEATURE | DOCS | MAINTENANCE.
```yaml
template: true
record_type: event
summary_ko: null # 이 사건의 행동·결과·다음 행동을 한국어로 작성
event_id: null
event_type: START
actor_id: null
recorded_at: null
occurred_at: null
source_revision: null
rule_refs: [{rule_id: null, rule_text: null, source_path: null}]
approval_refs: []
actions: []
changes: [{path: null, symbol: null, operation: MODIFY, reason: null}]
checks:
  - command: null
    environment: null
    input: null
    expected: null
    observed: null
    result: NOT_RUN
    evidence_ref: null
unperformed_checks: []
state_before: {}
state_after: {}
remaining_work: []
next_action: null
state_updates: [{path: null, result: NOT_APPLIED, evidence_ref: null}]
missing_reasons: {}
```
허용값 event_type: START | CHECKPOINT | CONFLICT_RESOLVED | STOP | RESUME | VERIFY | IMPLEMENTED | COMPLETE | CORRECTION.
허용값 operation: ADD | MODIFY | DELETE. 허용값 check.result: NOT_RUN | PARTIAL | PASSED | FAILED | NOT_APPLICABLE.
허용값 state_updates.result: APPLIED | NOT_APPLIED | FAILED | NOT_APPLICABLE. 사건을 먼저 기록하므로 아직 갱신 전이면 NOT_APPLIED를 남기고 다음 사건에서 실제 반영 여부를 확인한다.
조건 STOP: 사건 안에 stop을 추가하고 rule_refs에 규칙 ID와 원문을 기록한다. 원인·영향·재개 조건을 한국어로 사용자에게 알린다.
오류 구분: observed_error.message는 실제 원문, summary_ko는 한국어 설명이다. 관찰한 오류가 없으면 message=null로 두고 미발생·미확인을 설명한다. predicted_risk는 가능성으로만 작성한다.
```yaml
template: true
stop:
  category: null
  location: {path: null, symbol: null, stage: null}
  blocked_scope: []
  conflicting_evidence: []
  observed_error: {message: null, summary_ko: null, input: null, reproduction: null, result: null}
  predicted_risk: [{trigger: null, affected_paths: [], possible_failure: null}]
  attempts: []
  autonomous_fix_forbidden_because: null
  preserved_edits: []
  independent_allowed_work: []
  decision_request: {question: null, proposed_change: null, options: [], impact: null, requested_scope: []}
  resume_when: []
  resume_checks: []
  user_notification_ref: null
```
허용값 stop.category: RULE_CONFLICT | APPROVAL_GAP | EVIDENCE_GAP | ENVIRONMENT_FAILURE | SESSION_PAUSE.
상태 결정: 세션 중단은 PAUSED, 사람·모델 검토 대기는 WAITING_APPROVAL, 미해결 규칙·환경·근거 문제는 BLOCKED. implementation_status는 유지한다.
조건 CONFLICT_RESOLVED/RESUME: 사건 안에 resolution을 추가한다. 자동 수정은 명확한 규칙·기존 승인, 계약·타인 변경 보존, 검증 가능성을 모두 충족해야 한다.
```yaml
template: true
resolution:
  prior_stop_event_ref: null
  conflict_evidence: []
  new_evidence: []
  approval_refs: []
  rules_checked: []
  resume_conditions_met: []
  preserved_contracts: []
  changed_scope: []
  source_revision_rechecked: null
  recheck_evidence: []
```
조건 IMPLEMENTED/COMPLETE: 사건 안에 completion을 추가한다. 승인되지 않거나 비어 있는 완료 기준, 누락된 필수 검증으로 IMPLEMENTED를 표시하지 않는다. 기능 COMPLETE에는 사람 검토가 필요하다.
문서·유지보수 작업의 COMPLETE는 IN_REVIEW 상태로 산출물 전달을 기록할 수 있다. 제품 구현 완료나 사람의 채택 승인을 의미하지 않는다.
```yaml
template: true
completion:
  feature_id: null
  outputs: []
  delivered_behavior: null
  scope: []
  excluded_scope: []
  acceptance_results: [{ac_id: null, status: UNVERIFIED, evidence_refs: []}]
  source_revision: null
  connections:
    - from: {path: null, symbol: null}
      to: {path: null, symbol: null, external_boundary: null}
      payload: null
      contract_ref: null
      purpose: null
      verification_evidence: []
  verified_flow: null
  verification_scope: UNVERIFIED
  exception_coverage: []
  limitations: []
  remaining_work: []
  review_status: PENDING
  review_evidence: []
  integration_status: UNVERIFIED
  deployment_status: UNVERIFIED
  handoff: null
```
조건 중요한 추론·모델 전환·상위 모델 확인 불가: model을 추가한다. 확인하지 않은 모델 사용·전환을 주장하지 않는다.
```yaml
template: true
model:
  decision: null
  preferred_tier: HIGH
  actual_model: null
  verified_tier: UNVERIFIED
  reasoning_setting: null
  switch_status: NOT_ATTEMPTED
  capability_evidence: []
  held_decisions: []
  human_override_ref: null
```
허용값 model.switch_status: NOT_ATTEMPTED | SWITCHED | UNAVAILABLE | FAILED | UNVERIFIED.
HIGH 사용 불가·미확인: 근거와 대안만 준비한다. 상위 모델 검토 또는 사람의 명시적인 해당 범위 검토·예외 승인까지 중요한 결정을 보류한다.
조건 CORRECTION: correction을 추가한다. 원래 사건은 보존하고 근거에 따라 영향받은 최신 상태만 갱신한다.
```yaml
template: true
correction:
  target_event_ref: null
  previous_claim: null
  corrected_claim: null
  evidence_refs: []
  impact: null
  affected_paths: []
```
