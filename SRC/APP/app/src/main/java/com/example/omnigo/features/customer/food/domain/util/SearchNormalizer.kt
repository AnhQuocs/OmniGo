package com.example.omnigo.features.customer.food.domain.util

import java.text.Normalizer

private val DIACRITICS_REGEX = "\\p{InCombiningDiacriticalMarks}+".toRegex()

/** Chuẩn hoá chuỗi để tìm kiếm không phân biệt hoa/thường và dấu tiếng Việt. */
fun String.normalizeForSearch(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(DIACRITICS_REGEX, "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
        .lowercase()
