# Memory structure

Many ECSes advertise their speed over object-oriented approaches. How an ECS structures its data and the benefits or downsides that come with it is a complex topic, with many structures to explore. However, a common approach in designing an ECS is to keep related data together, specifically to help avoid cache misses.

The JVM has traditionally managed a lot of memory organization on its own, but through [Project Valhalla](https://en.wikipedia.org/wiki/Project_Valhalla_(Java_language)) we can hopefully get more control over the memory structure (at least ensure tightly packed data) and start thinking about optimizations from there.

For anyone interested, the following talk goes over how to think about CPU cache and some of its quirks:

<iframe width="560" height="315" src="https://www.youtube.com/embed/BP6NxVxDQIs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen="true"></iframe>
