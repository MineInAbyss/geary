package com.mineinabyss.geary.prefabs

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import java.io.File
import java.nio.file.FileSystems
import java.util.jar.JarFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString
import kotlin.io.path.walk
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
    ): Sequence<JarResource> = sequence {
        val resources = classLoader.getResources(directory)
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val protocol = resource.protocol
            if (protocol == "jar") {
                val jarPath = resource.path.substringBefore("!").removePrefix("file:")
                val jarFile = JarFile(jarPath)
                val entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.startsWith(directory) && !entry.isDirectory) {
                        yield(
                            JarResource(
                                classLoader,
                                path = entry.name.substringAfter(directory)
                                    .removePrefix(FileSystems.getDefault().separator),
                                resource = entry.name
                            )
                        )
                    }
                }
            } else if (protocol == "file") {
                val directoryPath = File(classLoader.getResource(directory)?.toURI() ?: return@sequence).toPath()
                yieldAll(directoryPath.walk().filter { it.isRegularFile() }.map {
                    JarResource(
                        classLoader,
                        path = it.toString().substringAfter(directoryPath.toString())
                            .removePrefix(FileSystems.getDefault().separator),
                        resource = directory + it.toString().substringAfter(directoryPath.toString())
                    )
                })
            }
        }
    }

    fun getResource(classLoader: ClassLoader, path: String): JarResource? {
        return classLoader.getResourceAsStream(path)?.let { stream ->
            JarResource(
                classLoader = classLoader,
                path = path,
                resource = path
            )
        }
    }

    private fun JarResource.asPrefabSource(namespace: String) = PrefabSource(
        source = stream.asSource().buffered(),
        key = PrefabKey.of(namespace, nameWithoutExt),
        formatExt = ext
    )

    data class JarResource(
        val classLoader: ClassLoader,
        val path: String,
        val resource: String
    ) {
        val nameWithoutExt = path.substringAfterLast(FileSystems.getDefault().separator).substringBeforeLast(".")
        val ext = path.substringAfterLast(".")
        val stream = classLoader.getResourceAsStream(resource)!!
    }
}
