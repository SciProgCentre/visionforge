package ru.mipt.npm.root

import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public interface RootValueProvider {
    /**
     * Provide a member cast or reinterpreted to given type.
     * Returns null if member with given name/type could not be resolved.
     */
    public fun <T : Any> provideOrNull(name: String, type: KType): T?
}

public interface RootModel {
    public val provider: RootValueProvider
}

public inline fun <reified T : Any> RootValueProvider.provide(name: String): T =
    provideOrNull(name, typeOf<T>()) ?: error("A member with type ${T::class} and name $name could not be resolved")

public inline fun <reified T : Any> RootModel.member(name: String? = null): PropertyDelegateProvider<Any?, Lazy<T>> =
    PropertyDelegateProvider { _, property ->
        lazy { provider.provide(name ?: property.name) }
    }
