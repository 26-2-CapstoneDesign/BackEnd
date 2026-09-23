# NAMING
VERSION: 3.0.0
APPLIES_WHEN: create_or_rename_identifier
INPUT: approved term records, actual declarations/references, existing language conventions.
OUTPUT: reused approved name OR proposed term awaiting approval.

N01 IF same meaning exists in docs/TERMS.md with status=APPROVED -> reuse its canonical_name; do not invent a synonym.
N02 IF same spelling has different meaning/type/unit/scope -> STOP the conflicting use; record evidence and request a decision.
N03 IF new shared concept OR shared rename -> add status=PROPOSED term; prepare affected paths/contracts; apply only after scoped human approval.
N04 IF local variable derived from an approved concept -> use established casing without new approval; preserve behavior/contracts.
N05 BEFORE rename -> inspect declarations, callers, serialized keys, DB mappings, tests, fixtures, dynamic/string references and docs.
N06 AFTER rename -> verify references + behavior; record term_id, before/after names, changed symbols and evidence.
N07 MUST_NOT treat a successful IDE rename as proof of API/DB compatibility; external fixed names need explicit mapping.

## CASING
Precedence: approved contract > mandatory framework form > established project rule > defaults below.
Defaults apply to NEW INTERNAL symbols only; they do not select a technology stack or rename existing public fields.
```yaml
casing_defaults:
  javascript_typescript_java: {variable: lowerCamelCase, parameter: lowerCamelCase, function: lowerCamelCase, type: UpperCamelCase, constant: UPPER_SNAKE_CASE}
  python: {variable: snake_case, parameter: snake_case, function: snake_case, type: UpperCamelCase, constant: UPPER_SNAKE_CASE}
  csharp: {local_variable: lowerCamelCase, parameter: lowerCamelCase, method: UpperCamelCase, public_property: UpperCamelCase, type: UpperCamelCase, constant: EXISTING_RULE_REQUIRED}
  other: HUMAN_DECISION_IF_NO_EXISTING_RULE
```

## SEMANTICS
- singular object; plural collection; boolean is/has/can/should; avoid double negation.
- verb-based function name matching actual side effects and absence/error contract.
- visible units where needed: Count, Ms, Bytes; date-only Date versus timestamp At follows approved definitions.
- camel acronyms: Id, Url, Api unless an existing contract requires otherwise.
- preserve distinction: absent, null, empty, zero and false; do not infer timezone or identifier scope from spelling.
- register shared abbreviations; avoid vague shared names data/info/temp without domain meaning.
- casing conversion changes spelling only; meanings/API keys/DB columns remain approved mappings.
- DO NOT register every local variable or add hypothetical domain concepts.
