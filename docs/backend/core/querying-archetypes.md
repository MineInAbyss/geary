# Querying Archetypes

## Matching arbitrary queries
We can get entities for a query by getting all entities matching that query's family. Currently [QueryManager.getEntitiesMatching](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.systems/-query-manager/get-entities-matching.html) manages making this lookup for arbitrary queries.

It uses a map of component id to a sparse bitset (currently [roaring bitmap](https://www.roaringbitmap.org/).) The manager contains an indexed list of archetypes and the nth bit of any sparce bitset represents whether the nth archetype contains that component.

So to get all archetypes that contain component A and B, we get the bitsets for A and B and do an AND operation for all their bits. We can then somewhat efficiently iterate over all set bits, and thus over all archetypes matching our query.

What `getEntitiesMatching` does is translate a family into bitset operations that leave all matching archetypes for that family.

### Performance ideas

This is actually a relatively slow process, and it's possible to achieve speedups by knowing the size of each bitset. I've seen other ECS implementations use some form of array instead of bitsets and simply start with the smallest one to narrow down what entities should be considered. Sparse bitsets could potentially give a similar result, but I haven't explored optimizations in this area.

## Tracking registered queries

For better performance, systems will just register themselves and whenever a new archetype gets added, all registered systems will check whether it matches their query.

The memory used by this is small since the number of systems ever registered is tiny compared to number of archetypes that could potentially exist.

### Performance ideas

Currently, when a new archetype gets created we iterate over all registered systems and check which ones match. There aren't many systems, but each one doing the match check adds some time complexity.

I've thought about potentially building up a graph similar to the archetype graph, which instead holds all registered systems. You could implement rules to navigate the graph based on the query and cover all matching systems as it is being navigated.

This becomes pretty hard since operations can be nested, and there's many types, but we know from propositional logic that the `NOT` and `OR`/`AND` connectives are enough to cover a query composed of any possible connectives we could come up with. This could simplify the graph building enough that if a query could be reduced to those two connectives, the graph could be quickly traversed and all matching systems found in a fraction of the time.
