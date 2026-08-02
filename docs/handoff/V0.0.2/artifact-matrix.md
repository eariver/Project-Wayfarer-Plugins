# V0.0.2 Artifact Matrix

Candidate-1 historical product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`.
Candidate-2 product-source anchor: `f2281093a03c17be0b0e69004059dd7ccb072b1c`.
Client Test Candidate-1 failed the first mandatory client scenarios and is rejected for
promotion; its evidence and staged bytes are retained unchanged as historical evidence.
Client Test Candidate-2 is `PREPARED_FOR_FOCUSED_CLIENT_RETEST`. Its metadata is a later
documentation-only commit and does not change the Candidate-2 product bytes.
No V0.0.2 tag or GitHub Release exists.

| Area | Current state | Release/candidate state | Gate |
|---|---|---|---|
| Core | Exact published V0.0.1 runtime reused unchanged; V001–V003 immutable | `Wayfarer_Core-V0.0.1.jar`, 11751447 bytes, `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2` | Published V0.0.1 release/checksum provenance |
| Main | Remediated source through V004; `0.0.2-SNAPSHOT` scope | Candidate-2: `wayfarer-main-0.0.2-SNAPSHOT.jar`, 4678511 bytes, `5b40dd4b66ab5fd15b9b89f30e5db09923759171e1f69429ab3ff669120ab36b`; Candidate-1 historical: `730d56888001e9c76bd127b25c118a937f03a5dd95a0fa381c8c38fec2517113` | Focused client retest / later publication |
| Frontier | Remediated source migration level; `0.0.2-SNAPSHOT` scope | Candidate-2: `wayfarer-frontier-0.0.2-SNAPSHOT.jar`, 4700734 bytes, `1559af0ebebb664a4f29dd08df41228fc9dfd9df1930da469b877075d829033d`; Candidate-1 historical: `f43829c7b6e06ea44549ffdd1ef26a567aef1563ba73a0808c47634742e9d3ec` | Focused client retest / later publication |
| Package scope | Candidate scope is `main-frontier` | Local ignored candidate staging only; no release package created | Later authorization |
| Launchpad | Current-config/current-view behavior; minimal durable authority; unloaded-world expiration deferral | No release hash | FRONT-D03 adopted; FRONT-D04 accepted with limitation |
| Main/Frontier recovery | Main stage-specific delivery diagnostics and Frontier MVI-aware readiness/self-heal remediation implemented | Candidate-2 prepared; focused Client Test still required | Client and Project gates |
| Waystone | Deferred/not authorized | No artifact/item/schema claim | Deferred by requirement |
| EM–MVI adapter | Prohibited/not present | No artifact | Create only after `ADAPTER_REQUIRED` |

The immutable Core V0.0.1 is not renamed or reattached as V0.0.2. Main and Frontier candidate
bytes are local ignored handoff artifacts, not a release. No tag, release, merge, or
`requirements_cleared` was created or inferred.
