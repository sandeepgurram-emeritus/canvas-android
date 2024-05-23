/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.student.features.people.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.facade.UserFacade

class PeopleDetailsLocalDataSource(private val userFacade: UserFacade): PeopleDetailsDataSource {

    override suspend fun loadUser(canvasContext: CanvasContext, userId: Long, forceNetwork: Boolean): User? {
        return userFacade.getUserById(userId)
    }

    override suspend fun loadMessagePermission(canvasContext: CanvasContext, requestedPermissions: List<String>, user: User?, forceNetwork: Boolean): Boolean {
        return false
    }
}