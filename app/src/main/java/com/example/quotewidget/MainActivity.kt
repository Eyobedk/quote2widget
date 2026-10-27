package com.example.quotewidget

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var quotes: MutableList<Quote>
    private lateinit var adapter: QuoteAdapter
    private lateinit var swatches: LinearLayout
    private lateinit var quoteInput: EditText
    private lateinit var authorInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        quotes = QuoteStore.load(this)
        adapter = QuoteAdapter()

        quoteInput = findViewById(R.id.quote_input)
        authorInput = findViewById(R.id.author_input)
        swatches = findViewById(R.id.swatches)

        val list = findViewById<ListView>(R.id.quote_list)
        list.adapter = adapter
        list.emptyView = findViewById(R.id.empty_view)
        list.setOnItemClickListener { _, _, position, _ -> showEditDialog(position) }

        findViewById<Button>(R.id.add_button).setOnClickListener { addQuote() }
        findViewById<Button>(R.id.pin_button).setOnClickListener { pinWidget() }

        buildSwatches()
    }

    override fun onResume() {
        super.onResume()
        // Keep the widget in sync whenever the app is opened.
        QuoteWidgetProvider.refreshAll(this)
    }

    private fun addQuote() {
        val text = quoteInput.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.type_a_quote, Toast.LENGTH_SHORT).show()
            return
        }
        quotes.add(0, Quote(text, authorInput.text.toString().trim()))
        quoteInput.text.clear()
        authorInput.text.clear()
        persist()
    }

    private fun showEditDialog(position: Int) {
        val quote = quotes[position]
        val pad = dp(20)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, dp(8), pad, 0)
        }
        val textField = EditText(this).apply {
            setText(quote.text)
            hint = getString(R.string.quote_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 2
        }
        val authorField = EditText(this).apply {
            setText(quote.author)
            hint = getString(R.string.author_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }
        container.addView(textField)
        container.addView(authorField)

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_quote)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val newText = textField.text.toString().trim()
                if (newText.isNotEmpty()) {
                    quotes[position] = Quote(newText, authorField.text.toString().trim())
                    persist()
                }
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                quotes.removeAt(position)
                persist()
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    private fun persist() {
        QuoteStore.save(this, quotes)
        adapter.notifyDataSetChanged()
        QuoteWidgetProvider.refreshAll(this)
    }

    private fun buildSwatches() {
        swatches.removeAllViews()
        val selected = QuoteStore.paletteIndex(this)
        QuoteStore.palettes.forEachIndexed { index, palette ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(12) }
                contentDescription = palette.name
                val transparent = palette.background ushr 24 == 0
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    // Transparent theme is drawn as a faint disc with a dashed outline.
                    setColor(if (transparent) 0x14000000 else palette.background)
                    when {
                        index == selected -> setStroke(dp(3), 0xFFFF9800.toInt())
                        transparent -> setStroke(dp(2), 0x88000000.toInt(), dp(4).toFloat(), dp(3).toFloat())
                        else -> setStroke(dp(1), 0x33000000)
                    }
                }
                setOnClickListener {
                    QuoteStore.setPalette(this@MainActivity, index)
                    QuoteWidgetProvider.refreshAll(this@MainActivity)
                    buildSwatches()
                }
            }
            swatches.addView(dot)
        }
    }

    private fun pinWidget() {
        val manager = getSystemService(AppWidgetManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(
                ComponentName(this, QuoteWidgetProvider::class.java), null, null
            )
        } else {
            Toast.makeText(this, R.string.pin_manual, Toast.LENGTH_LONG).show()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private inner class QuoteAdapter : ArrayAdapter<Quote>(this@MainActivity, 0, quotes) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView
                ?: this@MainActivity.layoutInflater.inflate(R.layout.item_quote, parent, false)
            val quote = getItem(position)!!
            view.findViewById<TextView>(R.id.item_text).text = quote.text
            val author = view.findViewById<TextView>(R.id.item_author)
            if (quote.author.isBlank()) {
                author.visibility = View.GONE
            } else {
                author.visibility = View.VISIBLE
                author.text = "— ${quote.author}"
            }
            return view
        }
    }
}
