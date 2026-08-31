# BlueMap CobbleFurnies Add-on

An exact-profile BlueMap 5.23 feature-backport add-on for the Poké Wool connected textures and
five static CobbleFurnies statues installed by All the Mons 1.2.0.

## Status and compatibility

Version `0.1.0-alpha.3` is an unpublished BlueMap 5.23 adapter migration
candidate for the same exact tuple:

- CobbleFurnies `1.2`, `CobbleFurnies-neoforge-1.2.jar`, 2,343,464 bytes,
  SHA-1 `451c445ff636c1e5821f13e3a3f40ee16ecb3342`, SHA-256
  `82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8`;
- Athena `4.0.6`, `athena-neoforge-1.21.1-4.0.6.jar`, 99,944 bytes,
  SHA-1 `4bcbdf388bd5e387beca7c627224aac33584b55b`, SHA-256
  `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5`;
- Minecraft `1.21.1`, NeoForge `21.1.248`, Java `21`;
- BlueMap feature backport
  `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`.

The route activates only when the installed CobbleFurnies and Athena JARs
match those exact bytes and all 132 required resources retain their locked
identities. Any mismatch leaves all blocks on BlueMap's stock path.

The released alpha.2 already compiles the four exact Athena model primitives
from `bluemap-athena-resource-models` `0.1.0-alpha.1`. Alpha.3 additionally
compiles the exact BlueMap 5.23 Adapter API pin and removes its local duplicate
helpers. Emitters, profiles, resource admission, routing, fallback, and gallery
behavior remain local and unchanged.

## Visual scope

The add-on owns exactly 37 block IDs:

- all 16 `*_poke_wool` blocks through Athena CTM;
- all 16 matching `*_poke_wool_carpet` blocks through Athena carpet CTM;
- the lower halves of `statue_ancient`, `statue_bulbasaur`,
  `statue_charmander`, `statue_pikachu`, and `statue_squirtle` as static
  bind-pose meshes compiled from the installed BBS 0.7.2 resources.

The closed color roster for both wool families is `red`, `orange`, `yellow`,
`brown`, `lime`, `green`, `cyan`, `light_blue`, `blue`, `magenta`, `purple`,
`pink`, `white`, `light_gray`, `gray`, and `black`.

The five upper statue halves and every other CobbleFurnies block stay on
BlueMap's stock renderer. The add-on does not parse animations. It emits both
winding directions for statue faces to reproduce the client's no-cull intent.

BlueMap remains the lighting host. Its block, sky, ambient, cave, and map-color
lighting can differ from Minecraft's client-side `entityCutoutNoCull`
appearance. The compatibility target is geometry, UVs, facing, bind pose,
connected-texture selection, and no-cull visibility—not pixel-identical client
lighting.

See [coverage](docs/COVERAGE.md), [architecture](docs/ARCHITECTURE.md),
[compatibility](docs/COMPATIBILITY.md), [provenance](docs/PROVENANCE.md), and
the [staging gate](docs/STAGING.md).

## Authoritative review gate

Clone with `--recurse-submodules`, or initialize an existing checkout with:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api \
  modules/bluemap-athena-resource-models
```

The settings preflight rejects an uninitialized, changed, dirty, incorrectly
pinned, or source-tree-mismatched module checkout. Then use Gradle 9.6.1,
Java 21, and the exact sibling BlueMap checkout:

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/absolute/path/to/bluemap-backport \
  -PreleaseGate=true \
  -PcobblefurniesJar=/absolute/path/CobbleFurnies-neoforge-1.2.jar \
  -PathenaJar=/absolute/path/athena-neoforge-1.21.1-4.0.6.jar \
  clean check build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyPinnedArtifacts
```

CI downloads both exact inputs to temporary storage, verifies their complete
byte/resource contract, and discards them. The production and sources JARs
contain no CobbleFurnies or Athena classes, models, textures, installed BBS
files, or derived meshes.

Tagged releases publish the production JAR, sources JAR, POM, and Gradle
module metadata under
`io.github.jan-guenter:bluemap-cobblefurnies-addon:<version>`, plus exact GitHub
Release copies and `SHA256SUMS`. A release tag must be the annotated tag
`v<addon_version>` on a reviewed main-branch commit.

## Installation and removal

Place only the reviewed add-on JAR in BlueMap's `config/bluemap/packs`
directory and restart the JVM. It is not a NeoForge mod and does not belong in
the server's `mods` directory. Remove it, restart BlueMap, and rerender the
affected area to restore stock output. The add-on writes no world or player
data.

## License and provenance

The add-on is released under the [MIT License](LICENSE). Four exact Athena CTM
model primitives are source-bundled from the owner's released MIT
`bluemap-athena-resource-models` module. The BlueMap add-on framework and
emitter retain their recorded Chipped origin; CobbleFurnies-specific
profile/data/orchestration and the bounded BBS/statue path remain local. No
exact third-party upstream source correlation is asserted. See
[THIRD_PARTY.md](THIRD_PARTY.md),
[NOTICE.md](NOTICE.md), and [docs/PROVENANCE.md](docs/PROVENANCE.md).
