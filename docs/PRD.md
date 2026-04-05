# Imperium Product Requirements Document

- Project: `Imperium`
- Document Type: Product Requirements Document
- Status: Draft v0.1
- Phase: Product Definition
- Owner: Caesar / Product
- Last Updated: 2026-03-29

## 1. Product Overview

### 1.1 Product Definition

`Imperium` is a multi-agent governance system built around a Roman institutional model.

The product does not treat AI collaboration as a free-form conversation between agents. Instead, it organizes AI work as a formal state process made of:

- imperial edict issuance
- senate deliberation
- tribune veto review
- caesar decision
- consul delegation
- institutional execution
- audit and archival

In product terms, `Imperium` is not only an orchestration tool. It is a governance operating system for complex AI work.

### 1.2 Product Thesis

Most agent systems optimize for autonomy. `Imperium` optimizes for governability.

The core thesis is:

- complex work should not go directly from request to execution
- high-value decisions benefit from structured disagreement
- veto power must be real, not decorative
- execution authority and decision authority should be separated
- the full path from order to archive should remain visible and auditable

### 1.3 Product Vision

Enable a single user, acting as Caesar, to command a governed AI state machine where every meaningful task can be debated, vetoed, approved, executed, audited, and archived with full traceability.

## 2. Problem Statement

### 2.1 Problems in Conventional Agent Systems

Conventional multi-agent systems commonly suffer from the following issues:

- agents collaborate too freely, making authority unclear
- planning and execution are often collapsed into one layer
- disagreement is weak or absent
- audit trails are incomplete or hard to interpret
- users can observe outputs but cannot govern the decision process
- risky work can move forward without formal challenge or approval

### 2.2 Product Opportunity

There is room for a system that makes multi-agent work feel more like institutional governance than autonomous improvisation.

The opportunity is to create a product where:

- authority is explicit
- conflicts are first-class
- users retain final sovereignty
- execution is systematic rather than ad hoc
- completed work leaves behind reusable institutional memory

## 3. Goals and Non-Goals

### 3.1 Goals

- Build a Roman governance model for agent orchestration
- Make senate deliberation a core product mechanic
- Give veto review real stopping power
- Place Caesar decision at key control points
- Separate policy formation from execution
- Preserve a complete activity trail from edict to archive
- Support multiple operational modes with different governance intensity

### 3.2 Non-Goals

- Simulate Roman history with historical completeness
- Build a role-playing game or entertainment-first product
- Support fully free-form peer-to-peer agent communication
- Optimize only for fastest possible execution
- Replace engineering delivery with theatrical framing alone

## 4. Target Users

### 4.1 Primary Users

- solo builders managing complex AI-assisted work
- technical founders making product and engineering decisions
- engineering leads who want structured AI delegation
- workflow designers building governed agent systems

### 4.2 Secondary Users

- research teams experimenting with AI governance
- product teams evaluating different decision-making workflows
- organizations needing auditable human-in-command orchestration

## 5. Product Principles

The following principles are normative and should guide all product, design, and engineering work.

### 5.1 Institution Before Autonomy

The system should define roles, handoff rules, and checkpoints before maximizing agent initiative.

### 5.2 Deliberation Before Execution

Complex work should move through a political or institutional decision layer before execution.

### 5.3 Real Veto Power

The tribune function must be able to block or return work, not merely issue warnings.

### 5.4 Caesar Retains Sovereignty

The user must remain the final authority on disputed or strategically important matters.

### 5.5 Full Traceability

Every important move should be visible as part of a continuous case history.

### 5.6 Distinct Powers

The product must preserve separation between:

- intake
- deliberation
- veto/review
- decision
- delegation
- execution
- audit
- archival

## 6. Roman Institutional Model

### 6.1 User Position

- `Caesar`: the human user, final decision authority, not a standard agent role

### 6.2 Product Roles

#### `praeco`

- Role: herald and intake officer
- Responsibilities:
  - receive imperial edicts
  - classify requests
  - normalize titles and briefs
  - recommend operating mode
  - route simple work directly to execution or complex work to senate review
