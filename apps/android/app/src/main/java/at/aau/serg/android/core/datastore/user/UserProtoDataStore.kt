package at.aau.serg.android.core.datastore.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import at.aau.serg.android.core.datastore.ProtoSerializer
import at.aau.serg.android.datastore.proto.User

private const val USER_DATASTORE_FILE = "user.pb"

internal val Context.UserProtoDataStore: DataStore<User> by dataStore(
    fileName = USER_DATASTORE_FILE,
    serializer = ProtoSerializer(
        defaultValue = User.getDefaultInstance(),
        parser = User.parser(),
        debugName = USER_DATASTORE_FILE
    )
)
