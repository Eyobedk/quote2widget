package com.example.quotewidget

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class Quote(val text: String, val author: String = "")

data class Palette(val name: String, val background: Int, val text: Int)

/** Stores quotes, the quote currently shown on the widget, and the chosen color theme. */
object QuoteStore {
    private const val PREFS = "quote_widget"
    private const val KEY_QUOTES = "quotes"
    private const val KEY_CURRENT = "current"
    private const val KEY_PALETTE = "palette"

    val palettes = listOf(
        Palette("Midnight", 0xFF1F2937.toInt(), 0xFFFFFFFF.toInt()),
        Palette("Ocean", 0xFF0E7490.toInt(), 0xFFFFFFFF.toInt()),
        Palette("Forest", 0xFF166534.toInt(), 0xFFFFFFFF.toInt()),
        Palette("Sunset", 0xFFC2410C.toInt(), 0xFFFFFFFF.toInt()),
        Palette("Rose", 0xFFBE185D.toInt(), 0xFFFFFFFF.toInt()),
        Palette("Paper", 0xFFFFFBEB.toInt(), 0xFF1F2937.toInt()),
    )

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(ctx: Context): MutableList<Quote> {
        val raw = prefs(ctx).getString(KEY_QUOTES, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Quote(o.getString("text"), o.optString("author", ""))
            }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(ctx: Context, quotes: List<Quote>) {
        val arr = JSONArray()
        quotes.forEach { arr.put(JSONObject().put("text", it.text).put("author", it.author)) }
        prefs(ctx).edit().putString(KEY_QUOTES, arr.toString()).apply()
    }

    /** The quote the widget should show right now, or null when there are no quotes yet. */
    fun current(ctx: Context): Quote? {
        val quotes = load(ctx)
        if (quotes.isEmpty()) return null
        val index = prefs(ctx).getInt(KEY_CURRENT, 0).coerceIn(0, quotes.lastIndex)
        return quotes[index]
    }

    /** Pick a different random quote (never the same one twice in a row). */
    fun advance(ctx: Context) {
        val size = load(ctx).size
        if (size <= 1) return
        val current = prefs(ctx).getInt(KEY_CURRENT, 0)
        var next: Int
        do {
            next = Random.nextInt(size)
        } while (next == current)
        prefs(ctx).edit().putInt(KEY_CURRENT, next).apply()
    }

    fun paletteIndex(ctx: Context): Int =
        prefs(ctx).getInt(KEY_PALETTE, 0).coerceIn(0, palettes.lastIndex)

    fun palette(ctx: Context): Palette = palettes[paletteIndex(ctx)]

    fun setPalette(ctx: Context, index: Int) {
        prefs(ctx).edit().putInt(KEY_PALETTE, index).apply()
    }
}