- Explicit boundaries:
  - does not deliberate policy
  - does not execute work
  - does not override veto

#### `senator_strategos`

- Role: strategic senator
- Responsibilities:
  - assess strategic value
  - judge prioritization and sequencing
  - identify opportunity cost
  - propose staged motions

#### `senator_juris`

- Role: legal and institutional senator
- Responsibilities:
  - identify policy and rule conflicts
  - evaluate scope legitimacy
  - challenge violations of stated constraints

#### `senator_fiscus`

- Role: fiscal senator
- Responsibilities:
  - estimate cost and effort
  - propose lighter or phased alternatives
  - identify waste or over-expansion

#### `tribune`

- Role: tribune of the plebs / veto authority
- Responsibilities:
  - review senate output for user-interest, safety, and policy risk
  - approve, reject, or return motions for re-deliberation
  - escalate sensitive matters to Caesar decision
- Explicit boundaries:
  - does not directly execute work
  - does not become a substitute consul

#### `consul`

- Role: executive coordinator
- Responsibilities:
  - convert approved motions into delegated mandates
  - assign execution roles
  - coordinate dependencies and progress
  - aggregate work products for audit and archive

#### `legatus`

- Role: engineering and technical execution lead
- Responsibilities:
  - implement systems, code, and technical outputs
  - perform concrete delivery work

#### `praetor`

- Role: validation and rules enforcement officer
- Responsibilities:
  - testing
  - verification
  - acceptance review
  - quality and safety checks

#### `aedile`

- Role: presentation and public-facing works officer
- Responsibilities:
  - UI and UX work
  - documentation and presentation
  - delivery polish and structure

#### `quaestor`

- Role: finance and resource officer
- Responsibilities:
  - data and budget analysis
  - consumption reporting
  - cost awareness during execution

#### `scriba`

- Role: scribe and archivist
- Responsibilities:
  - assemble briefs for Caesar
  - turn fragmented activity into formal records
  - generate final archive summaries

#### `censor`

- Role: institutional auditor
- Responsibilities:
  - evaluate process integrity
  - track override patterns
  - surface structural governance issues
  - generate long-term improvement signals

#### `governor_*`

- Role: domain-specific governor
- Responsibilities:
  - execute work in a bounded external or domain area
  - report back to the consul and archive layer

## 7. Operating Modes

`Imperium` supports multiple operational modes. These modes are not cosmetic. They influence required states, participating roles, veto intensity, and dispatch behavior.

### 7.1 `StandardSenate`

- Default mode
- Full institutional path
- Best for complex or high-value multi-role work

Typical path:

`EdictIssued -> Triaged -> InSenate -> Debating -> VetoReview -> AwaitingCaesar -> Mandated -> Delegated -> InExecution -> UnderAudit -> Archived`

### 7.2 `DirectDecree`

- Fast execution mode
- Skips senate deliberation by intent
- Best for low-risk, well-specified, urgent, or routine work

Typical path:

`EdictIssued -> Triaged -> AwaitingCaesar -> Mandated -> Delegated -> InExecution -> UnderAudit -> Archived`

### 7.3 `TribuneLock`

- High-risk governance mode
- Requires stronger review and stricter re-entry to Caesar decision when needed
- Best for production changes, security-sensitive work, large resource commitments, and irreversible actions

Typical path:

`EdictIssued -> Triaged -> InSenate -> Debating -> VetoReview -> AwaitingCaesar -> Mandated -> Delegated -> InExecution -> UnderAudit -> AwaitingCaesar -> Archived`

### 7.4 `CampaignMode`

- Future mode for long-running multi-stage efforts
- Not required for MVP
- Intended for strategic campaigns made of multiple subordinate dockets

## 8. State Machine

The system uses a complete case-state model rather than a simple task pipeline.

### 8.1 Core States

- `EdictIssued`
- `Triaged`
- `InSenate`
- `Debating`
- `VetoReview`
- `AwaitingCaesar`
- `Mandated`
- `Delegated`
- `InExecution`
- `UnderAudit`
- `Archived`
- `Rejected`

