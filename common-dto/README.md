Common DTO module

Publish to local Maven repository:

```bash
./gradlew publishToMavenLocal
```

Then in other services add `mavenLocal()` to repositories and dependency `implementation 'com.example:common-dto:0.0.1-SNAPSHOT'`.

