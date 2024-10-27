package com.mineinabyss.geary.prefabs

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import java.io.File
import java.io.InputStream
import kotlin.io.path.*
import kotlin.reflect.KClass

object PrefabsDSLExtensions {
    fun PrefabsDSL.fromJarResources(
        classLoaderRef: KClass<*>,
        vararg resources: String,
    ) {
        val classLoader = classLoaderRef.java.classLoader
        prefabsBuilder.paths.add(
            PrefabPath(namespaced.namespace) {
                resources.asSequence()
                    .map { getResource(classLoader, it) }
                    .mapNotNull { it?.asPrefabSource(namespaced.namespace) }
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
                walkJarResources(classLoader, folder).map { it.asPrefabSource(namespaced.namespace) }
            }
        )
    }

    fun PrefabsDSL.fromFiles(vararg from: java.nio.file.Path) {
        fromFiles(*from.map { Path(it.pathString) }.toTypedArray())
    }

    fun PrefabsDSL.fromDirectory(folder: java.nio.file.Path) {
        fromDirectory(Path(folder.pathString))
    }

    @OptIn(ExperimentalPathApi::class)
    fun walkJarResources(
        classLoader: ClassLoader,
        directory: String,
    ): Sequence<java.nio.file.Path> {
        val directoryPath = File(classLoader.getResource(directory)?.toURI() ?: return emptySequence()).toPath()
        return directoryPath.walk().filter { it.isRegularFile() }
    }

    fun getResource(classLoader: ClassLoader, path: String): java.nio.file.Path? {
        return File(classLoader.getResource(path)?.toURI() ?: return null).toPath()
    }

    private fun java.nio.file.Path.asPrefabSource(namespace: String) = PrefabSource(
        source = inputStream().asSource().buffered(),
        key = PrefabKey.of(namespace, nameWithoutExtension),
        formatExt = extension
    )

    data class NameToStream(val name: String, val stream: InputStream)
}