### 8.2 Control States

- `Suspended`
- `Revoked`

### 8.3 State Definitions

#### `EdictIssued`

- An imperial command has been created by Caesar.
- The case exists but has not yet been institutionally processed.

#### `Triaged`

- `praeco` has normalized the order.
- A recommended mode, title, and summary now exist.

#### `InSenate`

- The case has formally entered senate procedure.
- Debate may not yet have started.

#### `Debating`

- Senate deliberation is active.
- Multiple senators are producing differentiated positions.

#### `VetoReview`

- `tribune` is reviewing the senate output or motion.

#### `AwaitingCaesar`

- The system requires direct user approval, rejection, override, or restriction.

#### `Mandated`

- The matter has been officially authorized for execution.

#### `Delegated`

- `consul` has broken the mandate into executable delegations.

#### `InExecution`

- Execution roles are actively delivering outcomes.

#### `UnderAudit`

- Outputs are under validation, review, archival preparation, or final challenge.

#### `Archived`

- The case is complete and entered into the institutional archive.

#### `Rejected`

- The motion has been denied through institutional review.

#### `Suspended`

- The case is paused and may later resume.

#### `Revoked`

- Caesar has formally withdrawn the case.

### 8.4 Valid Core Transitions

- `EdictIssued -> Triaged`
- `Triaged -> InSenate`
- `Triaged -> AwaitingCaesar`
- `Triaged -> Mandated`
- `InSenate -> Debating`
- `Debating -> VetoReview`
- `VetoReview -> AwaitingCaesar`
- `VetoReview -> InSenate`
- `VetoReview -> Rejected`
- `AwaitingCaesar -> Mandated`
- `AwaitingCaesar -> InSenate`
- `AwaitingCaesar -> Revoked`
- `Mandated -> Delegated`
- `Delegated -> InExecution`
- `InExecution -> UnderAudit`
- `UnderAudit -> Archived`
- `UnderAudit -> InExecution`
- `UnderAudit -> AwaitingCaesar`

### 8.5 Global Control Transitions

- any non-terminal state may transition to `Suspended`
- `Suspended` resumes to its prior active state
- any non-terminal state may transition to `Revoked` by Caesar

## 9. Authority and Permission Matrix

Permissions are defined as bounded authority, not general communication freedom.

### 9.1 Governance Rules

- intake does not directly govern execution
- senators do not directly command execution agents
- tribune does not directly execute work
- execution roles do not directly petition Caesar in normal flow
- scriba does not become a dispatcher
- censor remains supervisory rather than operational

### 9.2 Recommended Allow Lists

#### `praeco`

- may dispatch to: `senator_strategos`, `senator_juris`, `senator_fiscus`, `consul`

#### `senator_strategos`

- may dispatch to: `tribune`, `scriba`, `consul`

#### `senator_juris`

- may dispatch to: `tribune`, `scriba`, `consul`

#### `senator_fiscus`

- may dispatch to: `tribune`, `scriba`, `consul`

#### `tribune`

- may dispatch to: `scriba`, `consul`
- may return cases to senate procedure through workflow control

#### `consul`

- may dispatch to: `legatus`, `praetor`, `aedile`, `quaestor`, `scriba`, `governor_*`

#### `legatus`

- may dispatch to: `consul`, `scriba`

#### `praetor`

- may dispatch to: `consul`, `scriba`, `tribune`

#### `aedile`

- may dispatch to: `consul`, `scriba`

#### `quaestor`

- may dispatch to: `consul`, `scriba`, `tribune`

#### `scriba`

- may dispatch to: `censor`
- primarily acts as terminal summarization and archival layer

#### `censor`

- may dispatch to: `scriba`
- may surface system warnings to Caesar-facing UX

#### `governor_*`

- may dispatch to: `consul`, `scriba`

### 9.3 Special Caesar Privileges

Caesar retains product-level superuser authority:

- override tribune decisions
- reopen senate debate
- revoke active mandates
- suspend any docket
- fast-track a case through direct decree
- impose execution constraints during approval

