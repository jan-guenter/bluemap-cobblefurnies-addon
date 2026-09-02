# ADR 0002: source-bundle shared face lighting

Status: accepted for the `0.1.0-alpha.4` candidate.

## Decision

Compile `FaceLighting` from the exact gitlink-pinned
`bluemap-addon-render-core` `0.1.0-alpha.2` source tree and remove the local
duplicate. Do not install or nest the module JAR.

The CTM and statue emitters keep the same calls and `Sample` values. The
shared class changes only its package and public API visibility. Its
executable statements are unchanged. Package-normalized local and shared
sources have the same SHA-256,
`1cc6589bac47c8992b33db630baa556add781edda226e95734bda10952aae5cf`.

The settings preflight checks the committed and indexed gitlink, checkout
HEAD, source-tree hash, and clean worktree. Archive gates require exactly one
shared class and nested record class. They reject the removed local class,
every other render-core class, and nested JARs.
