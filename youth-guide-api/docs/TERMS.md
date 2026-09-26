# TERMS
READ: matching concept/term_id records. WHEN new shared meaning/name or rename: require scoped human approval before use.
WHEN conflict: compare definitions, types, units and existing contracts; do not invent an alias to bypass the conflict.
APPROVED terms are usable only within their approval scope. DEPRECATED records retain replacement and migration evidence.
```yaml
schema_version: 3.0.0
template: false
record_type: term_registry
inventory_status: UNVERIFIED
terms: []
```
COPY_ON new_term_record; fill actual values; template=true is not a registered term.
```yaml
template: true
record_type: term
term_id: null
definition: null
canonical_name: null
collection_name: null
language_names: {}
api_name: null
db_name: null
external_name: null
type: null
unit: null
null_semantics: null
status: PROPOSED
approval_refs: []
source_refs: []
used_by: []
replacement_term_id: null
migration_ref: null
missing_reasons: {}
```
ENUM status: PROPOSED | APPROVED | DEPRECATED. null=unknown/unset; []=no recorded entries; neither proves absence in code.
