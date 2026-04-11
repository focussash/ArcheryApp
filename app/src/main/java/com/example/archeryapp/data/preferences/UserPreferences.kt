package com.example.archeryapp.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.archeryapp.domain.model.TargetType

internal interface TargetTypeStore {
    fun read(): String?
    fun write(code: String)
}

class UserPreferences internal constructor(
    private val store: TargetTypeStore
) {
    constructor(context: Context) : this(SharedPrefsTargetTypeStore(context))

    fun getLastTargetType(): TargetType = TargetType.fromCode(store.read())

    fun setLastTargetType(type: TargetType) {
        store.write(type.code)
    }
}

private class SharedPrefsTargetTypeStore(context: Context) : TargetTypeStore {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = prefs.getString(KEY_LAST_TARGET_TYPE, null)

    override fun write(code: String) {
        prefs.edit().putString(KEY_LAST_TARGET_TYPE, code).apply()
    }

    companion object {
        private const val PREFS_NAME = "archery_user_preferences"
        private const val KEY_LAST_TARGET_TYPE = "last_target_type"
    }
}
