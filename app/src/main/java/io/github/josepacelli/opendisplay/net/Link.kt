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
    val input: InputStream
    val output: OutputStream

    /** Peer identity, for logs and mid-session peer-swap detection. */
    val label: String

    fun close()
}

/** WiFi, or loopback via the Mac's `adb forward` override. */
class SocketLink(private val socket: Socket) : Link {
    init {
        // Disable Nagle: touch/scroll are small, latency-sensitive packets.
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
 */
class AccessoryLink(private val descriptor: ParcelFileDescriptor) : Link {
    override val input: InputStream = FileInputStream(descriptor.fileDescriptor)
    override val output: OutputStream = FileOutputStream(descriptor.fileDescriptor)

    /** Single accessory host — no address to report. */
    override val label: String = "usb-accessory"

    override fun close() {
        // Own try/catch each: one failing must not skip the others.
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
