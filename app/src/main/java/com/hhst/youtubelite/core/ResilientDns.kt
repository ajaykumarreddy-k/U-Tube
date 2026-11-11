package com.hhst.youtubelite.core

import okhttp3.Dns
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

/**
 * Dual-stack resilient DNS resolver for mobile networks and opportunistic Private DNS:
 * - Short fresh cache deduplicates concurrent player/thumbnail lookups.
 * - Retries a transient [UnknownHostException] once after a brief backoff.
 * - Prefers IPv4 addresses ahead of IPv6 while retaining IPv6 fallback to avoid broken IPv6 stalls.
 * - Falls back to recently resolved addresses if system DNS hiccups transiently.
 */
class ResilientDns(
    private val freshTtlMs: Long = DEFAULT_FRESH_TTL_MS,
    private val staleTtlMs: Long = DEFAULT_STALE_TTL_MS,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
    private val nowMs: () -> Long = System::currentTimeMillis,
    private val systemLookup: (String) -> List<InetAddress> = Dns.SYSTEM::lookup,
) : Dns {
    private data class Entry(val addresses: List<InetAddress>, val updatedAtMs: Long)

    private val cache = ConcurrentHashMap<String, Entry>()

    override fun lookup(hostname: String): List<InetAddress> {
        val key = hostname.trim().lowercase()
        if (key.isEmpty()) throw UnknownHostException(hostname)

        val now = nowMs()
        val cached = cache[key]
        if (cached != null && now - cached.updatedAtMs in 0L..freshTtlMs) {
            return cached.addresses
        }

        val resolved = resolveWithRetry(hostname)
        if (resolved != null && resolved.isNotEmpty()) {
            val ordered = orderPreferIpv4(resolved)
            cache[key] = Entry(ordered, nowMs())
            return ordered
        }

        val fallback = cache[key]
        if (fallback != null && nowMs() - fallback.updatedAtMs <= staleTtlMs) {
            return fallback.addresses
        }
        throw UnknownHostException("Unable to resolve host $hostname")
    }

    private fun resolveWithRetry(hostname: String): List<InetAddress>? {
        var lastError: UnknownHostException? = null
        repeat(2) { attempt ->
            if (attempt > 0 && retryDelayMs > 0L) {
                try {
                    Thread.sleep(retryDelayMs)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw lastError ?: UnknownHostException(hostname)
                }
            }
            try {
                val result = systemLookup(hostname)
                if (result.isNotEmpty()) return result
            } catch (e: UnknownHostException) {
                lastError = e
            }
        }
        if (cache[hostname.trim().lowercase()] != null) return null
        throw lastError ?: UnknownHostException(hostname)
    }

    companion object {
        const val DEFAULT_FRESH_TTL_MS = 20_000L
        const val DEFAULT_STALE_TTL_MS = 15 * 60_000L
        const val DEFAULT_RETRY_DELAY_MS = 60L

        internal fun orderPreferIpv4(addresses: List<InetAddress>): List<InetAddress> {
            if (addresses.size <= 1) return addresses
            val v4 = ArrayList<InetAddress>(addresses.size)
            val v6 = ArrayList<InetAddress>(addresses.size)
            val other = ArrayList<InetAddress>()
            for (addr in addresses) {
                when (addr) {
                    is Inet4Address -> v4 += addr
                    is Inet6Address -> v6 += addr
                    else -> other += addr
                }
            }
            return if (v4.isEmpty() || (v6.isEmpty() && other.isEmpty())) addresses else v4 + v6 + other
        }
    }
}

