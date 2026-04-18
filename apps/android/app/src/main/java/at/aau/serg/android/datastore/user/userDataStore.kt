package at.aau.serg.android.datastore.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import at.aau.serg.android.datastore.proto.User

internal val Context.userDataStore: DataStore<User> by dataStore(
    fileName = "user.pb",
    serializer = UserSerializer
)
