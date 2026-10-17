# WBS 12.04 시험후기 댓글 기능 작업 기록

```yaml
schema_version: 3.1.0
template: false
record_type: session
summary_ko: 시험후기 댓글·대댓글과 구조화된 reply 대상, baseline 0 기준 V1 migration을 구현하고 자동 테스트를 통과했다. 실제 MySQL 적용은 수행하지 않았으며 사람 코드 검토와 로컬 통합 검증을 기다린다.
task_id: WBS-12-04-REVIEW-COMMENT
session_id: WBS-12-04-REVIEW-COMMENT__codex__20261012T134019Z
kind: FEATURE
feature_id: WBS-12-04-REVIEW-COMMENT
human_owner: current_user
actor_id: codex
tool: Codex desktop
actual_model: null
started_at: '2026-10-12T13:40:19Z'
recorded_at: '2026-10-12T13:40:19Z'
timezone: UTC
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24
existing_edits: []
rules_version: 3.1.0
read_files:
  - AI_RULES.md
  - PROJECT_CONTEXT.md
  - README_AI.md
  - docs/CURRENT_STATE.md
  - docs/FEATURE_STATUS.md
  - docs/DECISIONS.md
  - docs/NAMING.md
  - docs/TERMS.md
  - docs/features/_TEMPLATE.md
  - docs/worklogs/_TEMPLATE.md
goal: 승인된 정책에 따라 시험후기 댓글·1단계 대댓글의 조회·작성·수정·삭제와 게시글 commentCount를 구현한다.
scope:
  - 댓글·대댓글 API와 scalar FK Entity
  - 최상위 댓글 페이지와 대댓글 일괄 조회
  - 혼합 삭제·tombstone 정책
  - commentCount 조회 계약
  - 인증·CSRF·공통 예외·version 처리
  - 09.03 게시글 삭제 연계
  - 자동 테스트와 AI 문서 갱신
approval_refs:
  - 현재 대화의 사용자 요청
implementation_status: IMPLEMENTED
work_status: IN_REVIEW
verification_status: PARTIAL
review_status: PENDING
integration_status: BRANCH_ONLY
deployment_status: NOT_DEPLOYED
latest_event_id: EVT-IMPLEMENTED-008
previous_log: null
remaining_work:
  - 실제 MySQL 통합 검증
  - 사람 코드 검토
resume_when: []
next_action: 사용자 코드 검토 후 로컬 또는 격리된 MySQL에서 baseline 0과 V1 migration을 검증한다.
missing_reasons:
  actual_model: 실행 환경에서 정확한 모델 식별 정보가 제공되지 않음.
not_applicable_reasons: {}
```


