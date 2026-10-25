package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.SwapItRepository
import kotlinx.coroutines.runBlocking
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
        assertEquals("SwapIt", appName)
    }

    @Test
    fun `test repository seed data and user switching`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SwapItRepository(context)
        repo.ensureSeeded()

        // Verify active default user is Emeka
        assertEquals("user_emeka", repo.currentUserId.value)

        // Switch to Zainab
        repo.switchActiveUser("user_zainab")
        assertEquals("user_zainab", repo.currentUserId.value)

        // Verify Nigerian listing can be fetched
        val item = repo.getListing("list_s23ultra")
        assertNotNull(item)
        assertEquals("Lagos", item?.state)
    }
}
