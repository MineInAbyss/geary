package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.ecs.api.GearyContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.BinaryFormat
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

context(GearyContext)
public class FileSystemStore(
    private val root: Path,
    private val format: BinaryFormat,
) : GearyStore {
    override suspend fun encode(entity: GearyEntity): ByteArray {
        return format.encodeToByteArray(
            GearyStore.componentsSerializer,
            entity.getPersistingComponents()
        )
    }

    override suspend fun decode(entity: GearyEntity, uuid: UUID) {
        try {
            val bytes = read(uuid) ?: return
            entity.apply {
                setAllPersisting(
                    format.decodeFromByteArray(
                        GearyStore.componentsSerializer,
                        bytes
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }

    override fun read(uuid: UUID): ByteArray? {
        val file = (root / uuid.toString())
        if (file.notExists()) return null
        return file.readBytes()
    }

    override suspend fun write(entity: GearyEntity, bytes: ByteArray) {
        val uuid = entity.getOrSet { UUID.randomUUID() }
        val encoded = encode(entity)
        val file = (root / uuid.toString())
        file.createFile().writeBytes(encoded)
    }
}
