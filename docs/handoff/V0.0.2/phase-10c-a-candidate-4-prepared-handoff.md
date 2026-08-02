# Phase 10C-A Candidate-4 Prepared Handoff

Status: `PREPARED_WAITING_FOR_OPERATOR`

Immutable Product reference: `9fe86d2e787ab1f86dcf38a5abdba6168515a802`.
Current PR evidence was produced from merge ref
`c529d77478cf4ee8ca30bf3aefc50765622f6937`, whose event head was
`acc175f7e8768a40ec1f86a9493b64ddc0caaf0d`. The merge-ref validation passed;
the Product HEAD was not directly checked out by the existing workflows.

Normal CI `30757341843` and Headless Runtime `30757341825` completed
successfully. The Headless Artifact is ID `8836419960` with GitHub digest
`sha256:23faa83e435315a2234a8ab5fe0cb0b893e9d8b17e687d3210b2b6d154204433`.

Candidate-4 Main and Frontier were reproduced twice byte-for-byte. The
Published V0.0.1 Core authority and approved Fixture were verified separately.
The fixed local staging and worksheet paths are recorded in the readiness
record; they are ignored local evidence and contain no commit-ready JARs.

The handoff stops before Section 15. Client Test is not started. No Operator
action is requested in this handoff. If a later Owner begins, the first action
is to verify the four fixed files against the Candidate checksum file.

Runtime preflight was not executed because this repository is not authorized to
change Project Wayfarer Runtime state. The Project Runtime Owner must provide
the exact startup command and perform the fresh MariaDB／Redis／Main／Frontier
preflight before any Minecraft Client connection.