All Caesar overrides must be recorded explicitly in the case history.

## 10. Core Product Flows

### 10.1 Standard Deliberation Flow

1. Caesar issues an edict.
2. `praeco` normalizes and classifies it.
3. The case enters senate procedure.
4. senators debate from differentiated viewpoints.
5. `tribune` reviews and may veto or return it.
6. Caesar approves, constrains, rejects, or overrides.
7. `consul` delegates execution.
8. execution roles perform work.
9. outputs move into audit.
10. `scriba` archives the completed case.

### 10.2 Direct Decree Flow

1. Caesar issues a direct order.
2. `praeco` confirms low-risk or urgent handling.
3. Caesar confirms or the system fast-tracks according to policy.
4. `consul` delegates execution.
5. the matter still enters audit before archive.

### 10.3 Veto Return Flow

1. senate produces a motion.
2. `tribune` identifies serious issue.
3. the case returns from `VetoReview` to `InSenate`.
4. senate re-deliberates under the updated objection.

### 10.4 Audit Return Flow

1. execution completes.
2. `praetor` or audit logic identifies quality or safety failure.
3. the case returns from `UnderAudit` to `InExecution`.
4. if the issue is strategic or politically sensitive, the case may instead return to `AwaitingCaesar`.

## 11. Functional Requirements

### 11.1 Edict Intake

The product must:

- allow Caesar to issue a new edict
- capture raw order text
- support explicit mode selection
- support priority selection
- generate a normalized docket title and summary through `praeco`

### 11.2 Senate Deliberation

The product must:

- support structured senate deliberation as a first-class workflow
- record separate senator positions
- identify consensus points and disputed points
- generate a recommended motion for review

### 11.3 Tribune Review

The product must:

- allow `tribune` to approve, reject, or return a motion
- capture rationale for each review outcome
- preserve veto records in the case history

### 11.4 Caesar Decision

The product must:

- present Caesar with a concise decision brief
- support approve, reject, revoke, constrain, and override actions
- store decision comments and constraints

### 11.5 Delegation and Execution

The product must:

- allow `consul` to break approved mandates into delegation items
- assign work to bounded execution roles
- track execution progress and blockers
- support multiple execution items per docket

### 11.6 Audit and Archive

The product must:

- support audit review before archive
- allow re-entry from audit to execution or Caesar review
- generate a final archive summary
- preserve all key decisions and artifacts

### 11.7 Observability

The product must:

- display current state and owner for every docket
- surface pending Caesar decisions
- expose deliberation, veto, execution, and audit history in a unified activity timeline
- show execution stalls and critical alerts

## 12. Non-Functional Requirements

### 12.1 Explainability

- The product should make political and execution reasoning legible to users.

### 12.2 Recoverability

- Active work should be retryable and resumable after interruption.

### 12.3 Auditability

- All major transitions, vetoes, overrides, and approvals must be recorded.

### 12.4 Governability

- Users must be able to pause, revoke, or override active cases according to policy.

### 12.5 Bounded Authority

- Agent-to-agent authority should be explicit and enforceable.

## 13. Information Architecture and Key Pages

### 13.1 `Caesar Desk`

Purpose:

- primary command surface
- edict issuance
- pending decision queue
- overview of critical alerts and active dockets

Key modules:

- issue new edict
- awaiting Caesar decisions
- senate currently debating
- active execution mandates
- critical alerts
- recent archives

### 13.2 `Senate Chamber`

Purpose:

- structured display of senate debate

Key modules:

- normalized proposal
- senator positions
- consensus map
- dispute map
- tribune notes
- motion summary prepared for Caesar

### 13.3 `Mandate Board`

Purpose:

- global state overview across all dockets

Key capabilities:

- state-based columns
- filtering by mode, priority, risk, current owner
- quick access to stalled or blocked items

### 13.4 `Consul Dispatch`

Purpose:

- delegation and execution coordination hub

Key capabilities:

