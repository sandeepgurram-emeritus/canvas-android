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
 *
 */
package com.emeritus.student.ui.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.emeritus.student.R
import org.hamcrest.core.AllOf.allOf

class PickerSubmissionUploadPage : BasePage(R.id.pickerSubmissionUploadPage) {
    private val deviceIcon by OnViewWithId(R.id.sourceDeviceIcon)
    private val cameraIcon by OnViewWithId(R.id.sourceCameraIcon)
    private val galleryIcon by OnViewWithId(R.id.sourceGalleryIcon)

    fun chooseDevice() {
        deviceIcon.click()
    }

    fun chooseCamera() {
        cameraIcon.click()
    }

    fun chooseGallery() {
        galleryIcon.click()
    }

    fun waitForSubmitButtonToAppear() {
        waitForViewWithText(R.string.submit)
    }

    fun submit() {
        onView(allOf(withText(R.string.submit), isDisplayed())).click()
    }
}