```yaml
template: false
record_type: event
summary_ko: 현재 브랜치와 09.01~10.03 통합 상태, 기존 review·security·exception 구조, Flyway 준비 상태를 확인했다.
event_id: EVT-START-001
event_type: START
actor_id: codex
recorded_at: '2026-10-12T13:40:19Z'
occurred_at: '2026-10-12T13:40:19Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24
rule_refs:
  - {rule_id: C01, rule_text: "MUST identify task.goal, scope, acceptance criteria, source revision and existing edits before implementation.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - feature-12-04-review-comment가 origin/develop과 같은 revision이고 working tree가 clean임을 확인했다.
  - 기존 Controller·Service·Repository·Entity·SecurityConfig·공통 예외와 09.01~10.03 feature 기록을 확인했다.
  - Flyway 의존성은 있으나 migration 파일·설정·버전 관례가 없음을 확인했다.
changes: []
checks:
  - command: git status --short
    environment: 실제 로컬 저장소
    input: null
    expected: 기존 변경 없음
    observed: 출력 없음
    result: PASSED
    evidence_ref: source_revision
unperformed_checks:
  - 자동 테스트와 assemble은 구현 후 수행한다.
state_before: {implementation_status: NOT_STARTED, work_status: QUEUED}
state_after: {implementation_status: IMPLEMENTING, work_status: ACTIVE}
remaining_work:
  - 댓글 구현과 테스트
  - 문서 상태 갱신
next_action: 승인된 Java 범위를 최소 변경으로 구현한다.
state_updates:
  - {path: docs/features/WBS-12-04-REVIEW-COMMENT.md, result: NOT_APPLIED, evidence_ref: null}
  - {path: docs/FEATURE_STATUS.md, result: NOT_APPLIED, evidence_ref: null}
  - {path: docs/CURRENT_STATE.md, result: NOT_APPLIED, evidence_ref: null}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 기존 DB가 비어 있지 않은 상황에서 Flyway 최초 migration을 안전하게 적용할 baseline/version 정책 근거가 없어 migration 파일과 설정 변경을 보류했다. 독립적인 Java 구현과 테스트는 계속한다.
event_id: EVT-STOP-002
event_type: STOP
actor_id: codex
recorded_at: '2026-10-12T13:40:19Z'
occurred_at: '2026-10-12T13:40:19Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24
rule_refs:
  - {rule_id: C10, rule_text: "WHEN fix violates rules OR approval/evidence is missing: STOP dependent implementation; preserve edits; prepare proposal/impact/checks; request a human decision.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청 중 baseline 전략이 없으면 공유 DB 영향 설정을 만들지 말라는 조건
actions:
  - build.gradle의 Flyway 의존성을 확인했다.
  - 저장소 전체와 Git 이력에 migration 파일과 naming/version convention이 없음을 확인했다.
  - 기존 worklog에 기준 버전은 DATA 담당과 결정한다는 경고를 확인했다.
changes: []
checks:
  - command: rg -n -i 'flyway|baseline|db/migration|schema_history' . && git log --all --name-status -- '*.sql'
    environment: 실제 로컬 저장소
    input: null
    expected: 기존 baseline/version 근거 확인
    observed: 의존성만 존재하고 migration 이력은 없으며 DATA 담당 결정 필요 기록이 있음
    result: PARTIAL
    evidence_ref: docs/worklogs/TASK-MEM-001__claude__20261008T210700Z.md
unperformed_checks:
  - 공용 DB 접속과 Flyway 실행은 사용자 금지에 따라 수행하지 않는다.
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE}
state_after: {implementation_status: IMPLEMENTING, work_status: ACTIVE}
remaining_work:
  - Flyway baseline/version 결정
  - Java 구현과 검증
next_action: migration에 의존하지 않는 코드·테스트를 진행하고 DB 적용 제한을 보고한다.
state_updates: []
missing_reasons: {}
stop:
  category: APPROVAL_GAP
  location: {path: src/main/resources/db/migration, symbol: community_post_comment migration, stage: migration 작성 전}
  blocked_scope:
    - 실행 가능한 Flyway migration 파일 생성
    - 기존 공유 DB baseline 설정 및 적용
  conflicting_evidence:
    - Flyway로 신규 스키마를 관리하라는 현재 사용자 결정
    - migration 이력과 최초 baseline/version 전략이 없으면 임의 설정하지 말라는 현재 사용자 조건
    - docs/worklogs/TASK-MEM-001__claude__20261008T210700Z.md의 DATA 담당 결정 필요 기록
  observed_error: {message: null, summary_ko: 실제 오류는 발생하지 않았으며 위험한 최초 적용을 실행 전 차단했다., input: null, reproduction: null, result: null}
  predicted_risk:
    - trigger: 비어 있지 않고 schema history가 없는 DB에 임의의 첫 migration을 적용
      affected_paths: [src/main/resources/db/migration, application 설정]
      possible_failure: 애플리케이션 시작 실패 또는 기존 스키마와 migration 이력 불일치
  attempts:
    - 저장소·Git 이력·문서에서 naming/version/baseline 근거 검색
  autonomous_fix_forbidden_because: DB migration 의미와 baseline 정책은 사람 승인이 필요한 공유 데이터 계약이다.
  preserved_edits: []
  independent_allowed_work:
    - 댓글 Java 구현
    - mock/static 자동 테스트
    - migration 미적용 상태와 SQL 요구사항 문서화
  decision_request:
    question: 기존 youth_guide DB의 Flyway 최초 baseline/version을 어떤 값과 절차로 등록할지 결정이 필요하다.
    proposed_change: DATA 담당 결정 후 그 convention에 맞춰 community_post_comment 생성 migration을 추가한다.
    options:
      - 기존 스키마 전체를 baseline으로 등록한 뒤 다음 버전에 댓글 migration 추가
      - 운영 전 빈 DB부터 재구성 가능한 전체 초기 migration을 먼저 도입
    impact: 결정 전에는 댓글 Java 코드는 컴파일 가능하지만 실제 DB 실행은 community_post_comment 테이블 부재로 불가능하다.
    requested_scope:
      - Flyway baseline/version convention
      - 기존 DB 적용 절차
  resume_when:
    - DATA 담당 또는 사용자가 최초 baseline/version 전략을 명시한다.
  resume_checks:
    - 현재 DB의 schema history 유무와 baseline 버전을 읽기 전용으로 확인
    - 정해진 migration naming convention 확인
  user_notification_ref: 현재 대화의 중간 진행 보고
```

