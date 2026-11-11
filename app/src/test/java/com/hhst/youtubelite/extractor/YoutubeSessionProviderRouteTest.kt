package com.hhst.youtubelite.extractor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YoutubeSessionProviderRouteTest {

    @Test
    fun transientNullNetworkDoesNotInvalidateActiveRoute() {
        val (sig1, changed1) = YoutubeSessionProvider.resolveRouteSignature("", "103", false, null)
        assertEquals("103:false:null", sig1)
        assertFalse(changed1)

        val (sig2, changed2) = YoutubeSessionProvider.resolveRouteSignature(sig1, null, null, null)
        assertEquals("103:false:null", sig2)
        assertFalse(changed2)

        val (sig3, changed3) = YoutubeSessionProvider.resolveRouteSignature(sig2, "103", false, null)
        assertEquals("103:false:null", sig3)
        assertFalse(changed3)
    }

    @Test
    fun realNetworkOrVpnChangeInvalidatesRoute() {
        val initial = "103:false:null"
        val (vpnSig, vpnChanged) = YoutubeSessionProvider.resolveRouteSignature(initial, "103", true, null)
        assertEquals("103:true:null", vpnSig)
        assertTrue(vpnChanged)

        val (wifiToCellSig, cellChanged) = YoutubeSessionProvider.resolveRouteSignature(vpnSig, "104", false, null)
        assertEquals("104:false:null", wifiToCellSig)
        assertTrue(cellChanged)
    }
}

