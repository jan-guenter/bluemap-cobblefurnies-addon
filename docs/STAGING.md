# Staging gate

Reuse only the disposable Minecraft/BlueMap staging server and PVC. Install
the exact All the Mons 1.2.0 CobbleFurnies/Athena pair and only the candidate
add-on under review in BlueMap's packs directory. Apply the shared low-cost
staging settings from the workspace root guide before startup.

The generated fixture is locked to 46 logical cells / 71 physical blocks in
occupied AABB `208..240, 100..101, 208..236`. It contains all 32 isolated wool
and carpet colors; red wool and blue carpet open-L/full-3-by-3 witnesses; the
five south-facing, two-block statues; and five stock furniture controls. The
camera is `(221.5, 108, 254.5)`, yaw `180`, pitch `14`, with BlueMap view
payload `221.5:100:222:33.47:0:1.32946:0:0:perspective`.

Run one bounded lifecycle:

1. Install the generated datapack, run
   `function cobblefurnies_gallery:build`, then
   `function cobblefurnies_gallery:verify`. Require 71 exact block states,
   block entities on all five lower halves, no block entity `id` on any upper
   half, and zero failures.
2. Save and restart once, then rerun the verifier and require the same result.
3. Require exact profile activation with no adapter/resource/compile failure;
   purge and render only the bounded staging map.
4. Inspect isolated colors, both connected topologies, all five statues, upper
   stock behavior, and the five stock controls from the locked camera.
5. Open the exact BlueMap link in the agent browser and perform the required
   blank/black/missing/gross-breakage sanity check before presenting it.

## Current preview evidence

The owner-accepted `0.1.0-alpha.1` preview was 107,618 bytes with SHA-256
`4e4bf32380fbe19d4a7a10240f8176e24320481c71b06551bd34912dc06e66e8`.
It ran as the sole active add-on through cold restart 13 to 14, one bounded
lifecycle/update, and verification of all 71 states, five lower block entities,
and five upper non-block-entities. The required lightweight agent BlueMap
sanity check passed against the fresh 1600-by-1100 screenshot (99,313 bytes,
SHA-256 `84bb13ef0889563b8fd281bfbf5164ec7f8724731e8f969dc453f21ec110e5e6`).
Owner visual acceptance passed for that exact gallery. The
`0.1.0-alpha.2` candidate changes only source ownership and inherits the
accepted visual scope; its exact four release payloads and combined runtime
cohort still require their independent gates.

BlueMap host lighting is expected to differ from Minecraft client
`entityCutoutNoCull` lighting. Staging acceptance targets geometry, UVs,
facing, static bind pose, CTM choice, and double-winding visibility. It does
not assert pixel-identical block/sky/ambient/cave/map-color shading.

Under the workspace reuse policy, the disposable gallery world, map,
screenshots, and logs may be replaced after acceptance.
