package com.emeritus.student.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.instructure.canvasapi2.utils.ContextKeeper

/**
 * Responsible for refreshing widgets.
 */
object WidgetUpdater {

     fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(ContextKeeper.appContext)

        updateNotificationsWidget(appWidgetManager)
        updateGradesWidget(appWidgetManager)
        updateTodoWidget(appWidgetManager)
    }

    private fun updateNotificationsWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getNotificationWidgetUpdateIntent(appWidgetManager))
    }

    private fun updateGradesWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getGradesWidgetUpdateIntent(appWidgetManager))
    }

    private fun updateTodoWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getTodoWidgetUpdateIntent(appWidgetManager))
    }

    fun getNotificationWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, NotificationWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, NotificationWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }

    fun getGradesWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, GradesWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, GradesWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }

    fun getTodoWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, TodoWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, TodoWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }
}
