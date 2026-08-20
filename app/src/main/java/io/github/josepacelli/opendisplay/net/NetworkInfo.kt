package io.github.josepacelli.opendisplay.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    fun localIPv4Address(context: Context): String? = localIPv4InetAddress(context)?.hostAddress

    /** Same lookup as [localIPv4Address], as an [Inet4Address] ready to bind a
     * [java.net.ServerSocket] to directly — so the socket only listens on the
     * WiFi-reachable interface instead of every interface (see SECURITY.md/SCR-006).
     * `null` when the active network isn't WiFi/Ethernet — e.g. mobile data only,
     * WiFi off — so the unauthenticated listener never binds onto the cellular
     * network (see SECURITY.md/SCR-006, issue #39). */
    fun localIPv4InetAddress(context: Context): Inet4Address? {
        if (!isActiveNetworkLocal(context)) return null
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

    /** WiFi or Ethernet, i.e. a LAN — never cellular, which is a WAN uplink with
     * no business hosting an unauthenticated listener. */
    private fun isActiveNetworkLocal(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
