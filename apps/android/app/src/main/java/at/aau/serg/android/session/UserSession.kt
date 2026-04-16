package at.aau.serg.android.session

import android.content.Context
import at.aau.serg.android.util.UserPrefs

object UserSession {

    fun isLoggedIn(context: Context): Boolean {
        return UserPrefs.getUsername(context) != null
    }

    fun getUsername(context: Context): String? {
        return UserPrefs.getUsername(context)
    }

    fun logout(context: Context) {
        UserPrefs.clear(context)
    }
}