- view approved mandates
- create and monitor delegation items
- inspect blockers and dependencies
- retry, escalate, or return work

### 13.5 `Audit and Veto`

Purpose:

- risk control, veto visibility, and institutional oversight

Key capabilities:

- high-risk docket queue
- tribune decisions
- audit exceptions
- override records

### 13.6 `Archives`

Purpose:

- final institutional memory

Key capabilities:

- browse archived dockets
- inspect final summaries and artifacts
- review decision timeline and override history

## 14. Canonical Data Model

The primary business object is a `docket`.

### 14.1 Docket Schema

```json
{
  "id": "IMP-20260329-001",
  "title": "Design authentication system",
  "state": "Debating",
  "priority": "high",
  "riskLevel": "medium",
  "currentOwner": "senate",
  "edict": {
    "raw": "Caesar original order",
    "issuer": "caesar",
    "issuedAt": "2026-03-29T10:00:00Z",
    "mode": "StandardSenate"
  },
  "summary": "Normalized brief",
  "caesarDecision": {
    "status": "pending",
    "comment": "",
    "constraints": [],
    "decidedAt": null
  },
  "senate": {
    "openedAt": "",
    "opinions": [],
    "consensus": [],
    "disputes": [],
    "recommendedMotion": ""
  },
  "tribuneReview": {
    "status": "pending",
    "reason": "",
    "notes": []
  },
  "mandate": {
    "approved": false,
    "scope": "",
    "constraints": [],
    "approvedAt": null
  },
  "delegations": [],
  "execution": {
    "progress": 0,
    "items": [],
    "blockers": []
  },
  "audit": {
    "status": "pending",
    "riskNotes": [],
    "qualityNotes": [],
    "overrideLog": []
  },
  "archive": {
    "finalSummary": "",
    "artifacts": [],
    "archivedAt": null
  },
  "activityLog": [],
  "updatedAt": ""
}
```

### 14.2 Senate Opinion Record

```json
{
  "agent": "senator_juris",
  "stance": "object",
  "summary": "Current proposal exceeds declared authority",
  "details": "Detailed rationale",
  "generatedAt": "2026-03-29T10:10:00Z"
}
```

### 14.3 Delegation Record

```json
{
  "role": "legatus",
  "objective": "Implement auth backend",
  "status": "in_progress",
  "dependsOn": [],
  "assignedAt": "2026-03-29T11:00:00Z"
}
```

### 14.4 Activity Record

```json
{
  "at": "2026-03-29T10:15:00Z",
  "kind": "debate",
  "actor": "senator_fiscus",
  "title": "Fiscal Objection",
  "summary": "The full scope is too expensive for first pass",
  "details": "Recommend staged rollout"
}
```

## 15. Activity and Audit Model

The system should unify all relevant history into a continuous timeline.

### 15.1 Activity Types

- `state`
- `debate`
- `review`
- `decision`
- `delegation`
- `execution`
- `audit`
- `archive`
- `override`
- `alert`
- `tool_result` (runtime-level optional detail)

### 15.2 Audit Priorities

The system should emphasize visibility of:

- veto use
- Caesar overrides
- returns from audit to execution
- returns from review to senate
- long-running stalls
- resource anomalies

## 16. Dispatch and Scheduling Model

The product should maintain bounded automatic dispatch while preserving institutional control.

### 16.1 Dispatch Types

- state dispatch: triggered by state transitions
- execution dispatch: triggered by consul delegation
- recovery dispatch: triggered by timeout, stall, or manual intervention

### 16.2 Scheduler Metadata

Each docket should carry scheduler metadata similar to:

```json
{
  "enabled": true,
  "lastProgressAt": "",
  "retryCount": 0,
  "escalationLevel": 0,
  "lastDispatchStatus": "success",
  "snapshot": {
    "state": "Delegated",
    "savedAt": ""
  }
}
```

### 16.3 Stall Handling

Recommended sequence:

1. retry dispatch
2. escalate to `consul`
3. escalate to `tribune` or Caesar-facing review when risk or deadlock increases
4. roll back to last stable state when recovery policy allows

