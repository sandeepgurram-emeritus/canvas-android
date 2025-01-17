/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */    package com.emeritus.student.mobius.common

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import org.greenrobot.eventbus.EventBus
import java.util.*

abstract class EventBusSource<E> : EventSource<E> {
    private val queuedEvents = LinkedList<E>()

    private var consumer: Consumer<E>? = null

    open fun getEventBus(): EventBus = EventBus.getDefault()

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        consumer = eventConsumer
        getEventBus().register(this)
        while (queuedEvents.isNotEmpty()) {
            consumer?.accept(queuedEvents.poll())
        }
        return Disposable { dispose() }
    }

    fun dispose() {
        consumer = null
        getEventBus().unregister(this)
    }

    fun sendEvent(event: E) {
        if (consumer == null) {
            queuedEvents.add(event)
        } else {
            consumer?.accept(event)
        }
    }
}
