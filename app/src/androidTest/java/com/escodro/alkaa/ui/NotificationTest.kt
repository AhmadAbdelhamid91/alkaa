package com.escodro.alkaa.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.escodro.alkaa.R
import com.escodro.alkaa.data.local.model.Task
import com.escodro.alkaa.framework.AcceptanceTest
import com.escodro.alkaa.ui.main.MainActivity
import com.escodro.alkaa.ui.task.alarm.TaskAlarmManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.koin.standalone.inject
import java.util.Calendar

/**
 * Test class to validate the notification flow.
 */
class NotificationTest : AcceptanceTest<MainActivity>(MainActivity::class.java) {

    private val alarmManager: TaskAlarmManager by inject()

    private val appName by lazy { context.getString(R.string.app_name) }

    @After
    fun clearTable() {
        daoProvider.getTaskDao().cleanTable()
    }

    @Test
    fun validateTaskNotification() {
        val taskName = "SOMEBODY RING THE ALARM"
        insertTask(taskName)
        goToNotificationDrawer()
        validateNotificationContent(taskName)
        clearAllNotifications()
    }

    @Test
    fun validateTaskUpdate() {
        val task = insertTask("Hi, I'm a PC")
        val updatedTitle = "Hi, I'm a Mac"
        task.title = updatedTitle
        daoProvider.getTaskDao().updateTask(task)

        goToNotificationDrawer()
        validateNotificationContent(updatedTitle)
        clearAllNotifications()
    }

    @Test
    fun validateCompletedTask() {
        val taskName = "I should not be seen"
        val task = insertTask(taskName)
        task.completed = true
        daoProvider.getTaskDao().updateTask(task)

        goToNotificationDrawer()
        validateNotificationNotShown(taskName)
    }

    @Test
    fun validateDeletedTask() {
        val taskName = "Ops, I did it again..."
        val task = insertTask(taskName)
        daoProvider.getTaskDao().deleteTask(task)

        goToNotificationDrawer()
        validateNotificationNotShown(taskName)
    }

    @Test
    fun validateTaskNotificationFlow() {
        val taskName = "Hey Jude"
        insertTask(taskName)
        goToNotificationDrawer()
        validateNotificationContent(taskName)
        uiDevice.findObject(By.text(taskName)).click()
        checkThat.viewHasText(R.id.edittext_taskdetail_title, taskName)
    }

    private fun insertTask(taskName: String) =
        with(Task(id = 15, title = taskName)) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.SECOND, 3)
            dueDate = calendar
            daoProvider.getTaskDao().insertTask(this)
            alarmManager.scheduleTaskAlarm(this)
            this
        }

    private fun goToNotificationDrawer() {
        uiDevice.pressHome()
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.pkg("com.android.systemui")), 1000)
    }

    private fun validateNotificationContent(taskName: String) {
        uiDevice.wait(Until.hasObject(By.text(appName)), 5000)
        val title = uiDevice.findObject(By.text(appName))
        val text = uiDevice.findObject(By.text(taskName))
        assertEquals(appName, title.text)
        assertEquals(taskName, text.text)
    }

    private fun validateNotificationNotShown(taskName: String) {
        uiDevice.wait(Until.hasObject(By.text(appName)), 5000)
        val title = uiDevice.findObject(By.text(appName))
        val text = uiDevice.findObject(By.text(taskName))
        assertNull(title)
        assertNull(text)
    }

    private fun clearAllNotifications() {
        goToNotificationDrawer()
        val clearAll: UiObject2 =
            uiDevice.findObject(By.res("com.android.systemui:id/dismiss_text"))
        clearAll.click()
    }
}