```yaml
template: false
record_type: event
summary_ko: 댓글 Entity·DTO·Controller·Service·Repository와 기존 게시글 조회·삭제 연계를 구현했다. 첫 review 테스트에서 불필요한 Mockito stubbing 2건이 실패해 해당 테스트 fixture만 lenient 처리한 뒤 재검증했다.
event_id: EVT-CHECKPOINT-003
event_type: CHECKPOINT
actor_id: codex
recorded_at: '2026-10-12T13:55:21Z'
occurred_at: '2026-10-12T13:55:21Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
rule_refs:
  - {rule_id: C05, rule_text: "MAY make small reversible internal edits ONLY within approved scope, preserving public behavior/contracts and others' edits.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - scalar FK와 @Version을 사용하는 CommunityPostComment를 추가했다.
  - 최상위 page와 replies 일괄 조회, mine·tombstone 변환을 구현했다.
  - LoginMember·CSRF·EXAM_REVIEW 게시글 잠금을 사용하는 댓글 write를 구현했다.
  - 혼합 삭제와 마지막 대댓글 삭제 시 tombstone 부모 정리를 구현했다.
  - 기존 게시글 조회 commentCount와 게시글 삭제 댓글 연계를 추가했다.
  - 댓글 GET 경로만 SecurityConfig에서 공개했다.
changes:
  - {path: src/main/java/com/youthguide/api/review/domain/CommunityPostComment.java, symbol: CommunityPostComment, operation: ADD, reason: 댓글 scalar FK·tombstone·version 도메인}
  - {path: src/main/java/com/youthguide/api/review/controller/CommunityPostCommentController.java, symbol: CommunityPostCommentController, operation: ADD, reason: 승인된 댓글 HTTP API}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentQueryService.java, symbol: getComments, operation: ADD, reason: thread page와 replies DTO 변환}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentCommandService.java, symbol: createComment/createReply/updateComment/deleteComment, operation: ADD, reason: 댓글 명령 정책}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommentQueryRepository.java, symbol: findTopLevelPage/findRepliesByParentCommentIds, operation: ADD, reason: N+1 없는 2-query 조회}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommentCommandRepository.java, symbol: comment lock/write/count, operation: ADD, reason: 댓글 write와 version 처리}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostQueryRepository.java, symbol: findPage/findDetail, operation: MODIFY, reason: 삭제되지 않은 댓글 전체 commentCount}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommandService.java, symbol: deletePost, operation: MODIFY, reason: 게시글 삭제 시 대댓글·댓글 자식 우선 삭제}
  - {path: src/main/java/com/youthguide/api/global/config/SecurityConfig.java, symbol: securityFilterChain, operation: MODIFY, reason: 댓글 GET만 익명 공개}
  - {path: src/main/java/com/youthguide/api/global/exception/ErrorCode.java, symbol: COMMENT_NOT_FOUND/COMMENT_DEPTH_EXCEEDED, operation: MODIFY, reason: 댓글 대상·깊이 공통 오류}
checks:
  - command: .\gradlew.bat test --tests "com.youthguide.api.review.*"
    environment: 로컬 Gradle·DB 쓰기 없음
    input: 최초 실행
    expected: review 테스트 통과
    observed: CommunityPostCommentQueryServiceTest 2건 UnnecessaryStubbingException
    result: FAILED
    evidence_ref: build/reports/tests/test/index.html
unperformed_checks:
  - 실제 MySQL comment table 기반 조회·write·lock·FK 검증
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: NOT_RUN}
state_after: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: PARTIAL}
remaining_work:
  - fixture 수정 후 재검증
  - Flyway migration 결정
next_action: 전체 자동 테스트와 assemble을 실행한다.
state_updates:
  - {path: docs/features/WBS-12-04-REVIEW-COMMENT.md, result: APPLIED, evidence_ref: 현재 diff}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT 항목}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT active task}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: Mockito fixture를 정리한 뒤 review와 전체 테스트, assemble, diff 검사를 모두 통과했다. 실제 MySQL과 Flyway migration은 baseline 결정 전까지 검증하지 않았다.
event_id: EVT-VERIFY-004
event_type: VERIFY
actor_id: codex
recorded_at: '2026-10-12T13:55:21Z'
occurred_at: '2026-10-12T13:55:21Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
rule_refs:
  - {rule_id: C12, rule_text: "MUST NOT weaken rules/tests to hide failure; model output never substitutes for approval or verification; obey host permissions.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 사용자 요청
actions:
  - tombstone에서 호출되지 않는 projection getter stubbing만 lenient로 변경했다.
  - review·전체 테스트와 assemble을 실행했다.
  - whitespace 오류와 변경 범위를 확인했다.
changes:
  - {path: src/test/java/com/youthguide/api/review/service/CommunityPostCommentQueryServiceTest.java, symbol: projection fixture, operation: MODIFY, reason: 분기별 미사용 Mockito stubbing 허용}
checks:
  - command: .\gradlew.bat test --tests "com.youthguide.api.review.*"
    environment: 로컬 Gradle·MySQL opt-in 비활성
    input: null
    expected: review 회귀 통과
    observed: 120건 중 110건 통과, 기존 MySQL opt-in 10건 건너뜀, 실패·오류 0건
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\gradlew.bat test
    environment: 로컬 Gradle
    input: null
    expected: 전체 회귀 통과
    observed: 156건 중 146건 통과, 기존 MySQL opt-in 10건 건너뜀, 실패·오류 0건
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\gradlew.bat assemble
    environment: 로컬 Gradle
    input: null
    expected: 패키징 성공
    observed: BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: Gradle 출력
  - command: git diff --check
    environment: 실제 로컬 저장소
    input: null
    expected: whitespace 오류 없음
    observed: 출력 없음, exit code 0
    result: PASSED
    evidence_ref: Git 출력
unperformed_checks:
  - Flyway migration 적용
  - 실제 MySQL community_post_comment native SQL·FK·lock 통합 검증
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: PARTIAL}
state_after: {implementation_status: IMPLEMENTING, work_status: WAITING_APPROVAL, verification_status: PARTIAL}
remaining_work:
  - Flyway baseline/version 결정과 migration 작성
  - 격리된 MySQL 통합 검증
  - 사람 코드 검토
next_action: 사용자 또는 DATA 담당의 Flyway 최초 baseline/version 결정을 기다린다.
state_updates:
  - {path: docs/features/WBS-12-04-REVIEW-COMMENT.md, result: APPLIED, evidence_ref: check_evidence}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 사용자가 구조화된 reply 대상 정책을 승인해 parent_comment_id는 root로 평탄화하고 reply_to_comment_id는 실제 대상 대댓글을 가리키도록 Java 구현과 테스트를 수정했다. Flyway migration은 baseline/version 결정 전까지 계속 보류한다.
event_id: EVT-CHECKPOINT-005
event_type: CHECKPOINT
actor_id: codex
recorded_at: '2026-10-15T01:39:12Z'
occurred_at: '2026-10-15T01:39:12Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
rule_refs:
  - {rule_id: C05, rule_text: "MAY make small reversible internal edits ONLY within approved scope, preserving public behavior/contracts and others' edits.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화에서 사용자가 reply_to_comment_id·현재 nickname·대상 삭제 시 mention 제거 정책을 명시적으로 승인
  - 현재 대화에서 사용자가 Flyway migration과 baseline 설정 보류를 명시
actions:
  - 최상위 댓글 대상 답글은 parent=root, replyTo=null로 유지했다.
  - 대댓글 대상 답글은 target의 root를 잠금 확인한 뒤 parent=root, replyTo=target으로 저장하도록 변경했다.
  - 실제 depth 2 이상인 비정상 데이터를 발견하면 COMMENT_DEPTH_EXCEEDED로 차단하도록 무결성 방어를 유지했다.
  - native query가 reply target 작성자의 현재 nickname을 조회하고 응답에 nullable replyToCommentId·replyToNickname을 반환하도록 변경했다.
changes:
  - {path: src/main/java/com/youthguide/api/review/domain/CommunityPostComment.java, symbol: replyToCommentId, operation: MODIFY, reason: 실제 답글 대상 scalar ID 보존}
  - {path: src/main/java/com/youthguide/api/review/service/CommunityPostCommentCommandService.java, symbol: createReply, operation: MODIFY, reason: 대댓글 대상 요청을 root 아래로 평탄화}
  - {path: src/main/java/com/youthguide/api/review/repository/CommunityPostCommentQueryRepository.java, symbol: comment queries, operation: MODIFY, reason: reply 대상 현재 nickname 조회}
  - {path: src/main/java/com/youthguide/api/review/repository/projection/CommunityPostCommentProjection.java, symbol: reply target getters, operation: MODIFY, reason: native alias 매핑}
  - {path: src/main/java/com/youthguide/api/review/dto/CommunityPostCommentResponse.java, symbol: replyTo fields, operation: MODIFY, reason: nullable 구조화된 mention 응답}
  - {path: src/main/java/com/youthguide/api/review/dto/CommunityPostCommentReplyResponse.java, symbol: replyTo fields, operation: MODIFY, reason: nullable 구조화된 mention 응답}
  - {path: src/main/java/com/youthguide/api/global/exception/ErrorCode.java, symbol: COMMENT_DEPTH_EXCEEDED, operation: MODIFY, reason: 정상 대댓글 대상 요청이 아닌 비정상 실제 깊이용 메시지로 정정}
checks:
  - command: .\gradlew.bat test --tests "com.youthguide.api.review.service.CommunityPostCommentCommandServiceTest" --tests "com.youthguide.api.review.service.CommunityPostCommentQueryServiceTest" --tests "com.youthguide.api.review.controller.CommunityPostCommentControllerTest" --tests "com.youthguide.api.review.repository.CommunityPostCommentRepositoryTest"
    environment: 로컬 Gradle·DB 쓰기 없음
    input: null
    expected: reply 평탄화·mention 응답 집중 테스트 통과
    observed: BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: Gradle 출력
unperformed_checks:
  - reply_to_comment_id self FK와 ON DELETE SET NULL 실제 MySQL 검증
state_before: {implementation_status: IMPLEMENTING, work_status: WAITING_APPROVAL, verification_status: PARTIAL}
state_after: {implementation_status: IMPLEMENTING, work_status: WAITING_APPROVAL, verification_status: PARTIAL}
remaining_work:
  - Flyway baseline/version 결정과 migration 작성
  - 격리된 MySQL 통합 검증
next_action: 전체 회귀와 assemble 결과를 기록하고 사용자 검토를 요청한다.
state_updates:
  - {path: docs/features/WBS-12-04-REVIEW-COMMENT.md, result: APPLIED, evidence_ref: 현재 diff}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: reply target 평탄화와 구조화된 mention 변경 후 review·전체 테스트와 assemble을 모두 통과했다. MySQL opt-in 테스트와 migration은 승인된 보류 정책에 따라 실행하지 않았다.
event_id: EVT-VERIFY-006
event_type: VERIFY
actor_id: codex
recorded_at: '2026-10-15T01:39:12Z'
occurred_at: '2026-10-15T01:39:12Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
rule_refs:
  - {rule_id: C12, rule_text: "MUST NOT weaken rules/tests to hide failure; model output never substitutes for approval or verification; obey host permissions.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 reply 대상 및 Flyway 보류 결정
actions:
  - A의 직접 대댓글 B와 B 대상 C, C 대상 D가 모두 A 아래에 중첩되는 서비스·조회 테스트를 추가했다.
  - 직접 답글은 replyTo=null이고 대댓글 대상 답글은 현재 대상 nickname을 반환하는 계약을 검증했다.
  - 비정상 depth 2 데이터와 삭제된 root에 대한 작성 차단을 검증했다.
checks:
  - command: .\gradlew.bat test --tests "com.youthguide.api.review.*"
    environment: 로컬 Gradle·MySQL opt-in 비활성
    input: null
    expected: review 회귀 통과
    observed: 123건 중 113건 통과, 기존 MySQL opt-in 10건 건너뜀, 실패·오류 0건
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\gradlew.bat test
    environment: 로컬 Gradle·MySQL opt-in 비활성
    input: null
    expected: 전체 회귀 통과
    observed: 159건 중 149건 통과, 기존 MySQL opt-in 10건 건너뜀, 실패·오류 0건
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\gradlew.bat assemble
    environment: 로컬 Gradle
    input: null
    expected: 패키징 성공
    observed: BUILD SUCCESSFUL
    result: PASSED
    evidence_ref: Gradle 출력
unperformed_checks:
  - Flyway migration 적용
  - 실제 MySQL reply_to_comment_id·ON DELETE SET NULL·native JOIN 통합 검증
state_before: {implementation_status: IMPLEMENTING, work_status: WAITING_APPROVAL, verification_status: PARTIAL}
state_after: {implementation_status: IMPLEMENTING, work_status: WAITING_APPROVAL, verification_status: PARTIAL}
remaining_work:
  - Flyway baseline/version 결정과 migration 작성
  - 격리된 MySQL 통합 검증
  - 사람 코드 검토
next_action: 사용자 검토 후 Flyway 최초 baseline/version 결정을 기다린다.
state_updates:
  - {path: docs/features/WBS-12-04-REVIEW-COMMENT.md, result: APPLIED, evidence_ref: check_evidence}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT}
missing_reasons: {}
```

