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

object PrefabsModuleExtensions {
    /** Loads prefabs under a [namespace] from a list of jar resource paths provided by a [classLoaderRef]. */
    fun PrefabsModule.fromJarResources(
        namespace: String,
        classLoaderRef: KClass<*>,
        vararg resources: String,
    ) {
        val classLoader = classLoaderRef.java.classLoader
        loader.load(
            PrefabPath(namespace) {
                resources.asSequence()
                    .map { getResource(classLoader, it) }
                    .mapNotNull { it?.asPrefabSource(namespace) }
            }
        )
    }

    /** Loads prefabs under a [namespace] from all subpaths of a jar resource path provided by a [classLoaderRef]. */
    fun PrefabsModule.fromJarResourceDirectory(
        namespace: String,
        classLoaderRef: KClass<*>,
        folder: String,
    ) {
        val classLoader = classLoaderRef.java.classLoader
        loader.load(
            PrefabPath(namespace) {
                walkJarResources(classLoader, folder).map { it.asPrefabSource(namespace) }
            }
        )
    }

    /** Loads prefabs under a [namespace] from a list of java nio paths. */
    fun PrefabsModule.fromFiles(
        namespace: String,
        vararg from: java.nio.file.Path,
    ) {
        fromFiles(namespace, *from.map { Path(it.pathString) }.toTypedArray())
    }


    /** Loads prefabs under a [namespace] from a java nio directory by walking it. */
    fun PrefabsModule.fromDirectory(
        namespace: String,
        folder: java.nio.file.Path,
    ) {
        fromDirectory(namespace, Path(folder.pathString))
    }

    @OptIn(ExperimentalPathApi::class)
    private fun walkJarResources(
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
                                path = entry.name.substringAfter(directory).removePrefix("/"),
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

    private fun getResource(classLoader: ClassLoader, path: String): JarResource? {
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
        val resource: String,
    ) {
        val nameWithoutExt = path.substringAfterLast(FileSystems.getDefault().separator).substringBeforeLast(".")
        val ext = path.substringAfterLast(".")
        val stream = classLoader.getResourceAsStream(resource)!!
    }
}
