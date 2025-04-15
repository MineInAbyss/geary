# Setup

Geary uses an addon system to let you load only what you need.

## Gradle setup

Add the dependencies you want, namely `geary-core` and any addons that can be found [here](https://github.com/MineInAbyss/Geary/tree/master/addons).

```kotlin
repositories {
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    val gearyVersion = "x.y.z"
    implementation("com.mineinabyss:geary-core:$gearyVersion")
    implementation("com.mineinabyss:geary-<addon-name>:$gearyVersion")
}
```

## Initialize Geary

Geary provides a helper DSL to get set up, choose an engine implementation, and install your chosen addons.

```kotlin
geary(ArchetypeEngineModule()) {

    // Install addons without configuration
    
    install(UUIDTracking)

    // Some addons provide their own DSL. It will install the addon if it's not already installed

    serialization {
        format("yml", ::YamlFormat)

        components {
            component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
        }
    }

    autoscan(classLoader, "com.mineinabyss.geary") {
        components()
    }
}.start()
// Optionally delay calling start so other parts of the code can call .configure for extra configuration
```

## Testing

Geary provides a testing module which does not start any pipeline tasks automatically. You can also extend `GearyTest` from the `geary-test` module to have an isolated world created per test.
