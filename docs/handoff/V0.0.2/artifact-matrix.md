# V0.0.2 Artifact Matrix

Candidate-1 historical product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`.
Candidate-2 product-source anchor: `f2281093a03c17be0b0e69004059dd7ccb072b1c`.
Client Test Candidate-1 failed the first mandatory client scenarios and is rejected for
promotion; its evidence and staged bytes are retained unchanged as historical evidence.
Candidate-2, Candidate-3, and Candidate-4 are historical rejected candidates. Candidate-5
Product remediation is `PRODUCT_PASS` and is `PREPARED_FOR_FOCUSED_CLIENT_RETEST`; its later
status metadata does not change the fixed Candidate-5 product bytes.
No V0.0.2 tag or GitHub Release exists.

| Area | Current state | Release/candidate state | Gate |
|---|---|---|---|
| Core | Exact published V0.0.1 runtime reused unchanged; V001–V003 immutable | `Wayfarer_Core-V0.0.1.jar`, 11751447 bytes, `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2` | Published V0.0.1 release/checksum provenance |
| Main | Remediated source through Candidate-5 Owner Bind/action guard; `0.0.2-SNAPSHOT` scope | Candidate-5: 4690577 bytes, `391ea0b1beae8ff4e7ed1e8428179ff5b5166ff85fdd1c67d0fdff6062b82079`; Candidate-1 through Candidate-4 retained as historical | Focused client retest / later publication |
| Frontier | Remediated source through Candidate-5 bounded readiness; `0.0.2-SNAPSHOT` scope | Candidate-5: 4713179 bytes, `dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8`; Candidate-1 through Candidate-4 retained as historical | Focused client retest / later publication |
| Package scope | Candidate scope is `main-frontier` | Local ignored candidate staging only; no release package created | Later authorization |
| Launchpad | Current-config/current-view behavior; minimal durable authority; unloaded-world expiration deferral | No release hash | FRONT-D03 adopted; FRONT-D04 accepted with limitation |
| Main/Frontier recovery | Main Owner Bind/durable delivery UX and Frontier bounded readiness/exact-current cleanup implemented | Candidate-5 fixed and prepared; focused Client Test still required | Client and Project gates |
| Waystone | Deferred/not authorized | No artifact/item/schema claim | Deferred by requirement |
| EM–MVI adapter | Prohibited/not present | No artifact | Create only after `ADAPTER_REQUIRED` |

The immutable Core V0.0.1 is not renamed or reattached as V0.0.2. Main and Frontier candidate
bytes are local ignored handoff artifacts, not a release. No tag, release, merge, or
`requirements_cleared` was created or inferred.
