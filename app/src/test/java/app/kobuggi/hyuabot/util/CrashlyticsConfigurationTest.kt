package app.kobuggi.hyuabot.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashlyticsConfigurationTest {
    @Test
    fun `recognizes common Android emulator fingerprints`() {
        assertTrue(isEmulator(build(model = "sdk_gphone64_x86_64", hardware = "ranchu")))
        assertTrue(isEmulator(build(fingerprint = "generic_x86/sdk/generic_x86:35/XYZ")))
        assertTrue(isEmulator(build(manufacturer = "Genymotion")))
    }

    @Test
    fun `does not classify a physical device as an emulator`() {
        assertFalse(
            isEmulator(
                build(
                    fingerprint = "samsung/e1sxxx/SM-G990:14/UP1A.231005.007/123456:user/release-keys",
                    model = "SM-G990",
                    manufacturer = "samsung",
                    brand = "samsung",
                    device = "e1sxxx",
                    product = "e1sxxx",
                    hardware = "exynos",
                )
            )
        )
    }

    private fun build(
        fingerprint: String = "physical/device/release-keys",
        model: String = "Physical Device",
        manufacturer: String = "Acme",
        brand: String = "acme",
        device: String = "device",
        product: String = "physical",
        hardware: String = "device",
    ) = EmulatorBuild(fingerprint, model, manufacturer, brand, device, product, hardware)
}