```yaml
template: false
record_type: event
summary_ko: 사용자가 기존 스키마의 Flyway baseline version을 0, 첫 댓글 migration을 V1__create_community_post_comment.sql로 승인해 migration 작성 중단 조건이 해소됐다.
event_id: EVT-RESUME-007
event_type: RESUME
actor_id: codex
recorded_at: '2026-10-15T01:52:14Z'
occurred_at: '2026-10-15T01:52:14Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
rule_refs:
  - {rule_id: C08, rule_text: "REUSE existing matching approval; scope expansion requires approval; silence and AI recommendations are not approval; implementation approval is not deployment approval.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화에서 사용자가 baseline version 0과 V1__create_community_post_comment.sql을 명시적으로 승인
actions:
  - 기존 migration과 baseline 설정 부재를 재확인했다.
  - application-local.yaml의 Flyway 비활성화는 보존하고 공통 application.yaml에도 자동 baseline 설정을 추가하지 않기로 했다.
changes: []
checks:
  - command: 저장소 SQL·Flyway 설정 검색
    environment: 실제 로컬 저장소·DB 접속 없음
    input: null
    expected: 기존 migration과 공통 baseline 설정 여부 확인
    observed: 기존 migration 없음, application-local.yaml에 flyway.enabled=false만 존재
    result: PASSED
    evidence_ref: 현재 명령 출력
unperformed_checks:
  - 공용 DB 및 로컬 MySQL 접속
state_before: {implementation_status: IMPLEMENTING, work_status: WAITING_APPROVAL, verification_status: PARTIAL}
state_after: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: PARTIAL}
remaining_work:
  - V1 migration 작성과 자동 검증
  - 실제 MySQL 통합 검증
next_action: 승인된 V1 migration을 작성하고 DB 접속 없는 검증을 수행한다.
state_updates: []
missing_reasons: {}
resolution:
  prior_stop_event_ref: EVT-STOP-002
  conflict_evidence:
    - 기존 DB가 비어 있지 않고 schema history가 없는 상태에서 최초 baseline/version이 미정이었음
  new_evidence:
    - 사용자가 baseline version 0과 댓글 migration V1을 명시
  approval_refs:
    - 현재 대화의 명시적 결정
  rules_checked: [C07, C08, C10]
  resume_conditions_met:
    - Flyway baseline/version convention 명시
    - migration filename 명시
  preserved_contracts:
    - application-local.yaml 미변경
    - 공용 DB 미접속·미적용
    - 기존 미커밋 12.04 변경 보존
  changed_scope:
    - src/main/resources/db/migration/V1__create_community_post_comment.sql 생성
  source_revision_rechecked: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
  recheck_evidence:
    - branch와 git status 및 Flyway 설정 검색 결과
```

