package kr.heartpattern.spikot.config

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

internal class ConfigProperty<T>(
    private val default: T,
    private val name: String? = null
) : ReadWriteProperty<ConfigSpec, T> {
    private var cache: T? = null
    private var cached = false

    override fun getValue(thisRef: ConfigSpec, property: KProperty<*>): T {
        if (!cached) {
            @Suppress("UNCHECKED_CAST")
            cache = thisRef.yaml.getByType("${thisRef.path}.${name
                ?: property.name}", property.returnType.jvmErasure) as T?
            if (cache == null) {
                thisRef.yaml.set("${thisRef.path}.${name ?: property.name}", default)
            }
            cached = true
        }
        return cache ?: default
    }

    override fun setValue(thisRef: ConfigSpec, property: KProperty<*>, value: T) {
        thisRef.yaml.set("${thisRef.path}.${name ?: property.name}", value)
        cache = value
        cached = true
    }
}