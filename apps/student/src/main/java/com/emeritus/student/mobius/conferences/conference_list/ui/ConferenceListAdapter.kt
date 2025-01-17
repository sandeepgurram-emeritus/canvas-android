/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
 */
package com.emeritus.student.mobius.conferences.conference_list.ui

import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.instructure.pandautils.utils.asStateList
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setTextForVisibility
import com.emeritus.student.R
import kotlinx.android.synthetic.main.adapter_conference_item.view.*
import kotlinx.android.synthetic.main.adapter_conference_list_error.view.*

interface ConferenceListAdapterCallback : BasicItemCallback {
    fun onConferenceClicked(conferenceId: Long)
    fun reload()
}

class ConferenceListAdapter(callback: ConferenceListAdapterCallback) :
    BasicRecyclerAdapter<ConferenceListItemViewState, ConferenceListAdapterCallback>(callback) {
    override fun registerBinders() {
        register(ConferenceListEmptyBinder())
        register(ConferenceListErrorBinder())
        register(ConferenceListHeaderBinder())
        register(ConferenceListItemBinder())
    }
}

class ConferenceListEmptyBinder : BasicItemBinder<ConferenceListItemViewState.Empty, ConferenceListAdapterCallback>() {
    // TODO: Get correct image and messaging for empty view
    override val layoutResId = R.layout.adapter_conference_list_empty
    override val bindBehavior = NoBind()
}

class ConferenceListErrorBinder : BasicItemBinder<ConferenceListItemViewState.Error, ConferenceListAdapterCallback>() {
    // TODO: Get correct image and messaging for error view
    override val layoutResId = R.layout.adapter_conference_list_error
    override val bindBehavior = Item {_, callback, _ ->
        conferenceListRetry.onClick { callback.reload() }
    }
}

class ConferenceListHeaderBinder : BasicItemBinder<ConferenceListItemViewState.ConferenceHeader, ConferenceListAdapterCallback>() {
    override val layoutResId = R.layout.adapter_conference_header
    override val bindBehavior = Item {data, _, _ ->
        title.text = data.title
    }
}

class ConferenceListItemBinder : BasicItemBinder<ConferenceListItemViewState.ConferenceItem, ConferenceListAdapterCallback>() {
    override val layoutResId = R.layout.adapter_conference_item
    override val bindBehavior = Item { data, callback, _ ->
        icon.imageTintList = data.tint.asStateList()
        title.text = data.title
        subtitle.setTextForVisibility(data.subtitle)

        statusLabel.text = data.label
        statusLabel.setTextColor(data.labelTint)

        onClick { callback.onConferenceClicked(data.conferenceId) }
    }
}
