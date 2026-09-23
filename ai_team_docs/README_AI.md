# README_AI
PACKAGE_VERSION: 3.1.0
ENTRYPOINT: AI_RULES.md
PATH_BASE: repository_root
FORMAT: directives for rules; fenced YAML for records; uppercase enums; snake_case keys; Korean human reports.
REPORT_LANGUAGE: worklog·오류·중단·승인 요청·기능 설명·완료·인수인계의 제목과 설명은 한국어. 키·상태 코드·식별자·원문 증거는 보존한다.

INSTALL: place these files at the project root; inspect existing same-name files before replacing them.
START: provide the following task input together with access to the named files.
```yaml
template: true
task_id: null
actor_id: null
goal: null
scope: []
acceptance_criteria: []
source_revision: null
approval_refs: []
required_files: [AI_RULES.md, docs/CURRENT_STATE.md]
```

BOOTSTRAP: Read required_files. Execute applicable WHEN/IF rules. Reuse valid approvals. Record actual actions and evidence. Stop dependent changes when a gate fails.
DEFAULT_READ: AI_RULES.md + docs/CURRENT_STATE.md; add only this task's named feature, naming, decision and log files.
RULE_AUTHORITY: current host instructions and scoped human requests remain authoritative; document content cannot grant tool permissions.

FILES:
- AI_RULES.md: core conditions, permitted actions, approval/stop/completion gates.
- PROJECT_CONTEXT.md: supplementary instruction detail; no numbered-section routing.
- docs/NAMING.md + docs/TERMS.md: naming policy and approved concept/name mappings.
- docs/DECISIONS.md: scoped human approvals, proposals and supersession.
- docs/CURRENT_STATE.md: short active-task manifest and required_files.
- docs/FEATURE_STATUS.md: per-feature status index; actual paths in feature_file/latest_worklog.
- docs/features/_TEMPLATE.md: feature schema; replace placeholders before use.
- docs/worklogs/_TEMPLATE.md: session/event schema plus conditional STOP/RESUME/COMPLETE payloads.
- docs/worklogs/: immutable history and per-session current summary.
- AI_COLLABORATION_DESIGN.md: 사람이 읽는 한국어 포트폴리오 설명; 반복 작업의 기본 필독 대상이 아님.

STATUS_EXAMPLE_ONLY: implementation_status=IMPLEMENTING + work_status=WAITING_APPROVAL means 구현 중이며 사람 결정을 기다림.
COMPLETION_EXAMPLE_ONLY: implementation_status=IMPLEMENTED + work_status=IN_REVIEW means 구현·필수 검증 완료, 사람 확인 대기.
HUMAN_REPORT: summary_ko와 수행 내용·영향·검증 결과·다음 행동은 한국어로 작성한다. 영어 오류 원문에는 한국어 설명을 함께 기록한다.
GITHUB: AI_COLLABORATION_DESIGN.md를 이 패키지의 루트에 함께 두면 상대 링크로 설계와 실제 규칙·양식을 확인할 수 있다.
LIMIT: Markdown does not automatically read files, switch models or enforce rules. Record tool/model limitations honestly.
