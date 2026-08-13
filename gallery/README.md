# CobbleFurnies staging gallery

This deterministic datapack reproduces the locked stock fixture exactly:
46 logical cells and 71 CobbleFurnies blocks inside occupied AABB
`208..240, 100..101, 208..236`:

- 16 isolated poke-wool colors and 16 matching carpets;
- red wool and blue carpet open-L plus full 3-by-3 witnesses;
- five lower-anchor, static bind-pose statues with their stock upper halves;
- five stock controls: `oak_chair`, `oak_table`, `red_sofa`,
  `red_cabinetry`, and `poke_ball_desk`.

All five statues face south. The exact camera is `(221.5, 108, 254.5)`, yaw
`180`, pitch `14`; the corresponding BlueMap view payload is
`221.5:100:222:33.47:0:1.32946:0:0:perspective`.

The statue meshes may visually extend above the occupied AABB. The datapack
contains only block IDs, states, coordinates, commands, and metadata; it does
not contain CobbleFurnies or Athena assets.

Validate and package it with:

```text
python3 gallery/generate.py --check
gallery/package.sh /tmp/cobblefurnies-gallery.zip
```

On the disposable ATM 1.2.0 staging server, run:

```text
/function cobblefurnies_gallery:build
/function cobblefurnies_gallery:verify
/function cobblefurnies_gallery:pose
/function cobblefurnies_gallery:release
```

`verify` checks every generated placement and records mismatches in
`#failures cobblefurnies_gallery`. Visual acceptance still requires a fresh
BlueMap render; command verification is only a placement census.
