package at.aau.serg.android.core.datastore

import androidx.datastore.core.CorruptionException
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ProtoSerializerTest {

    private lateinit var serializer: ProtoSerializer<User>
    private lateinit var defaultUser: User

    @Before
    fun setup() {
        defaultUser = User.getDefaultInstance()

        serializer = ProtoSerializer(
            defaultValue = defaultUser,
            parser = User.parser(),
            debugName = "User"
        )
    }

    @Test
    fun readFrom_parsesValidProto() = runTest {
        val original = User.newBuilder()
            .setUid("123")
            .setDisplayName("Alice")
            .build()

        val input = ByteArrayInputStream(original.toByteArray())

        val parsed = serializer.readFrom(input)

        assertEquals("123", parsed.uid)
        assertEquals("Alice", parsed.displayName)
    }

    @Test
    fun readFrom_throwsCorruptionException_onInvalidBytes() = runTest {
        val invalidBytes = byteArrayOf(0x00, 0x01, 0x02)
        val input = ByteArrayInputStream(invalidBytes)

        try {
            serializer.readFrom(input)
            throw AssertionError("Expected CorruptionException but none was thrown")
        } catch (e: CorruptionException) {
            // success
        }
    }

    @Test
    fun writeTo_writesCorrectBytes() = runTest {
        val user = User.newBuilder()
            .setUid("abc")
            .setDisplayName("Bob")
            .build()

        val output = ByteArrayOutputStream()

        serializer.writeTo(user, output)

        val parsed = User.parseFrom(output.toByteArray())

        assertEquals("abc", parsed.uid)
        assertEquals("Bob", parsed.displayName)
    }

    @Test
    fun readWrite_roundTripConsistency() = runTest {
        val user = User.newBuilder()
            .setUid("round")
            .setDisplayName("Trip")
            .build()

        val output = ByteArrayOutputStream()
        serializer.writeTo(user, output)

        val input = ByteArrayInputStream(output.toByteArray())
        val parsed = serializer.readFrom(input)

        assertEquals(user, parsed)
    }
}
