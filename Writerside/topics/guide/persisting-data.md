# Persisting data

- Geary works closely with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) to allow components to be encoded, stored, and persisted.
- Serializable components can use `setPersisting`.
- Use autoscanner to register serializable components for you.

# Uses

For Minecraft, we persist using the binary format CBOR, as well as YAML for prefabs defined in config files.

Use `entity.setPersisting` with a serializable component to set and persist it.

# Making your data serializable

Components don't need to be serializable, but you should always try to make yours serializable.

Be sure to read more about ktx.serialization, but here's a quick explanation to get you started.

- Annotate a class as `@Serializable` and `@SerialName("yournamespace:component_name")`.
- Properties inside will be serialized if they all have a serializer.
- Use `@Transient` to avoid serializing a property.

## Polymorphic serialization

Since components can be Any object, when you want to serialize any component, use `@Polymorphic GearyComponent`, ex:

```kotlin
@Serializable
@SerialName(...)
class SomeData(
    val components: List<@Polymorphic GearyComponent>
)
```

# Autoscanning

Within the Geary extension DSL, you can ask to autoscan and register components. This will allow them to be used with any of the formats stored in the `Formats` singleton.

The autoscanner will look through classes at runtime and automatically register all components annotated with `@AutoscanComponent`.
