## Better Doors

Server-side mod that adds:

- double doors
- double fence gates
- configurable door and gate knocking
- optional permission checks

## Loaders

- Fabric (`fabric` module)
- Quilt (`quilt` module)
- NeoForge (`neoforge` module)

## Target versions

- Minecraft `26.1.x` (`[26.1, 26.2)` in loader metadata)
- Java `25`

## Build

Build and collect all loader jars in one folder:

```bash
./gradlew build
```

Collected artifacts are written to:

```text
build/collected-jars
```
