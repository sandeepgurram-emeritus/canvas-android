package com.emeritus.student.db.sqlColAdapters

/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class DateColAdapterTest {

    @Test
    fun `Date encodes correctly`() {
        val localDateTime = OffsetDateTime.parse("2019-12-12T06:12:12-06:00")
        val expectedValue = "2019-12-12T12:12:12Z"
        val actualValue = DateAdapter().encode(localDateTime)

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `Date decodes correctly`() {
        val serverDateTime = "2019-12-12T12:12:12Z"
        val expectedValue = OffsetDateTime.parse(serverDateTime).withOffsetSameInstant(OffsetDateTime.now().offset)
        val actualValue = DateAdapter().decode(serverDateTime)

        assertEquals(expectedValue.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), actualValue.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }
}