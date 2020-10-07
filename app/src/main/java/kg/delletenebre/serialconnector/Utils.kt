package kg.delletenebre.serialconnector

import android.content.Context

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
}