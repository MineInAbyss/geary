package com.mineinabyss.geary.prefabs

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import java.io.InputStream
import java.net.URL
import java.util.jar.JarFile
import kotlin.io.path.pathString
import kotlin.reflect.KClass

object PrefabsDSLExtensions {
    fun PrefabsDSL.fromJarResources(
        classLoaderRef: KClass<*>,
        vararg resources: String,
    ) {
        val classLoader = classLoaderRef.java.classLoader
        prefabsBuilder.paths.add(
            PrefabPath(namespaced.namespace) {
                resources.asSequence().map {
                    PrefabSource(
                        source = (classLoader.getResourceAsStream(it)
                            ?: error("Resource $it not found when loading prefab"))
                            .asSource().buffered(),
                        key = PrefabKey.of(namespaced.namespace, it),
                        formatExt = it.substringAfterLast('.')
                    )
                }
            }
        )
    }

    fun PrefabsDSL.fromJarResourceDirectory(
        classLoaderRef: KClass<*>,
        folder: String,
    ) {
        val classLoader = classLoaderRef.java.classLoader
        prefabsBuilder.paths.add(
            PrefabPath(namespaced.namespace) {
                walkJarResources(classLoader, folder).map {
                    PrefabSource(
                        source = it.asSource().buffered(),
                        key = PrefabKey.of(namespaced.namespace, it.toString()),
                        formatExt = it.toString().substringAfterLast('.')
                    )
                }
            }
        )
    }

    fun PrefabsDSL.fromFiles(vararg from: java.nio.file.Path) {
        fromFiles(*from.map { Path(it.pathString) }.toTypedArray())
    }

    fun PrefabsDSL.fromDirectory(folder: java.nio.file.Path) {
        fromDirectory(Path(folder.pathString))
    }

    fun walkJarResources(
        classLoader: ClassLoader,
        directory: String,
    ): Sequence<InputStream> = sequence {
        val dirUrl: URL = classLoader.getResource(directory) ?: return@sequence
        val jarPath = dirUrl.path.substringBefore("!").removePrefix("file:")
        val jarFile = JarFile(jarPath)
        val entries = jarFile.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.name.startsWith(directory) && !entry.isDirectory) {
                yield(jarFile.getInputStream(entry))
            }
        }
    }
}
