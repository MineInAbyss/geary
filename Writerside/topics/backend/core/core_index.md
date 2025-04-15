# Core

We use an archetypal engine, heavily inspired by flecs (especially our concept of relations.) Ensure you read the Geary guide before starting this, as I use many terms and concepts from there.

The core functionality we want from ECS is adding and removing data on entities, and quickly querying entities to work with our data.

## Archetypes

We group entities with an identical set of components into one "archetype." The reasoning is that most situations with really high entity counts have many similar entities (imagine a set of 1 million particles.) We can also memoize some expensive operations using archetypes, so they also act as a sort of cache layer.

Notice that there are downsides too, since adding and removing components on an entity requires moving a bunch of data from one archetype to another (this isn't an issue when just updating data though, which is a relief!) Generally archetypes are said to maximize iteration performance, at the cost of slower data modification.

## Querying data

The core part of writing fast queries is efficiently finding all the archetypes that match a family of components. We can cache a lot of this work for queries that run often (ex. in repeating systems) at the cost of memory usage.

Once we get all matching archetypes, we also need to efficiently read data for each entity, which traditionally comes from tightly packing it in memory (but this is difficult on the JVM.)

In the rest of this section, we'll explore the specifics of how Geary modifies entity data, and queries it.
