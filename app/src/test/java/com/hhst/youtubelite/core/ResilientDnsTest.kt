package com.hhst.youtubelite.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class ResilientDnsTest {

    @Test
    fun prefersIpv4BeforeIpv6AndPreservesOrderWithinFamily() {
        val v6a = InetAddress.getByName("2607:f8b0:4005:80b::200e")
        val v4a = InetAddress.getByName("142.250.190.46")
        val v6b = InetAddress.getByName("2607:f8b0:4005:80b::200f")
        val v4b = InetAddress.getByName("142.250.190.78")
        val now = AtomicLong(1_000L)
        val dns = ResilientDns(
            retryDelayMs = 0L,
            nowMs = now::get,
            systemLookup = { listOf(v6a, v4a, v6b, v4b) },
        )

        val resolved = dns.lookup("www.youtube.com")
        assertEquals(listOf(v4a, v4b, v6a, v6b), resolved)
    }

    @Test
    fun retriesTransientUnknownHostException() {
        val v4 = InetAddress.getByName("142.250.190.46")
        val calls = AtomicInteger(0)
        val dns = ResilientDns(
            retryDelayMs = 0L,
            systemLookup = {
                if (calls.incrementAndGet() == 1) throw UnknownHostException("transient")
                listOf(v4)
            },
        )

        assertEquals(listOf(v4), dns.lookup("rr1.googlevideo.com"))
        assertEquals(2, calls.get())
    }

    @Test
    fun fallsBackToRecentCacheWhenDnsFailsLater() {
        val v4 = InetAddress.getByName("142.250.190.46")
        val clock = AtomicLong(1_000L)
        var fail = false
        val dns = ResilientDns(
            freshTtlMs = 20_000L,
            staleTtlMs = 600_000L,
            retryDelayMs = 0L,
            nowMs = clock::get,
            systemLookup = {
                if (fail) throw UnknownHostException("doh hiccup")
                listOf(v4)
            },
        )

        assertEquals(listOf(v4), dns.lookup("youtubei.googleapis.com"))
        clock.set(30_000L) // past fresh TTL, still inside stale TTL
        fail = true
        assertEquals(listOf(v4), dns.lookup("youtubei.googleapis.com"))
    }

    @Test
    fun throwsUnknownHostWhenUncachedAndLookupFails() {
        val dns = ResilientDns(
            retryDelayMs = 0L,
            systemLookup = { throw UnknownHostException("offline") },
        )
        assertThrows(UnknownHostException::class.java) {
            dns.lookup("nonexistent.invalid")
        }
    }
}

