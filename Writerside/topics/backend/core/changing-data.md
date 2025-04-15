# Changing data

There are two general ways to modify data on an entity:

- Arbitrary operation with a known entity reference (we often do this when an external system like Minecraft changes state and this state needs to be propagated to our engine)
- Iteration, where we don't care about specific entities, only families they match.

Let's explore how we handle these two situations.

## Entity references

All entity references are a 64-bit ID. When we modify data on an entity, we first get the entity's Record (its archetype and index within the archetype), then update data within the archetype or move it to another archetype (ex when adding/removing components.)

Currently, this is done in the [TypeMap](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes.maps/-type-map/index.html) class, which uses a hashmap under the hood, but an array is probably better since we reuse entity IDs after deletion anyway. I think flecs mentions a hashmap because they append a few "version" bits when reusing an id to avoid problems when holding onto an entity reference, but we don't currently do this since we hold long term references through a `UUID` component + `UUID -> entity id` hashmap anyways.

Naturally this introduces some indirection and a lot of jumping around in memory, but these actions don't run as often as system iteration and so the indirection should be acceptable.

## Iteration

When iterating through many entities, we avoid jumping through the reference using `accessors`. These help us construct a system's family while also acting as property delegates that let us access data during iteration. Under the hood, they build instructions for how a system should access data while iterating over each archetype.

The process looks something like this: A system knows all archetypes that match its family, so iterating through them is pretty simple. For each archetype, it memoizes indices for the components it'll access. Knowing these, accessing a component for the nth entity in the archetype requires us to access the nth component in each array we already have access to.

A core speedup of ecs comes from keeping these data arrays tightly packed, which we'll eventually be able to do in java (see [Future plans for memory structure](memory-structure.md)). What still confuses me is how we avoid cache misses when accessing multiple components on one archetype. We're accessing the same index on each array, but these are all spaced out in memory. We could load multiple sections of each tightly packed array to fill up our cache and jump to the next bunch of components once we've gone through the cache, but I have no clue how to approach this on the JVM.

Still, system iteration lets us avoid a few layers of indirection even if we're not hitting theoretical max performance.
