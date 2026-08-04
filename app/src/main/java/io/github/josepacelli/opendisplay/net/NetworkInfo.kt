package io.github.josepacelli.opendisplay.net

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Local IPv4 lookup for the manual-connection fallback: when mDNS discovery
 * doesn't reach the Mac (some routers/corporate networks block multicast),
 * the user can type this address + port straight into the Mac app's host
 * override instead.
 *
 * Uses [NetworkInterface] rather than the deprecated
 * `WifiManager.connectionInfo.ipAddress` so it keeps working over Ethernet
 * adapters or a USB-tethering-provided interface too, not just WiFi.
 */
object NetworkInfo {

    fun localIPv4Address(): String? = localIPv4InetAddress()?.hostAddress

    /** Same lookup as [localIPv4Address], as an [Inet4Address] ready to bind a
     * [java.net.ServerSocket] to directly — so the socket only listens on the
     * WiFi-reachable interface instead of every interface (see SECURITY.md/SCR-006). */
    fun localIPv4InetAddress(): Inet4Address? {
        return try {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
