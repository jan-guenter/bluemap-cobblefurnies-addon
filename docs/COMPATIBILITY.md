# Compatibility

Compatibility is exact and evidence-locked.

| Component | Accepted identity |
| --- | --- |
| All the Mons | `1.2.0`, repository commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft / NeoForge / Java | `1.21.1` / `21.1.248` / `21` |
| BlueMap | `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` |
| CobbleFurnies | `CobbleFurnies-neoforge-1.2.jar`, 2,343,464 bytes, SHA-1 `451c445ff636c1e5821f13e3a3f40ee16ecb3342`, SHA-256 `82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8` |
| Athena | `athena-neoforge-1.21.1-4.0.6.jar`, 99,944 bytes, SHA-1 `4bcbdf388bd5e387beca7c627224aac33584b55b`, SHA-256 `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5` |

Both artifacts and their 132-resource active closure are mandatory. This is
not a compatibility claim for another artifact, a later pack, general Athena
CTM, animated BBS, other statues/furniture, or a different BlueMap host ABI.

Installed resource pixels are read at runtime, but the locked resource-byte
gate intentionally rejects replacements for this alpha profile. A changed
texture/model pack therefore needs its own evidence and review rather than
silently reusing the route.

BlueMap host lighting can diverge from Minecraft client lighting. In
particular, BlueMap's block/sky/ambient/cave/map-color pipeline is not
`entityCutoutNoCull`; this profile guarantees the audited geometry, UV,
connected-texture, facing, static bind-pose, and double-winding visibility
behavior only.
