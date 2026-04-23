package at.aau.serg.android.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import java.io.InputStream
import java.io.OutputStream

class ProtoSerializer<T : MessageLite>(
    override val defaultValue: T,
    private val parser: Parser<T>,
    private val debugName: String
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T =
        try {
            parser.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto file: $debugName", e)
        }

    override suspend fun writeTo(t: T, output: OutputStream) {
        t.writeTo(output)
    }
}
