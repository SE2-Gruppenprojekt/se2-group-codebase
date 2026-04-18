package at.aau.serg.android.datastore.user

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import at.aau.serg.android.datastore.proto.User
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserSerializer : Serializer<User> {

    override val defaultValue: User = User.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): User {
        try {
            return User.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read User proto.", e)
        }
    }

    override suspend fun writeTo(t: User, output: OutputStream) {
        t.writeTo(output)
    }
}
