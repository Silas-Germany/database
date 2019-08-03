package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

@Suppress("UNUSED")
actual class CommonUUID actual constructor(mostSigBits: Long, leastSigBits: Long) {
    actual fun getLeastSignificantBits(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getMostSignificantBits(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual object CommonUUIDObject {
    actual fun randomCommonUUID(): CommonUUID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun nameUUIDFromBytes(name: String): CommonUUID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual val KClass<*>.shortName: String
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.