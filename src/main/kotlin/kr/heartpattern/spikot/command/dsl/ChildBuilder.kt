/*
 * Copyright 2020 HeartPattern
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

package kr.heartpattern.spikot.command.dsl

import kr.heartpattern.spikot.command.AbstractCommand
import kotlin.reflect.KClass

/**
 * Builder DSL for building child command
 */
class ChildBuilder internal constructor() {
    @PublishedApi
    internal val set: MutableSet<KClass<out AbstractCommand>> = mutableSetOf()

    /**
     * Add child command
     * @param T Type of child command
     */
    inline fun <reified T : AbstractCommand> add() {
        set += T::class
    }

    /**
     * Add child command
     * @param type Type of child command
     */
    fun <T : AbstractCommand> add(type: KClass<T>) {
        set += type
    }
}