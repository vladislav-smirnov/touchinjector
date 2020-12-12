package io.github.airdaydreamers.touchinjector.data

sealed class InjectionType {
    object Instrumentation : InjectionType()
    data class Native(val deviceName: String) : InjectionType()
}
