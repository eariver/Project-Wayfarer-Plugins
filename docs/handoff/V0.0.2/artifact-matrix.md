# V0.0.2 Artifact Matrix

Candidate-1 historical product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`.
Candidate-2 product-source anchor: `f2281093a03c17be0b0e69004059dd7ccb072b1c`.
Client Test Candidate-1 failed the first mandatory client scenarios and is rejected for
promotion; its evidence and staged bytes are retained unchanged as historical evidence.
Candidate-2 and Candidate-3 are historical rejected candidates. Client Test Candidate-4 is
`PREPARED_FOR_FOCUSED_CLIENT_RETEST`; its later Prepared metadata does not change the fixed
Candidate-4 product bytes.
No V0.0.2 tag or GitHub Release exists.

| Area | Current state | Release/candidate state | Gate |
|---|---|---|---|
| Core | Exact published V0.0.1 runtime reused unchanged; V001–V003 immutable | `Wayfarer_Core-V0.0.1.jar`, 11751447 bytes, `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2` | Published V0.0.1 release/checksum provenance |
| Main | Remediated source through Candidate-4 Owner Bind; `0.0.2-SNAPSHOT` scope | Candidate-4: 4690292 bytes, `c263f6957c69bf958b6374e37efbf0cff7cc0e21d27530acf7faa46cd1b54522`; Candidate-2 and Candidate-1 retained as historical | Focused client retest / later publication |
| Frontier | Remediated source through Candidate-4 bounded readiness; `0.0.2-SNAPSHOT` scope | Candidate-4: 4710866 bytes, `7897c31bdc69e05112e286235658364d2771ab875113f9410341b6d9910e1bac`; Candidate-2 and Candidate-1 retained as historical | Focused client retest / later publication |
| Package scope | Candidate scope is `main-frontier` | Local ignored candidate staging only; no release package created | Later authorization |
| Launchpad | Current-config/current-view behavior; minimal durable authority; unloaded-world expiration deferral | No release hash | FRONT-D03 adopted; FRONT-D04 accepted with limitation |
| Main/Frontier recovery | Main Owner Bind/durable delivery UX and Frontier bounded readiness/exact-current cleanup implemented | Candidate-4 fixed and prepared; focused Client Test still required | Client and Project gates |
| Waystone | Deferred/not authorized | No artifact/item/schema claim | Deferred by requirement |
| EM–MVI adapter | Prohibited/not present | No artifact | Create only after `ADAPTER_REQUIRED` |

The immutable Core V0.0.1 is not renamed or reattached as V0.0.2. Main and Frontier candidate
bytes are local ignored handoff artifacts, not a release. No tag, release, merge, or
`requirements_cleared` was created or inferred.
