# Relations

Geary treats components as entities, so each class gets a unique entity id. Relations take advantage of this by combining two ids together.

|**Definition:** Relation
|
| A relation is a component-entity pair which may store data.
| - The entity with the relation is called the `source`
| - The component part is the relation's `kind`, it defines what the relation *is*
| - The second is the relation's `target`, or which entity our relation is *with*

{.info}

For instance, we may say Alice has a relation of the kind `Friend` with Bob. Alice is the relation's source, and Bob is the relation's target.

## Use

Relations are extremely useful in many situations where we can't create unlimited components. We explore two key uses for them.

### Describing entity relations

Relations can describe a tangible relation between two entities. For example, parent/child relations, or being the instance of a prefab.

### Avoiding component reuse

We may use relations to avoid making many variations of the same component. 

For instance, if we had potion effects with a duration, instead of a `HealingDuration`, `StrengthDuration`, etc... we may use a single `Duration` component as a relation with any effect.

## Syntax

Relations may be added or set just like components:

```kotlin
sealed class Friend
sealed class Persists
data class Loves(val amount: Int)

val alice = entity()
val bob = entity()

alice.addRelation<Friend>(bob) //(1)

alice.set<String>("Hello world")
alice.addRelation<Persists, String>() //(2)

alice.setRelation(Loves(3000), bob) //(3)
```

1. A relation with kind defined by a component
2. A relation with both kind and target defined by components
3. A relation with data set

## Reading relations

### [`getRelation`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/get-relation.html) and [`hasRelation`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/has-relation.html) for reading a single relation

```kotlin
alice.hasRelation<Persists, String>() // returns true
alice.getRelation<Loves>(bob) // returns Loves(amount = 3000)
```

### [`getRelations`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/get-relations.html) for quick queries

Geary provides a function that can handle many kinds of relation queries by letting you specify a kind and target through components. Here are the rules:

- Any on kind/target <=> The kind/target may be any entity
- non nullable kind <=> The relation must hold data
- non nullable target <=> A component of the target's type must also be set on the entity

```kotlin
getRelations<Friend?, Any?>() // Gets any added/set relations with kind Friend
getRelations<Loves, Any?>() // Gets any set relations with kind Loves
```

### [`getRelationsWithData`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/get-relations-with-data.html) for extra info in queries

The queries are identical to `getRelations`, but the returned items have a lot more info in them, which looks a bit like this:

```kotlin
class RelationWithData<T, K> {
    val data: T
    val targetData: K
    val kind: GearyEntity
    val target: GearyEntity
    val relation: Relation
}
```

This is useful for certain applications but requires a bit more overhead:

```kotlin
alice.getRelationsWithData<Persists?, Any>().forEach { relWithData ->
    // assuming save takes the component type, and data to save for it
    alice.save(relWithData.target, relWithData.targetData)
}
```
