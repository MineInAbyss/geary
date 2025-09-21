# Events

## Intro

I originally designed Geary for our needs in Minecraft. There are many APIs that let developers extend upon the Minecraft server (we use Bukkit for Mine in Abyss), but a lot are event driven (i.e. the correct way to add custom behaviour is usually to intercept an event and customize the outcome.)

What interested me is that usually the way an event gets modified is just by editing the data on it (ex. marking it as cancelled, changing something on a player involved in the event, or changing the strength of an attack.)

So, this inspired events as entities in Geary. An event is just a bundle of data, so to fire an event we create a new entity, add data to it, and have a mechanism to find all systems matching this entity.

## Current implementation

The whole system is unoptimized as hell, but I think there's something to the idea of events that can have arbitrary information added onto them, and to have listeners be built the same way as repeating systems.

The key point where this needs to be fast is finding all matching systems (this is the inverse problem of finding all matching entities and I talk about possible ideas for speedups in [Querying archetypes](querying-archetypes.md#performance-ideas-1)). This information is rather hard to cache, and there's a lot of it to cache, but that's the approach currently.

We hardcode three types of involvement in an event: an entity is the source, target, or the event itself. When an archetype can act as the event, we track a list of potential listeners for it, (similarly there are lists for when our entity can be a target or source) then when we fire the event, we get an intersection of all the listeners for involved entities. Notice that we need to do this for each entity, since events have to query multiple entities at a time (source, target, and the event itself.)

## Future plans

Instead of hardcoding this entity involvement, we could use relations that mark an entity as the source or target of the event. Then, we can generalize a system for arbitrarily querying multiple entities (which is especially useful for parent/child relationships!)

Once there is a faster solution to finding matching systems, events would be a multi-entity extension to that problem. We are likely to want multi-entity queries elsewhere too (ex parent/child relations), so these solutions should be one.
