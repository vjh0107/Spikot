package io.github.ReadyMadeProgrammer.Spikot.misc

import java.util.*

interface UndoStack<T : Undoable> {
    val isUndoable: Boolean
    fun push(work: T)
    fun undo()
}

class SimpleUndoStack<T : Undoable> : UndoStack<T> {
    private val stack = Stack<T>()
    override val isUndoable: Boolean
        get() = stack.isNotEmpty()

    override fun push(work: T) {
        if (!work.isUndoable) {
            throw IllegalArgumentException("Not undoable value")
        } else {
            stack.push(work)
        }
    }

    override fun undo() {
        if (isUndoable) {
            stack.pop().undo()
        } else {
            throw IllegalArgumentException("No more undoable work")
        }
    }
}