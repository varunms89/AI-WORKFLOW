package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.gemini.GeminiRoles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Order Workflow", appName)
  }

  @Test
  fun `gemini roles configuration test`() {
    val roles = GeminiRoles.ALL
    assertEquals(4, roles.size)

    val strategist = GeminiRoles.WORKFLOW_STRATEGIST
    assertEquals("Workflow Strategist", strategist.name)
    assertEquals("models/gemini-3.8-flash", strategist.recommendedModel)
    assertTrue(strategist.systemInstruction.contains("Workflow Strategist"))

    val general = GeminiRoles.GENERAL_OPERATIONS
    assertEquals("gemini-3.5-flash", general.recommendedModel)

    val fast = GeminiRoles.FAST_DISPATCHER
    assertEquals("gemini-3.1-flash-lite-preview", fast.recommendedModel)

    val architect = GeminiRoles.CODE_ARCHITECT
    assertEquals("gemini-3.1-pro-preview", architect.recommendedModel)
  }
}

