# 사람의 결정·승인 기록
언어: 문제·대안·영향·검증 계획·승인 요청·조건은 한국어로 작성한다. 인용한 답변·규칙 원문과 고정 키·상태·ID는 보존한다.
읽기: 해당 decision_id의 기록을 확인한다. 유효한 승인에는 APPROVED, 실제 사람의 근거, 일치하는 범위, 충족된 조건이 필요하다.
승인 재사용: 같은 범위의 승인을 다시 요청하지 않는다. 모르는 시각은 명시하며 시간·승인을 꾸미거나 양식을 승인으로 취급하지 않는다.
결정 변경: supersedes를 포함한 새 결정을 추가하고 기존 근거와 기록을 보존한다.
허용값 status: PROPOSED | APPROVED | REJECTED | DEFERRED | SUPERSEDED.

```yaml
schema_version: 3.1.0
template: false
record_type: decision_registry
decisions:
- decision_id: DEC-DOC-002
  title: 공통 문서 구조 개정
  status: APPROVED
  approver: current_user
  approved_at: null
  recorded_at: '2026-09-20T08:00:12Z'
  evidence:
  - kind: EXPLICIT_REQUEST
    source: 현재 대화의 사용자 요청
    content: 핵심 규칙 30~60줄, 변수명 통일, 상세 worklog, 중요 작업 사람 동의, 상위 모델 우선, 기능별 현황과 토큰 절약을 포함한 각 md 파일 작성 요청.
  scope:
  - 요청에 맞춘 문서 패키지 작성
  - 기존 매뉴얼 분리·개정
  excluded_scope:
  - 제품 요구사항·스택·도메인 이름 확정
  - 제품 코드 변경
  - 팀 전체 채택
  - 병합·배포
  conditions:
  - 요구사항·기능 미정 상태 유지
  source_version: 1.0.0
  target_version: 2.0.0
  missing_reasons:
    approved_at: 원본 요청 메시지의 정확한 시각은 제공되지 않음. 기록 시각으로 대체하지 않음.
    approver_role: 현재 사용자의 별도 팀 직책·권한 범위는 미확인.
  worklog_refs:
  - docs/worklogs/TASK-DOC-002__codex__20260920T062153Z.md
- decision_id: DEC-DOC-003
  title: AI 지시문과 구조화된 기록으로 문서 형식 개정
  status: APPROVED
  approver: current_user
  approved_at: null
  recorded_at: '2026-09-20T08:00:12Z'
  evidence:
  - kind: EXPLICIT_REQUEST
    source: 현재 대화의 사용자 요청
    content: 이건 사람이 읽기좋은 문서같은데 AI가 읽기 좋은 문서로 바꿔줘
  scope:
  - 기존 요청의 의미를 유지한 문서 형식 개정
  - 조건문·고정 상태값·YAML 기록 양식 도입
  - 이전 사건 보존과 새 개정 로그 작성
  excluded_scope:
  - 제품 요구사항·코드·계약 변경
  - 이 문서의 팀 운영 규정 채택
  - 모델 전환·과금 승인
  - 병합·배포
  conditions:
  - 핵심 규칙 30~60줄 유지
  - 중요 변경의 사람 승인·중단 조건 유지
  - 미확정 사실을 확정하지 않음
  source_version: 2.0.1
  target_version: 3.0.0
  missing_reasons:
    approved_at: 원본 요청 메시지의 정확한 시각은 제공되지 않음.
    approver_role: 현재 사용자의 별도 팀 직책·권한 범위는 미확인.
  worklog_refs:
  - docs/worklogs/TASK-DOC-002__codex__20260920T070034Z.md
- decision_id: DEC-DOC-004
  title: 사람용 기록 한국어화와 포트폴리오 설계 문서 작성
  status: APPROVED
  approver: current_user
  approved_at: null
  recorded_at: '2026-09-20T08:29:53Z'
  evidence:
  - kind: EXPLICIT_REQUEST
    source: 현재 대화의 사용자 요청
    content: worklog나 오류같은 사람이 직접 확인해야 하는 문서들은 한글로 작성되게 할 수 있어? 그리고 포트폴리오용으로 깃허브에 추가해야하니까 이런방식으로 서로 다른 AI모델의 판단
      차이를 줄이기위해 이런 방법을 썻다는 md파일도 만들어줘
  scope:
  - 한국어 작업·오류·승인·상태·완료 기록 규칙과 양식 개정
  - 고정 키·상태 코드·원문 증거 유지
  - GitHub에 추가할 한국어 설계 설명 MD 작성
  - 새 개정 로그와 ZIP 작성
  excluded_scope:
  - GitHub 원격 저장소 수정·공개·커밋·병합
  - 다중 모델 비교 실험을 수행했다고 주장
  - 제품 요구사항·코드 변경
  - 과거 사건 원문 수정
  conditions:
  - 핵심 규칙 30~60줄 유지
  - 미확정 사실과 실측하지 않은 효과를 성과로 적지 않음
  source_version: 3.0.0
  target_version: 3.1.0
  worklog_refs:
  - docs/worklogs/TASK-DOC-003__codex__20260920T082615Z.md
  missing_reasons:
    approved_at: 사용자 메시지의 정확한 발생 시각이 제공되지 않음.
    approver_role: 현재 사용자의 별도 팀 직책은 미확인.
```

새 결정 양식: 제안 상태는 실행 허가가 아니다.
```yaml
template: true
record_type: decision
decision_id: null
title: null
status: PROPOSED
recorded_at: null
task_refs: []
feature_refs: []
rule_refs: []
problem: null
evidence: []
current_behavior: null
proposed_change: null
options: []
recommendation_reason: null
affected_paths: []
affected_contracts: []
impact: null
verification_plan: []
rollback_plan: []
decision_request: null
approver: null
approved_at: null
scope: []
excluded_scope: []
conditions: []
supersedes: []
revisit_when: []
worklog_refs: []
missing_reasons: {}
```
