package com.daaniikusnanta.storyapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.net.toUri
import com.daaniikusnanta.storyapp.R

class StoriesWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null) {
            if (intent.action == TOAST_ACTION) {
                val user = intent.getStringExtra(EXTRA_ITEM)
                    ?: context.getString(R.string.story_user_placeholder)

                Toast.makeText(
                    context,
                    user,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TOAST_ACTION = "com.daaniikusnanta.storyapp.widget.TOAST_ACTION"
        const val EXTRA_ITEM = "com.daaniikusnanta.storyapp.widget.EXTRA_ITEM"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, StackWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = intent.toUri(Intent.URI_INTENT_SCHEME).toUri()

            val toastIntent = Intent(context, StoriesWidget::class.java)
            toastIntent.action = TOAST_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                else 0
            )

            val views = RemoteViews(context.packageName, R.layout.stories_widget)
            views.setRemoteAdapter(R.id.stack_view, intent)
            views.setEmptyView(R.id.stack_view, R.id.empty_view)
            views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}