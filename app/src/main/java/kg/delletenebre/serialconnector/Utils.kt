package kg.delletenebre.serialconnector

import android.content.Context
import android.content.Intent
import android.os.Build

object Utils {
    fun getResourceId(context: Context, name: String, type: String): Int {
        return context.resources.getIdentifier(name, type, context.packageName)
    }

    fun getStringIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "string", context.packageName)
    }

    fun getIntegerIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "integer", context.packageName)
    }

    fun getBooleanIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "bool", context.packageName)
    }

    fun getIdIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "id", context.packageName)
    }

    fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopService(context: Context, intent: Intent) {
        context.stopService(intent)
    }

    fun restartService(context: Context, intent: Intent) {
        stopService(context, intent)
        startService(context, intent)
    }
}