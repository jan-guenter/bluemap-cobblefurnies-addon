# ADR 0001: reuse first-party add-on framework and Athena CTM without a shared runtime

Status: accepted for `0.1.0-alpha.1`.

The activation/profile/adapter framework and the 16 wool/16 carpet Athena
4.0.6 CTM semantics already exist in the owner's MIT BlueMap Chipped Add-on.
This repository adapts that first-party implementation at commit
`c474a82b6bfd1b4173d119cb1e053a5458167e4b` and records the reuse in its
packaged notices.

The CobbleFurnies-specific profile/data/orchestration and five-statue path use
a separate, bounded BBS 0.7.2 interpreter authored here. It accepts only the
locked bind-pose schema and exact model hashes, flattens group transforms, and
emits no-cull geometry. It is not a general BBS or animation library.

A shared installed runtime is rejected for this release because it would add
an unnecessary classloader/version dependency to two independently deployable
add-ons. Revisit extraction only when another proven consumer needs the same
bounded source and failure contract. No CobbleFurnies/Athena resource or
derived mesh may enter a shared module.
