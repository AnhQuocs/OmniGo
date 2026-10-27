package com.example.omnigo.features.customer.food.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchNormalizerTest {

    @Test
    fun `strips vietnamese accents and handles d character`() {
        assertEquals("pho bo ha noi", "Phở Bò Hà Nội".normalizeForSearch())
        assertEquals("banh mi dac biet", "Bánh Mì Đặc Biệt".normalizeForSearch())
        assertEquals("tra sua tran chau", "Trà sữa trân châu".normalizeForSearch())
    }

    @Test
    fun `handles whitespaces and uppercase`() {
        assertEquals("com tam", "   CƠM TẤM   ".normalizeForSearch())
        assertEquals("", "".normalizeForSearch())
    }
}
