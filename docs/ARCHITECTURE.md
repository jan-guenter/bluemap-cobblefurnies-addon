# Architecture

This repository produces one plain BlueMap add-on JAR. It has no NeoForge
bootstrap, client renderer, packet, world state, or required configuration.

```text
BlueMap add-on entrypoint
        |
exact BlueMap 5.23 feature-backport adapter
        |
dual exact-JAR + 132-resource gate
        |
32 Athena CTM definitions + 5 exact BBS definitions
        |
synthetic dispatch for exactly 37 block IDs
        |
BlueMap tile model and host lighting
```

## Activation and routing

The process route starts inactive. During resource loading it requires the
exact CobbleFurnies 1.2 and Athena 4.0.6 byte identities, all required resource
bytes, the synthetic dispatch model, and five successfully compiled static
statues. Only after the complete profile succeeds is it published atomically.
Failure leaves the entire route inactive.

The resource extension exposes exactly 85 installed texture keys and redirects
exactly 37 physical block IDs. It does not redirect the five statue upper
halves separately: the block ID reaches the renderer, which handles only
serialized `half=lower`; `half=upper` delegates to the original stock model.
Every other CobbleFurnies block also remains stock.

## Connected textures

The wool renderer samples eight exact-state neighbors in each face plane,
selects the Athena CTM role for four quadrants, and suppresses faces against
the same block ID. Its four pure connection/face primitives are compiled from
the exact pinned `bluemap-athena-resource-models` source module. Carpet CTM
uses the installed one-sixteenth-high shape, four side faces, and connected
top/bottom surfaces. Emission, lighting, resource admission, profile data, and
orchestration remain project-owned and local.

## Static statues

At resource reload the bounded interpreter reads each installed
`models/bb/statue_*.bbs.json`, requires BBS version 0.7.2 with empty animations,
validates exact byte/count/hash limits, flattens group/cube transforms, and
caches an immutable mesh. It keeps zero extents and reversed UVs. The lower
block anchors the entire bind-pose mesh; facing rotates around the lower-block
center.

The client render type is no-cull while BlueMap's web material is front-sided,
so each source quad emits two forward and two exact reverse-winding triangles.
The locked five-model census is 221 cubes, 1,326 source quads, 2,652 source
triangles, and 5,304 emitted triangles.

## Failure and lighting boundaries

Malformed persisted state or a contained emission failure resets the complete
partial tile-model segment, restores the prior map color, and invokes stock
rendering. `MaxCapacityReachedException` is propagated unwrapped so BlueMap can
apply its own capacity policy.

BlueMap remains the lighting host. Its block/sky light, ambient and cave
shading, and map-color accumulation are not the Minecraft client's
`entityCutoutNoCull` pipeline. Geometry, UVs, facing, CTM selection, bind pose,
and no-cull visibility are matched; pixel-identical lighting is not claimed.
