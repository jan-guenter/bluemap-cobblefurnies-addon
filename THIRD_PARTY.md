# Third-party and first-party components

| Component | Use | Exact identity | Declared license | Bundled |
| --- | --- | --- | --- | --- |
| BlueMap | Compile/runtime host ABI | Backport `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d` | MIT | No |
| CobbleFurnies | Operator-installed JSON, BBS, and texture resources | `CobbleFurnies-neoforge-1.2.jar`, 2,343,464 bytes, SHA-1 `451c445ff636c1e5821f13e3a3f40ee16ecb3342`, SHA-256 `82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8`; CurseForge `1188698/8340192`; Modrinth `AXY1OO9m/Ynk3uYUi` | MIT, declared by the exact JAR's `META-INF/neoforge.mods.toml` | No |
| Athena | Exact installed format dependency and compatibility identity | `athena-neoforge-1.21.1-4.0.6.jar`, 99,944 bytes, SHA-1 `4bcbdf388bd5e387beca7c627224aac33584b55b`, SHA-256 `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5`; Modrinth `b1ZV3DIJ/dJgL278E` | MIT, declared by the exact JAR | No |
| BlueMap Chipped Add-on | First-party add-on activation/profile/adapter framework and Athena CTM implementation reuse | commit `c474a82b6bfd1b4173d119cb1e053a5458167e4b` | MIT | Source adapted into this project under MIT; no artifact bundled |
| JetBrains annotations | Compile-only dependency | `23.0.0` | Apache-2.0 | No |
| JUnit | Tests | `5.11.4` | EPL-2.0 | No |
| Checkstyle | Source style | `10.18.2` | LGPL-2.1-or-later | No |
| Gradle | Build tool | `9.6.1` | Apache-2.0 | No |

The packaged profile contains factual identifiers, resource paths, byte sizes,
schemas, rosters, and hashes only. It contains no third-party resource bytes.
No exact CobbleFurnies or Athena source commit is correlated to the shipped
JARs by this project.
