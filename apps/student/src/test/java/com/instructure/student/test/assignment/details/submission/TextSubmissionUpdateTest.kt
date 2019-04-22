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
 */
package com.instructure.student.test.assignment.details.submission

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.TextSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.TextSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.TextSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.TextSubmissionUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class TextSubmissionUpdateTest : Assert() {
    private val initSpec = InitSpec(TextSubmissionUpdate()::init)
    private val updateSpec = UpdateSpec(TextSubmissionUpdate()::update)

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var initModel: TextSubmissionModel

    @Before
    fun setup() {
        course = Course()
        assignment = Assignment(id = 1234L, courseId = course.id, name = "name")
        initModel = TextSubmissionModel(assignmentId = assignment.id, canvasContext = course, assignmentName = assignment.name)
    }

    @Test
    fun `Initializes with an InitializeText effect`() {
        val text = "Some text from a save"
        val startModel = initModel.copy(initialText = text)
        initSpec
                .whenInit(startModel)
                .then(
                        assertThatFirst(
                                FirstMatchers.hasModel(startModel),
                                matchesFirstEffects<TextSubmissionModel, TextSubmissionEffect>(TextSubmissionEffect.InitializeText(text))
                        )
                )
    }

    @Test
    fun `TextChanged event with non empty text results in model change to isSubmittable`() {
        val text = "Some text to submit"
        val startModel = initModel.copy(isSubmittable = false)
        val expectedModel = startModel.copy(isSubmittable = true)

        updateSpec
                .given(startModel)
                .whenEvent(TextSubmissionEvent.TextChanged(text))
                .then(
                        assertThatNext(
                                NextMatchers.hasModel(expectedModel)
                        )
                )
    }

    @Test
    fun `TextChanged event with empty text results in model change to not isSubmittable`() {
        val text = ""
        val startModel = initModel.copy(isSubmittable = true)
        val expectedModel = startModel.copy(isSubmittable = false)

        updateSpec
                .given(startModel)
                .whenEvent(TextSubmissionEvent.TextChanged(text))
                .then(
                        assertThatNext(
                                NextMatchers.hasModel(expectedModel)
                        )
                )
    }

    @Test
    fun `SubmitClicked event results in SubmitText effect`() {
        val text = "Some text to submit"

        mockkStatic(URLEncoder::class)
        every { URLEncoder.encode(any(), any()) } returns text

        updateSpec
                .given(initModel)
                .whenEvent(TextSubmissionEvent.SubmitClicked(text))
                .then(
                        assertThatNext(
                                matchesEffects<TextSubmissionModel, TextSubmissionEffect>(TextSubmissionEffect.SubmitText(text, course, assignment.id, assignment.name))
                        )
                )
    }

    @Test
    fun `SubmitClicked event with new lines in text results in SubmitText effect`() {
        val text = "Some text to submit\nWith a new line"
        val expected = "Some text to submit<br/>With a new line"

        mockkStatic(URLEncoder::class)
        every { URLEncoder.encode(any(), any()) } returns expected

        updateSpec
                .given(initModel)
                .whenEvent(TextSubmissionEvent.SubmitClicked(text))
                .then(
                        assertThatNext(
                                matchesEffects<TextSubmissionModel, TextSubmissionEffect>(TextSubmissionEffect.SubmitText(expected, course, assignment.id, assignment.name))
                        )
                )
    }

    @Test
    fun `SubmitClicked event with unsupported encoding characters in text results in SubmitText effect`() {
        val text = "Some text to submit"

        mockkStatic(URLEncoder::class)
        every { URLEncoder.encode(any(), any()) } throws UnsupportedEncodingException()

        updateSpec
                .given(initModel)
                .whenEvent(TextSubmissionEvent.SubmitClicked(text))
                .then(
                        assertThatNext(
                                matchesEffects<TextSubmissionModel, TextSubmissionEffect>(TextSubmissionEffect.SubmitText(text, course, assignment.id, assignment.name))
                        )
                )
    }

}