## 17. API Design Direction

This PRD does not fully specify implementation, but the resource model should revolve around `dockets`.

### 17.1 Core Endpoints

- `POST /api/dockets`
- `GET /api/dockets`
- `GET /api/dockets/{id}`
- `POST /api/dockets/{id}/transition`
- `POST /api/dockets/{id}/suspend`
- `POST /api/dockets/{id}/revoke`

### 17.2 Senate Endpoints

- `POST /api/dockets/{id}/senate/open`
- `GET /api/dockets/{id}/senate`
- `POST /api/dockets/{id}/senate/redeliberate`

### 17.3 Tribune Endpoints

- `POST /api/dockets/{id}/tribune/approve`
- `POST /api/dockets/{id}/tribune/reject`
- `POST /api/dockets/{id}/tribune/return`

### 17.4 Caesar Endpoints

- `POST /api/dockets/{id}/caesar/approve`
- `POST /api/dockets/{id}/caesar/reject`
- `POST /api/dockets/{id}/caesar/override`
- `POST /api/dockets/{id}/caesar/restrict`

### 17.5 Execution Endpoints

- `POST /api/dockets/{id}/delegate`
- `POST /api/dockets/{id}/execution/progress`
- `POST /api/dockets/{id}/execution/complete`

### 17.6 Audit and Archive Endpoints

- `POST /api/dockets/{id}/audit/pass`
- `POST /api/dockets/{id}/audit/return`
- `POST /api/dockets/{id}/archive`

## 18. UX Language Guidelines

The product language should maintain the Roman governance frame without sacrificing clarity.

Recommended language:

- use `edict`, `docket`, `motion`, `mandate`, `archive`
- use `awaiting Caesar` instead of vague approval language
- use `tribune review` instead of generic compliance-only framing when appropriate
- use `senate chamber` rather than generic discussion thread

The language should remain understandable to users who do not know Roman history.

## 19. MVP Scope

### 19.1 In Scope for MVP

- full role model
- complete state model
- three operating modes: `StandardSenate`, `DirectDecree`, `TribuneLock`
- Caesar Desk
- Senate Chamber
- Mandate Board
- Consul Dispatch
- unified activity history
- tribune review
- Caesar decision flow
- audit-to-archive flow

### 19.2 Out of Scope for MVP

- full `CampaignMode`
- advanced multi-governor federation
- mobile-first experience
- complex voting mechanics inside senate
- historical simulation depth beyond the operating metaphor
- full performance scoring for all roles

## 20. Roadmap

### Phase 1: Institutional Core

- role system
- state machine
- modes
- senate chamber
- tribune review
- Caesar decision
- consul delegation
- archive flow

### Phase 2: Governance Reinforcement

- censor analytics
- richer override audit
- budget anomaly handling
- stronger execution recovery and escalation controls

### Phase 3: Expansion

- campaign mode
- governor ecosystem
- deeper knowledge archive
- external system adapters

## 21. Risks and Open Considerations

### 21.1 Risks

- Overweight process for simple work
- Weak senator differentiation causing fake deliberation
- Decorative tribune role that never truly blocks
- Excessive Caesar overrides undermining institutional value
- Execution roles leaking into decision power

### 21.2 Mitigations

- support multiple operating modes
- explicitly differentiate senator prompts and mandates
- enforce veto and return paths in workflow logic
- record and surface every override
- bind permissions tightly to workflow state

## 22. Product Acceptance Criteria for This Phase

This product definition phase is considered complete when the following are true:

- the Roman institutional metaphor is stable and internally consistent
- roles and responsibilities are explicit
- state machine is complete and non-trivial
- permission matrix separates deliberation, veto, execution, and archive layers
- operating modes define different governance intensities
- the core data model supports deliberation, decision, execution, audit, and archive
- MVP scope is clearly bounded

## 23. Final Product Statement

`Imperium` is a governed AI operating system where Caesar commands, the senate deliberates, the tribune can veto, the consul executes, the institutions deliver, and the scriba records the full history of power becoming action.
