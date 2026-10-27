package com.example.omnigo.features.customer.food.presentation.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class FoodPriceFormatterTest {

    @Test
    fun `formats positive amounts with thousand separators in US and Vietnam locales`() {
        val usFormatted = FoodPriceFormatter.formatAmount(50000.0, Locale.US)
        assertEquals("50,000", usFormatted)

        val millionFormatted = FoodPriceFormatter.formatAmount(1250000.0, Locale.US)
        assertEquals("1,250,000", millionFormatted)
    }

    @Test
    fun `returns zero as formatted 0`() {
        assertEquals("0", FoodPriceFormatter.formatAmount(0.0, Locale.US))
    }

    @Test
    fun `returns null for invalid amounts`() {
        assertNull(FoodPriceFormatter.formatAmount(null))
        assertNull(FoodPriceFormatter.formatAmount(-1000.0))
        assertNull(FoodPriceFormatter.formatAmount(Double.NaN))
        assertNull(FoodPriceFormatter.formatAmount(Double.POSITIVE_INFINITY))
    }
}
