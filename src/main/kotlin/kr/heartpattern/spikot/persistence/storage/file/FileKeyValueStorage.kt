/*
 * Copyright 2020 Spikot project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kr.heartpattern.spikot.persistence.storage.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kr.heartpattern.spikot.misc.*
import kr.heartpattern.spikot.module.AbstractModule
import kr.heartpattern.spikot.persistence.storage.KeyValueStorage
import kr.heartpattern.spikot.serialization.StringSerializeFormat
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class FileKeyValueStorage<K, V> private constructor(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>,
    private val format: StringSerializeFormat
) : AbstractModule(), KeyValueStorage<K, V> {
    constructor(
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>,
        format: StringSerializeFormat,
        namespace: String
    ) : this(keySerializer, valueSerializer, format) {
        this.namespace = namespace
    }

    constructor(
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>,
        format: StringSerializeFormat,
        directory: File
    ) : this(keySerializer, valueSerializer, format) {
        this.directory = directory
    }


    lateinit var directory: File
    var namespace: String? = null

    override fun onLoad() {
        if (namespace != null)
            directory = file(namespace!!)

        directory.mkdirs()
    }

    override suspend fun getAllKeys(): Collection<K> {
        return directory
            .listFiles()!!
            .filter {
                it.extension == format.fileExtensionName
            }
            .map {
                deserialize(keySerializer, it.nameWithoutExtension)
            }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun save(key: K, value: Option<V>) {
        val file = File(directory, serialize(keySerializer, key) + ".${format.fileExtensionName}")
        if (value is Just) {
            file.createNewFile()
            try {
                file.writeText(format.serializer.encodeToString(valueSerializer, value.value))
            } catch (ex: Exception) {
                println("Couldn't save to ${file.path}: key = ${key.toString()}, value = ${value.value.toString()}")
            }
        } else {
            file.delete()
        }
    }


    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun load(key: K): Option<V> {
        val file = File(directory, serialize(keySerializer, key) + ".${format.fileExtensionName}")
        return if (file.exists()) {
            format.serializer.decodeFromString(valueSerializer, file.readText()).just
        } else {
            None
        }
    }


    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            directory.listFiles(File::delete)
        }
    }

    private fun <T> serialize(serializer: KSerializer<T>, value: T): String {
        val encoder = SingleStringEncoder()
        serializer.serialize(encoder, value)
        return encoder.encoded!!
    }

    private fun <T> deserialize(serializer: KSerializer<T>, value: String): T {
        val decoder = SingleStringDecoder(value)
        return serializer.deserialize(decoder)
    }
}