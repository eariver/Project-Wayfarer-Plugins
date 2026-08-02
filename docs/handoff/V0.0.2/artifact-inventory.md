# V0.0.2 Artifact Inventory

Candidate-1 and Candidate-2 are historical rejected candidates; Candidate-3 is rejected and
preserved. Candidate-4 is fixed and prepared for focused Client retest. No V0.0.2 release is
authorized.

| Artifact | Development version | Release state | Notes |
|---|---|---|---|
| Wayfarer_Core | V0.0.1 reused | immutable existing release | product commit `49e00e21716c1c13a2dbb170fdad1b19c4275612` |
| Wayfarer_Main | 0.0.2-SNAPSHOT | Candidate-4 fixed, focused retest pending | Product HEAD `9fe86d2e787ab1f86dcf38a5abdba6168515a802`; SHA-256 `c263f6957c69bf958b6374e37efbf0cff7cc0e21d27530acf7faa46cd1b54522`; 4690292 bytes |
| Wayfarer_Frontier | 0.0.2-SNAPSHOT | Candidate-4 fixed, focused retest pending | Product HEAD `9fe86d2e787ab1f86dcf38a5abdba6168515a802`; SHA-256 `7897c31bdc69e05112e286235658364d2771ab875113f9410341b6d9910e1bac`; 4710866 bytes |

Build outputs are local/CI artifacts and are not committed. Candidate-4 filenames and hashes are
recorded in the ignored Candidate-4 manifest; focused Client gates remain required before any
publication claim.
