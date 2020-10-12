package kg.delletenebre.serialconnector.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Selection
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kg.delletenebre.serialconnector.CommunicationService
import kg.delletenebre.serialconnector.R
import java.text.SimpleDateFormat
import java.util.*

class LogsActivity : AppCompatActivity() {
    private val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    CommunicationService.ACTION_CONNECTION_ESTABLISHED -> {
                        val connectionType = intent.getStringExtra("connectionType").toString()
                        val name = intent.getStringExtra("name").toString()
                        addMessage("$connectionType\t\tсоединение установлено | $name")
                    }
                    CommunicationService.ACTION_CONNECTION_LOST -> {
                        val connectionType = intent.getStringExtra("connectionType").toString()
                        val name = intent.getStringExtra("name").toString()
                        addMessage("$connectionType\t\tсоединение потеряно | $name")
                    }
                    CommunicationService.ACTION_DATA_RECEIVED -> {
                        val connectionType = intent.getStringExtra("connectionType").toString()
                        val name = intent.getStringExtra("name").toString()
                        val data = intent.getStringExtra("data").toString()
                        addMessage("$connectionType\t\tполучены данные от $name\n\t\t\t\t\t\t$data")
                    }
                }
            }
        }
    }

    private lateinit var logsView: TextView
    private lateinit var autoScrollCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logs_activity)
        delegate.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        logsView = findViewById(R.id.logs_view)
        logsView.movementMethod = ScrollingMovementMethod()

        autoScrollCheckbox = findViewById(R.id.autoscroll_checkbox)

        val clearButton: Button = findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            logsView.text = ""
        }

        IntentFilter().also { intentFilter ->
            intentFilter.addAction(CommunicationService.ACTION_CONNECTION_ESTABLISHED)
            intentFilter.addAction(CommunicationService.ACTION_CONNECTION_LOST)
            intentFilter.addAction(CommunicationService.ACTION_DATA_RECEIVED)
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    fun addMessage(message: String) {
        if (message.isNotBlank()) {
            val timestamp = (SimpleDateFormat("HH:mm:ss", Locale.ROOT))
                .format(Calendar.getInstance().time)

            var crlf = ""
            if (message.last() != '\n') {
                crlf = "\r\n"
            }
            logsView.append("$timestamp\t$message$crlf")
            logsView.text = logsView.text // update text to apply line height

            val editable = logsView.editableText
            if (autoScrollCheckbox.isChecked) {
                Selection.setSelection(editable, editable.length)
            } else {
                Selection.removeSelection(editable)
            }
        }
    }
}