# Release procedure

Pull-request CI reacquires and verifies the exact CobbleFurnies/Athena inputs,
checks generated profile/gallery files and provenance, runs Python/Java/style
tests, builds reproducible archives, verifies package boundaries, and generates
the exact POM/module metadata.

Before tagging:

1. Require a reviewed PR that changes `addon_version` to the intended higher
   version and passes CI.
2. Confirm the exact commit, version, changelog, provenance, four publication
   payloads, and clean worktree. Initialize the pinned toolkit with
   `git submodule update --init --recursive -- tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api modules/bluemap-athena-resource-models modules/bluemap-addon-render-core`;
   the settings trust preflight must pass without changing any gitlink.
3. Complete the disposable staging lifecycle in [STAGING.md](STAGING.md) and
   obtain owner visual acceptance.
4. Merge to `main` and create annotated tag `v<addon_version>` on that exact
   reviewed commit.

The tag workflow fails unless the tag is annotated, equals the version, points
at an ancestor of `main`, and its commit is associated with a merged PR to
`main` that changed `gradle.properties` to the tagged `addon_version`.

Publication is resume-safe and immutable. Before mutation the workflow requires
the Maven version to be wholly absent or wholly present with exact bytes,
SHA-1/MD5 sidecars, and one metadata entry; partial state fails closed. It then
verifies the exact five GitHub assets, attests both JARs, publishes Maven only
if absent, reverifies remote bytes, and publishes alpha versions as
prereleases. It never deploys to a server and never replaces published bytes.
