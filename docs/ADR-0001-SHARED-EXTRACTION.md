# ADR 0001: reuse first-party add-on framework and Athena CTM without a shared runtime

Status: superseded for the four pure model primitives in `0.1.0-alpha.2`.

The activation/profile/adapter framework and the 16 wool/16 carpet Athena
4.0.6 CTM semantics already exist in the owner's MIT BlueMap Chipped Add-on.
This repository adapts that first-party implementation at commit
`c474a82b6bfd1b4173d119cb1e053a5458167e4b` and records the reuse in its
packaged notices.

The CobbleFurnies-specific profile/data/orchestration and five-statue path use
a separate, bounded BBS 0.7.2 interpreter authored here. It accepts only the
locked bind-pose schema and exact model hashes, flattens group transforms, and
emits no-cull geometry. It is not a general BBS or animation library.

A shared installed runtime remains rejected because it would add a
classloader/version dependency to independently deployable add-ons. Four
proven consumers now need the same pure `CtmTextureRole`, `CtmSelector`,
`CtmConnections`, and `CubeFace` sources, so `0.1.0-alpha.2` compiles those
classes from an exact commit-pinned source submodule. The module has no
entrypoint or installed runtime. Emitters, lighting, profiles, resource
admission, fallback, and all CobbleFurnies/Athena resources remain outside it.
