# Copilot Instructions for the OSGi Specification Repository

## Build System

This is a **Bnd Workspace** built with Gradle 8.14 and the bnd plugin (7.1.0).
Individual subprojects have no `build.gradle` — all project configuration lives in `bnd.bnd` files and shared includes from `cnf/includes/`.

### Key Commands

```bash
# Full build + publish artifacts to cnf/generated/repo
./gradlew :build :publish

# Build specifications (HTML, PDF, ZIP)
./gradlew :osgi.specs:specifications
./gradlew :osgi.specs:core.pdf
./gradlew :osgi.specs:cmpn.pdf

# Run a single TCK test project
./gradlew :org.osgi.test.cases.cm:testOSGi

# Show Java version and bnd config
./gradlew :buildscriptDependencies
```

Gradle parallel builds are supported.
JVM args: `-Xms1g -Xmx3g` (set in `gradle.properties`).

## Architecture

The repository contains ~190 subprojects organized by role:

| Prefix | Role | Example |
|--------|------|---------|
| `org.osgi.service.*` | Service specification APIs (interfaces, constants, DTOs) | `org.osgi.service.cm` |
| `org.osgi.impl.service.*` | Reference implementations of service specs | `org.osgi.impl.service.async` |
| `org.osgi.test.cases.*` | TCK (Test Compatibility Kit) compliance tests | `org.osgi.test.cases.cm` |
| `org.osgi.annotation.*` | Compile-time annotations processed by bnd | `org.osgi.annotation.versioning` |
| `org.osgi.namespace.*` | OSGi namespace definitions | `org.osgi.namespace.service` |
| `org.osgi.util.*` | Utility libraries (tracker, promise, converter) | `org.osgi.util.promise` |
| `org.osgi.framework` | Core framework API | |
| `org.osgi.dto` | Base DTO class and core DTOs | |

### Key Directories

- **`cnf/`** — Bnd workspace configuration hub.
  `cnf/build.bnd` is the master config (compiler, repos, versions, plugins).
  `cnf/includes/` has reusable bnd fragments (`tck.bnd`, `companion.bnd`, `core.bnd`, `cmpn.bnd`, `jdt.bnd`).
- **`osgi.build/`** — Aggregator project; `:build` and `:publish` tasks live here.
- **`osgi.specs/`** — DocBook source and tooling for generating HTML/PDF specifications.
- **`osgi.tck/`** — Packages individual test.cases projects into distributable TCK bundles.
- **`templates/`** — Starter templates for new service specs, implementations, and TCK projects.
- **`licensed/`** — Third-party tools (Apache FOP, DocBook XSL) used by spec generation.

### How Subprojects Are Configured

Every subproject has a `bnd.bnd` that typically starts with includes:

```bnd
-include: ${includes}/jdt.bnd, ${includes}/companion.bnd, ${includes}/cmpn.bnd
Export-Package: ${p}.*; -split-package:=first
-buildpath: \
    ${osgi.annotation.buildpath},\
    org.osgi.framework;maven-scope=provided;version=1.8
```

The `${includes}` macro resolves to `cnf/includes/`.
The `${p}` macro resolves to the project name (which matches the main package).

## Code Conventions

### Java Version and Compiler

Source and target: **Java 1.8**.
All compilations use `-Xlint:unchecked`.

### File Headers

Every file requires an Apache License 2.0 header with SPDX identifier:

```java
/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * ...
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
```

### Formatting

- Indentation: **tabs** (tab size 4)
- Line endings: **LF**
- Trailing whitespace: trimmed
- Final newline: required
- Formatting is enforced by Eclipse JDT settings imported via `cnf/includes/jdt.bnd`

### Versioning Annotations

Interfaces in service specs must be annotated with semantic versioning markers from `org.osgi.annotation.versioning`:

- **`@ProviderType`** — on interfaces implemented by the spec provider (breaking changes require major version bump)
- **`@ConsumerType`** — on interfaces implemented by consumers (additions only require minor bump)

Package versions are declared in `package-info.java` using `@Version` referencing a constant:

```java
@Version(CONFIGURATION_ADMIN_SPECIFICATION_VERSION + ".1")
package org.osgi.service.cm;

import static org.osgi.service.cm.ConfigurationConstants.CONFIGURATION_ADMIN_SPECIFICATION_VERSION;
import org.osgi.annotation.versioning.Version;
```

### DTO Pattern

DTOs extend the `org.osgi.dto.DTO` base class.
Fields are **all public** with no getters/setters and no business methods.
DTOs are annotated `@NotThreadSafe`.

```java
public class BundleStartLevelDTO extends DTO {
    public long   bundle;
    public int    startLevel;
    public boolean activationPolicyUsed;
}
```

### Custom Annotations

OSGi annotations use `@Retention(RetentionPolicy.CLASS)` — they are processed by bnd at build time, not available at runtime.
Key annotations: `@Capability`, `@Requirement`, `@Export`, `@Header` (from `org.osgi.annotation.bundle`).

### Testing

- Framework: **JUnit Platform** (JUnit 5), with support for JUnit 3/4 test cases.
- Test runner: `biz.aQute.tester.junit-platform`
- TCK tests are run inside an OSGi framework via `testOSGi` Gradle task.
- Test projects embed sub-bundles (defined in `bnd/` subdirectories) via `-includeresource` in `bnd.bnd`.
- Shared test utilities live in `org.osgi.test.support`.

### Thread Safety

Document thread safety on interfaces using `@ThreadSafe` or `@NotThreadSafe` in Javadoc.

## Contribution Workflow

- **Git workflow**: triangular — clone origin, push to your fork, PRs against origin.
- **Branch naming**: `issues/XXX` for bugs, `requirements/XXX` for requirement discussions, `design/XXX` for design proposals.
- **Commits**: must be signed off (`-s` flag) and use an ECA-registered email.
- **License**: all contributions under Apache License 2.0.

## CI

GitHub Actions (`.github/workflows/cibuild.yml`) runs:
1. **build** — `./gradlew :buildscriptDependencies :build` on Java 17
2. **spec** — builds HTML/PDF specifications
3. **tck** — runs TCK tests in matrix (parallelized from `osgi.tck`)
4. **gh\_pages** — publishes specs to GitHub Pages (main branch only)
