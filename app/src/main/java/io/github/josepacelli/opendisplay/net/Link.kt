package io.github.josepacelli.opendisplay.net

import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * A live peer connection: a byte stream pair plus a way to close it.
 * Lets [PhoneReceiver] run the same protocol over TCP or a USB accessory fd.
 */
interface Link {
    /** Bytes coming from the peer. */
    val input: InputStream

    /** Bytes going to the peer. */
    val output: OutputStream

    /** Peer identity, for logs and mid-session peer-swap detection. */
    val label: String

    /** Releases every resource this link holds. Safe to call more than once. */
    fun close()
}

/** WiFi, or loopback via the Mac's `adb forward` override.
 * @param socket an already-accepted, connected TCP socket. */
class SocketLink(private val socket: Socket) : Link {
    init {
        socket.tcpNoDelay = true
    }

    override val input: InputStream = socket.getInputStream()
    override val output: OutputStream = socket.getOutputStream()
    override val label: String = socket.remoteSocketAddress?.toString() ?: "unknown"

    override fun close() {
        socket.close()
    }
}

/**
 * USB accessory transport (AOA). The streams share one fd via reference
 * counting, so all three owners must be closed explicitly — otherwise the
 * GC finalizes them later, possibly after the fd number has been reused.
 *
 * @param descriptor open file descriptor for the attached USB accessory.
 */
class AccessoryLink(private val descriptor: ParcelFileDescriptor) : Link {
    override val input: InputStream = FileInputStream(descriptor.fileDescriptor)
    override val output: OutputStream = FileOutputStream(descriptor.fileDescriptor)

    /** Single accessory host — no address to report. */
    override val label: String = "usb-accessory"

    override fun close() {
        try {
            input.close()
        } catch (_: IOException) {
        }
        try {
            output.close()
        } catch (_: IOException) {
        }
        try {
            descriptor.close()
        } catch (_: IOException) {
        }
    }
}