```yaml
template: false
record_type: event
summary_ko: baseline 0 다음에 실행할 V1 댓글 테이블 migration을 추가하고 review·전체 테스트, assemble, 필수 SQL 구문과 패키징을 검증했다. 실제 MySQL 적용은 수행하지 않았다.
event_id: EVT-IMPLEMENTED-008
event_type: IMPLEMENTED
actor_id: codex
recorded_at: '2026-10-15T01:52:14Z'
occurred_at: '2026-10-15T01:52:14Z'
source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
rule_refs:
  - {rule_id: C17, rule_text: "ON IMPLEMENTED/COMPLETE: record feature/output, acceptance evidence, file-symbol edges, payload/contract, verified flow, exceptions, limitations, review and handoff.", source_path: AI_RULES.md}
approval_refs:
  - 현재 대화의 baseline 0·V1 migration·댓글 테이블 계약 승인
actions:
  - CommunityPostComment Entity와 일치하는 BIGINT·VARCHAR(255)·DATETIME(6)·lock_version 컬럼을 정의했다.
  - post·author·parent는 RESTRICT, reply target은 ON DELETE SET NULL로 정의했다.
  - top-level page·reply batch·commentCount·author·reply target 조회 패턴에 맞춘 인덱스를 정의했다.
  - 공통 및 local application 설정은 변경하지 않았다.
changes:
  - {path: src/main/resources/db/migration/V1__create_community_post_comment.sql, symbol: community_post_comment, operation: ADD, reason: baseline 0 다음 최초 댓글 스키마 migration}
checks:
  - command: .\gradlew.bat test --tests "com.youthguide.api.review.*"
    environment: 로컬 Gradle·MySQL opt-in 비활성
    input: null
    expected: review 회귀 통과
    observed: 123건 중 113건 통과, MySQL opt-in 10건 건너뜀, 실패·오류 0건
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\gradlew.bat test
    environment: 로컬 Gradle·MySQL opt-in 비활성
    input: null
    expected: 전체 회귀 통과
    observed: 159건 중 149건 통과, MySQL opt-in 10건 건너뜀, 실패·오류 0건
    result: PASSED
    evidence_ref: build/test-results/test
  - command: .\gradlew.bat assemble
    environment: 로컬 Gradle
    input: null
    expected: migration 리소스를 포함한 패키징 성공
    observed: BUILD SUCCESSFUL, BOOT-INF/classes/db/migration/V1__create_community_post_comment.sql 포함
    result: PASSED
    evidence_ref: build/libs bootJar
  - command: migration 필수 구문 정적 검사
    environment: 로컬 파일·DB 접속 없음
    input: null
    expected: 필수 컬럼·FK 정책·인덱스 포함
    observed: MIGRATION_STATIC_CHECK=PASSED
    result: PASSED
    evidence_ref: src/main/resources/db/migration/V1__create_community_post_comment.sql
unperformed_checks:
  - 실제 MySQL V1 migration 실행
  - flyway_schema_history baseline 0 및 V1 기록 확인
  - 실제 FK·CHECK·인덱스와 삭제 순서 검증
state_before: {implementation_status: IMPLEMENTING, work_status: ACTIVE, verification_status: PARTIAL}
state_after: {implementation_status: IMPLEMENTED, work_status: IN_REVIEW, verification_status: PARTIAL}
remaining_work:
  - 사람 코드 검토
  - 로컬 또는 격리된 MySQL 통합 검증
next_action: 사용자 검토 후 로컬 MySQL에 명령행 override로 baseline 0과 V1을 적용해 검증한다.
state_updates:
  - {path: docs/features/WBS-12-04-REVIEW-COMMENT.md, result: APPLIED, evidence_ref: AC-14}
  - {path: docs/FEATURE_STATUS.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT}
  - {path: docs/CURRENT_STATE.md, result: APPLIED, evidence_ref: WBS-12-04-REVIEW-COMMENT}
missing_reasons: {}
completion:
  feature_id: WBS-12-04-REVIEW-COMMENT
  outputs:
    - src/main/resources/db/migration/V1__create_community_post_comment.sql
    - 시험후기 댓글·1단계 대댓글 Java 구현과 자동 테스트
  delivered_behavior: baseline 0 다음 V1에서 댓글 테이블과 root parent·실제 reply target·tombstone·version·FK·조회 인덱스를 생성할 수 있는 migration을 제공한다.
  scope:
    - 승인된 WBS 12.04 Java 기능
    - V1 댓글 테이블 migration
  excluded_scope:
    - 공용 DB 및 로컬 MySQL migration 실행
    - application-local.yaml과 공통 자동 baseline 설정 변경
  acceptance_results:
    - {ac_id: AC-01, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-02, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-03, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-04, status: MET, evidence_refs: [EVT-VERIFY-006]}
    - {ac_id: AC-05, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-06, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-07, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-08, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-09, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-10, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-11, status: MET, evidence_refs: [EVT-VERIFY-006]}
    - {ac_id: AC-12, status: MET, evidence_refs: [EVT-VERIFY-004]}
    - {ac_id: AC-13, status: MET, evidence_refs: [EVT-IMPLEMENTED-008]}
    - {ac_id: AC-14, status: MET, evidence_refs: [src/main/resources/db/migration/V1__create_community_post_comment.sql]}
  source_revision: 790bfd159bf8a646ee801d081598e8dfceb48f24 (+ 미커밋 12.04 변경)
  connections:
    - from: {path: src/main/resources/db/migration/V1__create_community_post_comment.sql, symbol: community_post_comment}
      to: {path: src/main/java/com/youthguide/api/review/domain/CommunityPostComment.java, symbol: CommunityPostComment, external_boundary: MySQL}
      payload: 댓글 ID·게시글 ID·작성자 ID·root parent·reply target·본문·UTC 시각·version
      contract_ref: WBS-12-04 댓글 테이블
      purpose: 승인된 댓글 Entity와 실제 MySQL 스키마 연결
      verification_evidence: [migration 필수 구문 정적 검사, assemble]
  verified_flow: baseline version 0 → V1 migration 탐색·패키징까지 검증; 실제 MySQL 적용과 FK 동작은 미검증
  verification_scope: MIXED
  exception_coverage:
    - migration 실패 시 애플리케이션 시작 실패를 그대로 노출하며 예외를 숨기지 않음
  limitations:
    - 기존 MySQL parent PK의 signed·precision 정합성과 SQL 실행은 아직 확인하지 않음
    - baseline-on-migrate는 공통 설정에 넣지 않아 로컬 실행 인자로 명시해야 함
  remaining_work:
    - 사람 코드 검토
    - 로컬 또는 격리된 MySQL 통합 검증
  review_status: PENDING
  review_evidence: []
  integration_status: BRANCH_ONLY
  deployment_status: NOT_DEPLOYED
  handoff: application-local.yaml, 공용 DB, 비밀값, commit·push·merge를 변경하거나 실행하지 않았다.
```
