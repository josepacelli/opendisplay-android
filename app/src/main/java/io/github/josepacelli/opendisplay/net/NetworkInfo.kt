package io.github.josepacelli.opendisplay.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address

/**
 * Local IPv4 lookup for the manual-connection fallback: when mDNS discovery
 * doesn't reach the Mac (some routers/corporate networks block multicast),
 * the user can type this address + port straight into the Mac app's host
 * override instead.
 */
object NetworkInfo {

    /** @param context used to read connectivity state.
     * @return this device's local IPv4 address as text, or `null` if none is usable. */
    fun localIPv4Address(context: Context): String? = localIPv4InetAddress(context)?.hostAddress

    /** Same lookup as [localIPv4Address], as an [Inet4Address] ready to bind a
     * [java.net.ServerSocket] to directly — so the socket only listens on the
     * WiFi-reachable interface instead of every interface (see SECURITY.md/SCR-006).
     * `null` when the active network isn't WiFi/Ethernet — e.g. mobile data only,
     * WiFi off — so the unauthenticated listener never binds onto the cellular
     * network (see SECURITY.md/SCR-006, issue #39).
     *
     * Resolves the address through the same active [android.net.Network] that
     * passed the WiFi/Ethernet check, instead of scanning every
     * [java.net.NetworkInterface] on the device — otherwise a VPN's tunnel
     * interface (also "up, non-loopback") could win the race and the listener
     * would end up bound to whatever network the VPN routes to instead of the
     * LAN (see SECURITY.md/SCR-009, issue #135).
     *
     * A VPN's own [android.net.Network] reports `TRANSPORT_WIFI` too — Android
     * copies the underlying network's transport onto it — so the WiFi/Ethernet
     * check alone still passes for it; excluding `TRANSPORT_VPN` explicitly is
     * what keeps this from resolving to the tunnel interface when a VPN sits on
     * top of WiFi (confirmed on real hardware, WiFi + VPN both active).
     *
     * @param context used to read connectivity state.
     * @return the active network's first IPv4 address, or `null` if the active network isn't
     * WiFi/Ethernet or no such address exists. */
    fun localIPv4InetAddress(context: Context): Inet4Address? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return null
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        val isLocal = (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        if (!isLocal) return null
        val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
        return linkProperties.linkAddresses
            .mapNotNull { it.address as? Inet4Address }
            .firstOrNull()
    }
}
