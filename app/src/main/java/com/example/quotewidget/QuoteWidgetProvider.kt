package com.example.quotewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT) {
            QuoteStore.advance(context)
            refreshAll(context)
        }
    }

    companion object {
        const val ACTION_NEXT = "com.example.quotewidget.ACTION_NEXT"

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, QuoteWidgetProvider::class.java))
            ids.forEach { update(context, manager, it) }
        }

        private fun update(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quote)
            val quote = QuoteStore.current(context)
            val palette = QuoteStore.palette(context)

            // Tint the rounded background and recolor the text to match the chosen theme.
            if (palette.background ushr 24 == 0) {
                // Transparent theme: a zero-alpha tint would leave the white shape visible, so hide it.
                views.setViewVisibility(R.id.widget_bg, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_bg, View.VISIBLE)
                views.setInt(R.id.widget_bg, "setColorFilter", palette.background)
            }
            views.setTextColor(R.id.widget_text, palette.text)
            views.setTextColor(R.id.widget_author, palette.text)
            views.setTextColor(R.id.widget_edit, palette.text)

            if (quote == null) {
                views.setTextViewText(R.id.widget_text, context.getString(R.string.widget_empty))
                views.setTextViewText(R.id.widget_author, "")
            } else {
                views.setTextViewText(R.id.widget_text, "“${quote.text}”")
                views.setTextViewText(
                    R.id.widget_author,
                    if (quote.author.isBlank()) "" else "— ${quote.author}"
                )
            }

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            val openApp = PendingIntent.getActivity(
                context, 1, Intent(context, MainActivity::class.java), flags
            )
            val next = PendingIntent.getBroadcast(
                context, 0,
                Intent(context, QuoteWidgetProvider::class.java).setAction(ACTION_NEXT),
                flags
            )

            // Tap the widget: next quote (or open the app if there are no quotes yet).
            views.setOnClickPendingIntent(R.id.widget_root, if (quote == null) openApp else next)
            // Tap the pencil: open the app to edit quotes.
            views.setOnClickPendingIntent(R.id.widget_edit, openApp)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
