# Provenance

Machine-readable locks live in
`src/main/resources/bluemap-cobblefurnies/profiles/exact-artifacts.json`, the
adjacent exact profile directory, and `provenance/upstreams.json`. The latter
is copied byte-for-byte into both published JARs.

The exact CobbleFurnies input is `CobbleFurnies-neoforge-1.2.jar`, 2,343,464
bytes, SHA-1 `451c445ff636c1e5821f13e3a3f40ee16ecb3342`, SHA-256
`82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8`.
Its distribution identities are CurseForge project/file `1188698/8340192` and
Modrinth project/version `AXY1OO9m/Ynk3uYUi`. Its exact JAR metadata declares
MIT.

The exact Athena input is `athena-neoforge-1.21.1-4.0.6.jar`, 99,944 bytes,
SHA-1 `4bcbdf388bd5e387beca7c627224aac33584b55b`, SHA-256
`43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5`,
Modrinth `b1ZV3DIJ/dJgL278E`. Its exact JAR metadata declares MIT.

The generator verifies filenames, sizes, SHA-1/SHA-256/SHA-512, exact Athena
loader definitions, all five BBS schemas/hashes/counts, and the complete
132-path resource closure. Generated output contains factual identifiers,
counts, schemas, resource paths, and hashes—not upstream resource bytes.

No exact CobbleFurnies or Athena source checkout is correlated or attested.
The public CobbleFurnies repository, if consulted, is reference-only. This
project makes no reproducible-build claim for either input.

The BlueMap add-on activation/profile/adapter framework and Athena CTM
selection/emission implementation are adapted from the owner's first-party MIT
BlueMap Chipped Add-on at commit
`c474a82b6bfd1b4173d119cb1e053a5458167e4b`. The CobbleFurnies-specific
profile data/orchestration and bounded BBS interpreter/statue path were
authored for this repository. Neither published JAR contains CobbleFurnies or
Athena source, classes, models, textures, BBS files, captures, or derived
meshes.
