# Gradle Bootstrap Configuration Cache Fix v0.0.1

## Symptom

The build fails with Kotlin DSL compilation errors such as:

```text
Classpath entry points to a non-existent location:
%TEMP%\wayfarer-gradle-9.6.1\extract\gradle-9.6.1\lib\...

Unresolved reference 'java-library'
```

## Cause

The original Wrapper bootstrap ran with the project-wide Configuration Cache enabled.
Gradle serialized absolute classpath entries belonging to the temporary Gradle installation.
The bootstrap script then deleted that temporary directory, leaving a reusable but invalid
project-local Configuration Cache entry.

## Apply

Copy these files over the repository root:

```text
tools/bootstrap-gradle-wrapper.ps1
tools/repair-stale-gradle-cache.ps1
```

For the repository that has already generated its Wrapper, run:

```powershell
pwsh ./tools/repair-stale-gradle-cache.ps1
```

Equivalent manual recovery:

```powershell
./gradlew.bat --stop
Remove-Item -Recurse -Force ./.gradle
./gradlew.bat --no-configuration-cache clean check
```

The corrected bootstrap:

- invokes the temporary Gradle with `--no-configuration-cache`;
- isolates `GRADLE_USER_HOME` under the temporary bootstrap directory;
- removes project-local bootstrap metadata before deleting the temporary Gradle installation;
- still verifies the Gradle distribution and Wrapper JAR SHA-256 values.
