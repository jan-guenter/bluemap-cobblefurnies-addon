# Visual coverage

The closed profile owns exactly 37 CobbleFurnies block IDs:

| Route | IDs | Geometry |
| --- | ---: | --- |
| `*_poke_wool` | 16 | full-cube Athena CTM |
| `*_poke_wool_carpet` | 16 | one-sixteenth-high Athena carpet CTM |
| named statue lower halves | 5 | exact installed BBS 0.7.2 static bind pose |

The named statues are `ancient`, `bulbasaur`, `charmander`, `pikachu`, and
`squirtle`. Their upper halves remain stock. The five-model input census is 221
cubes and 2,652 source triangles; no-cull double winding produces 5,304 emitted
triangles.

Both 16-ID wool rosters use exactly these prefixes: `red`, `orange`, `yellow`,
`brown`, `lime`, `green`, `cyan`, `light_blue`, `blue`, `magenta`, `purple`,
`pink`, `white`, `light_gray`, `gray`, and `black`.

The locked gallery contains 46 logical cells / 71 physical blocks: 32 isolated
color swatches, four connected-topology witnesses, five two-block statues, and
five stock furniture controls. Red wool and blue carpet each have an open-L and
a full 3-by-3 witness. The occupied AABB is
`208..240, 100..101, 208..236`.

The owner-accepted `0.1.0-alpha.1` staging preview was 107,618 bytes with SHA-256
`4e4bf32380fbe19d4a7a10240f8176e24320481c71b06551bd34912dc06e66e8`.
The agent visual sanity check and owner visual acceptance passed. The
`0.1.0-alpha.2` candidate changes source ownership only and inherits that
bounded acceptance; the review does not cover every possible neighboring
arrangement or lighting condition.

All other furniture, all statue upper halves, animation, live block-entity
presentation, generalized BBS, and other CobbleFurnies/Athena versions are out
of scope. BlueMap host lighting divergence from the client is documented and
is not treated as geometry/UV failure.
