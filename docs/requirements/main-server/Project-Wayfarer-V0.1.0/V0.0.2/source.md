# V0.0.2 Requirement Source

## Received artifact

- Original filename:
  `Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED.md`
- Repository snapshot:
  `Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements.md`
- SHA-256:
  `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`
- Received: 2026-07-30
- Plugin pre-execution HEAD:
  `874c2268da5a94f024b8c4532f409d8698b85a2f`
- Plugin work base (`origin/main`):
  `efe9d81029a10ce9ca0ce01f9c6770a4991784bc`
- Project reference HEAD:
  `344eedc738d75954daa43facfeef302944f2963a`
- V0.0.1 annotated tag object:
  `e3fc76dde615809e6dd9b0f8900122232d178f37`
- V0.0.1 product source:
  `49e00e21716c1c13a2dbb170fdad1b19c4275612`
- V0.0.1 Core artifact:
  `Wayfarer_Core-V0.0.1.jar`
- V0.0.1 Core SHA-256:
  `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2`

The snapshot is an exact byte-for-byte copy of the received requirement. It must not be edited to
make implementation or evidence more convenient.

## Authority order

1. Latest repository record reflecting an explicit Project Owner decision.
2. Current Project Wayfarer formal docs and Runtime Lock.
3. Current Server and Theme concepts.
4. Current Plugin concepts.
5. V0.0.1 Stable contracts, ADRs, migrations, and release evidence.
6. The V0.0.2 requirement snapshot.
7. Existing Plugin Repository design specification.
8. The V0.0.2 long-running execution instruction.
9. Local implementation decisions that preserve every higher boundary.

The final execution instruction explicitly defers Waystone production for V0.0.2. That scoped
instruction overrides older Project documents that list Waystone in the broader V0.1.0 target.

## Project Repository references

All paths below were read from immutable Project commit
`344eedc738d75954daa43facfeef302944f2963a`.

| Path | Git blob |
|---|---|
| `docs/06-acceptance-tests.md` | `d838eedfb0f3411746a9b99a2f0c63943fa76efd` |
| `docs/09-roadmap.md` | `8481727c6b2e339785f3f652c00048fa4443bb52` |
| `docs/10-waymark-economy.md` | `b25b462a7056feb4113066603d50f8a3035d6b2e` |
| `docs/12-permission-model.md` | `a486c0cfc625c0f420854fbc36aa25f0079d5376` |
| `docs/14-frontier-v0.1.0-scope.md` | `8cb28384fa0c8a65f665ba65fda3a8b64e96ac9d` |
| `docs/15-frontier-runtime-lock.md` | `e78abf0f5cc4efc2ba64ac2397c6e109c956e2ec` |
| `versions.yml` | `e7c9b0c86ee3428fede25c18a48437e80ccb624a` |
| `plugin-manifest.yml` | `ec073a00d3b8601bce8591d55e4d314c459c0c0c` |
| `concepts/plugins/Project_Wayfarer_Plugin_Concept_v0.0.3.md` | `f5ec0698659f9db2617b79f0d80fc09ce5756dad` |
| `concepts/plugins/main/Project_Wayfarer_Growth_Tool_Concept_v0.0.5.md` | `d8d129f67b4c1104e0085a0e71fd93dd154b5209` |
| `concepts/plugins/frontier/Project_Wayfarer_Worlds_Beyond_Plugin_Concept_v0.0.4.md` | `8caca1d90dac1db228146bf19fc330ed539785b4` |
| `concepts/plugins/frontier/Project_Wayfarer_Ruined_Frontier_Integration_Decision_Concept_v0.0.2.md` | `9d6363c508416fd01f905d9224ac4d853bc6f99d` |
| `concepts/frontier/Frontier_Server_Specification_V0.0.5.md` | `38d503c8f37bf6a33976bd2ca56f1024d99710f2` |
| `concepts/frontier/Worlds_Beyond_Specification_V0.0.6.md` | `22961a2c9d32c76377fb96ae144193bce0793b00` |
| `concepts/frontier/Ruined_Frontier_Specification_V0.0.5.md` | `7dc61b0cb730ed51946cac8a0e12a42a49ca4f3a` |

The Project Repository was read-only during this task.

## V0.0.1 immutable migration baseline

| Migration | Git blob | SHA-256 |
|---|---|---|
| Core V001 | `878e9b14ddf6a39b0a4b7b7a35e303adc2893c84` | `59035D3BF0EE9F11E2A6756138FA55F331DC79546778C473BACBDE887A894840` |
| Core V002 | `3a8ce1c4df384497d335493f9117020b678dea5b` | `B45709E7740A4E720B05C26ED02E66E965784980898EAB29445B1293B422F260` |
| Core V003 | `f4f0b195318fa79518f331c0f519272c96c40ff3` | `83483E0494B687EC4FF11AF7872AE89E1406BB678AE2B000897FC66FF7A048B4` |

## Module migration baselines fixed during V0.0.2

| Migration | SHA-256 |
|---|---|
| Main V001 | `845738E92E7629F53F088C73CCDF6165E4A87FAC4C50EFB1690A365E8E7B1668` |
| Main V002 | `695F2169556925C8CE3CFD256352F5CC4B70C3AFD5D393AEC80095BF44B05B87` |
| Main V003 | `90935B57C0F4485675B064E393448ADE95538E66FB7800D97AF086526C061021` |
| Frontier V001 | `E63A9A3D2C6DAAC52E0E7A43374755A8D11908AD4DA1494F711A8D1A1EB182D1` |
| Frontier V002 | `D3656098FD22C1E93BE8722C2F35B0F811E34796EE70BA196D7A1F1CEB31ACF7` |

The V001 files were inherited unchanged from `origin/main`. V002 and Main V003 are additive
drafts in this branch. Isolated MariaDB tests cover empty schema, prior-version upgrade, repeat
validation, failure, and Core/Main/Frontier location boundaries. ADR 0009 fixes module-local
runtime ownership; Project deployment remains unauthorized.

## Phase 10C-A Revision B provenance and amendment anchor

The received Phase 10B-C Candidate-3 input set is preserved under the new work root and is
treated as historical evidence, not as a mutable source of product authority:

| Input | Immutable identity |
|---|---|
| Formal instruction used | `.ai-work/luna-gpt-5.6-v002/add_instructions/Luna_Max_Phase_10C_A_Candidate_4_Owner_Bind_and_Readiness_Instructions_Revision_B.md` (the requested v003 instruction file was not present in the repository at start), SHA-256 `B40CF5ED90CD1DEC5FFB82F8371D94977581E3B0A8BC313EE3000535283BCF2E` |
| Independent review | `.ai-work/luna-gpt-5.6-v002/add_instructions/reference/PHASE_10B_C_CANDIDATE_3_INDEPENDENT_REVIEW_REVISION_B.md`, SHA-256 `E661025C9AB27B3110DD094D94FD99869A26CA87CAD6BBA711B08A444F28216B` |
| Candidate-3 product source | `25200ad4745fdaae79c761a56083f6e648e9a06b` |
| Candidate-3 Core artifact | SHA-256 `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2` |
| Candidate-3 submission | ZIP SHA-256 `293f59881f244a565bf9385ea2d6b390ae4d86f41c9d4886758e2406990c13dc` |

The Owner Amendment for Phase 10C-A Revision B is tracked in the decision register and
traceability/client-acceptance documents. It does not rewrite the immutable requirement
snapshot, migration history, Core artifact, or Project Runtime authority. Candidate-3 failure
timing remains explicitly unresolved where poll-by-poll evidence was absent.
