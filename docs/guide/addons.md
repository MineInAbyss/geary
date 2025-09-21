# Addons

Geary has a simple addon system inspired by Ktor that lets you write addons that other code can configure, dsl blocks like `serialization` and `autoscan` use the addon system under the hood.

## Installing an addon

Addons can be installed within the root `geary` configuration or an addon's configuration block, as shown later.

```kotlin
geary(ArchetypeEngineModule()) {
    install(MyAddon)
}
```

## Creating a non-configurable addon

You may use addons to set up your engine, in this case it's unlikely any other part of code will need to further configure your addon, so we may use a Unit for our configuration, or omit the block entirely.

```kotlin
val MyAddon = createAddon<Unit>("My addon", configuration = {
    // this block will run immediately, use it to ensure other addons are loaded
    install(AnotherAddon)
    serialization { ... }
}) {
    // this block will run after all addons are configured and geary.start() is called
    // it provides some helper blocks to run your code on different load phases, shown below in their load order
    components {
        // initialize any components you need, ex. autoscan uses this block
    }
    
    systems {
        // Create your systems here, these should exist before any entities are created, though can be registered later too
    }
    
    entities {
        // Load any entities you need, ex. prefabs uses this to load entiteis from files
    }
    
    onStart {
        // All other phases have finished and your addon can run its startup tasks
    }
}
```

## Configurable addons

You may use the `configuration` block to provide a configuration class for your addon, which can be used by other addons, and inside your init block.

```kotlin

class MyConfigurableAddon {
    val printStrings: MutableList<String> = mutableListOf()
    
    fun add(text: String) = printStrings.add(text)
}

val MyAddon = createAddon("My addon", configuration = {
    MyConfigurableAddon()
}) {
    onStart {
        configuation.printStrings.forEach { println(it) }
    }
}
```

Now other plugins may do:

```kotlin
install(MyAddon) {
    add("A string!")
}
```

## Configurable addon with builders

Finally, you may split your addon into a mutable builder for the configuration phase and a built addon for init. This is useful if other addons need to access your addon at runtime, but not modify its data:

```kotlin

class MyBuilder {
    fun build(): MyBuiltConfig = ...
}

class MyBuiltConfig { ... }

val MyAddon = createAddon("My addon", configuration = {
    MyBuilder()
}) {
    val built = configuration.build()
    
    onStart {
        built.doSomething()
    }
    
    built
}
```

Now other addons can use your built class after addons are initialized, or during their own initialization if they are loaded after your addon (ex. by calling `install` in their configuration block.)

```kotlin
val built: MyBuiltConfig = geary.getAddon(MyAddon)
```
