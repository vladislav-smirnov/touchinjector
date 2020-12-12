@file:Suppress("DEPRECATION")

package io.github.airdaydreamers.touchinjector

import io.github.airdaydreamers.touchinjector.data.InjectionType
import io.github.airdaydreamers.touchinjector.data.InjectorType
import io.github.airdaydreamers.touchinjector.impl.PointerInstrumentationInjector
import io.github.airdaydreamers.touchinjector.impl.PointerNativeInjector

class PointerInjectorFactory {
    companion object {
        //old.
        @JvmStatic
        @JvmOverloads
        @Deprecated(
            message = "It was made for Java style. Just show another way. Will be removed",
            replaceWith = ReplaceWith(
                expression = "PointerInjectorFactory.getPointerInjector(InjectionType.)",
                imports = arrayOf("io.github.airdaydreamers.touchinjector.data.InjectionType")
            )
        )
        //TODO: will be removed
        fun getPointerInjector(type: InjectorType, deviceName: String? = null): PointerInjector =
            when (type) {
                InjectorType.NATIVE -> {
                    if (!deviceName.isNullOrEmpty()) {
                        PointerNativeInjector(deviceName)
                    } else {
                        throw IllegalArgumentException("device name should not be null.")
                    }
                }

                InjectorType.INSTRUMENTATION -> PointerInstrumentationInjector()
            }

        @JvmStatic
        @JvmOverloads
        fun getPointerInjector(type: InjectionType = InjectionType.Instrumentation): PointerInjector {
            return when (type) {
                is InjectionType.Instrumentation -> PointerInstrumentationInjector()
                is InjectionType.Native -> PointerNativeInjector(type.deviceName)
            }
        }
    }
}