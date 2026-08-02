# V0.0.2 Artifact Matrix

Candidate product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`.
Client Test Candidate: `V0.0.2-Client-Candidate-1` (`FIXED`).
The candidate-fixation metadata HEAD is a later documentation-only commit and is recorded in the
Phase 09A result report; it does not change the candidate product-source anchor or staged bytes.
No V0.0.2 tag or GitHub Release exists.

| Area | Current state | Release/candidate state | Gate |
|---|---|---|---|
| Core | Exact published V0.0.1 runtime reused unchanged; V001–V003 immutable | `Wayfarer_Core-V0.0.1.jar`, 11751447 bytes, `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2` | Published V0.0.1 release/checksum provenance |
| Main | Current source through V004; `0.0.2-SNAPSHOT` scope | `wayfarer-main-0.0.2-SNAPSHOT.jar`, 4671368 bytes, `730d56888001e9c76bd127b25c118a937f03a5dd95a0fa381c8c38fec2517113` | Client candidate / later publication |
| Frontier | Current source migration level; `0.0.2-SNAPSHOT` scope | `wayfarer-frontier-0.0.2-SNAPSHOT.jar`, 4682233 bytes, `f43829c7b6e06ea44549ffdd1ef26a567aef1563ba73a0808c47634742e9d3ec` | External client checks / later publication |
| Package scope | Candidate scope is `main-frontier` | Local ignored candidate staging only; no release package created | Later authorization |
| Launchpad | Current-config/current-view behavior; minimal durable authority; unloaded-world expiration deferral | No release hash | FRONT-D03 adopted; FRONT-D04 accepted with limitation |
| Main/Frontier recovery | Main paid reissue and Frontier durable redelivery implemented | Candidate bytes fixed; Client Test still required | Client and Project gates |
| Waystone | Deferred/not authorized | No artifact/item/schema claim | Deferred by requirement |
| EM–MVI adapter | Prohibited/not present | No artifact | Create only after `ADAPTER_REQUIRED` |

The immutable Core V0.0.1 is not renamed or reattached as V0.0.2. Main and Frontier candidate
bytes are local ignored handoff artifacts, not a release. No tag, release, merge, or
`requirements_cleared` was created or inferred.
