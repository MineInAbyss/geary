package com.mineinabyss.geary.prefabs

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile
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
                        yield(JarResource(
                            nameWithoutExt = entry.name.substringAfterLast("/").substringBeforeLast("."),
                            ext = entry.name.substringAfterLast("."),
                            stream = jarFile.getInputStream(entry)
                        ))
                    }
                }
            } else if (protocol == "file") {
                val directoryPath = File(classLoader.getResource(directory)?.toURI() ?: return@sequence).toPath()
                yieldAll(directoryPath.walk().filter { it.isRegularFile() }.map {
                    JarResource(
                        nameWithoutExt = it.nameWithoutExtension,
                        ext = it.extension,
                        stream = it.inputStream()
                    )
                })
            }
        }
    }

    fun getResource(classLoader: ClassLoader, path: String): JarResource? {
        return classLoader.getResourceAsStream(path)?.let { stream ->
            JarResource(
                nameWithoutExt = path.substringAfterLast("/").substringBeforeLast("."),
                ext = path.substringAfterLast("."),
                stream = stream
            )
        }
    }

    private fun JarResource.asPrefabSource(namespace: String) = PrefabSource(
        source = stream.asSource().buffered(),
        key = PrefabKey.of(namespace, nameWithoutExt),
        formatExt = ext
    )

    data class JarResource(
        val nameWithoutExt: String,
        val ext: String,
        val stream: InputStream,
    )
}
