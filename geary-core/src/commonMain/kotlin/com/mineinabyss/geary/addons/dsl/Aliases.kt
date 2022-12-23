package com.mineinabyss.geary.addons.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlin.reflect.KClass

/** The polymorphic builder scope that allows registering subclasses. */
typealias SerializerRegistry<T> = PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>?) -> Unit
