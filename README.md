# tes-feature

Feature backend service

## Running

### Component tests

```bash
./gradlew :tes-features-component-tests:test -Pcomponent-tests
```

### Local development

Running `tes-features-service` with Gradle:

```bash
./gradlew tes-features-service:localDev
```

Setup a run configuration to run `tes-features-service` locally with IntelliJ:

| **Setting**       | *Value**                                                  |
|-------------------|-----------------------------------------------------------|
| Module            | `tes-features:tes-features-service:test`                  |
| Application class | `dk.sunepoulsen.tes.features.service.LocalDevApplication` |
| Active profiles   | `local,tests`                                             |
