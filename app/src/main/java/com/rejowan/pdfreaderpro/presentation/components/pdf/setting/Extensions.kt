package com.bhuvaneshw.pdf.setting

internal fun <T : Enum<T>> PdfSettingsSaver.save(key: String, value: T) {
    save(key, value.name)
}

internal inline fun <reified T : Enum<T>> PdfSettingsSaver.getEnum(key: String, default: T): T {
    return enumValueOf<T>(getString(key, default.name))
}
