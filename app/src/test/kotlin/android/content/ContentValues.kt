package android.content

class ContentValues() {
    private val map = mutableMapOf<String, Any?>()
    fun putNull(key: String) {
        map[key] = null
    }
    fun put(key: String, value: String?) {
        map[key] = "$value"
    }
    fun put(key: String, value: Int?) {
        map[key] = "$value"
    }
    fun put(key: String, value: Boolean?) {
        map[key] = "$value"
    }
    fun put(key: String, value: Long?) {
        map[key] = "$value"
    }
    fun put(key: String, value: Float?) {
        map[key] = "$value"
    }
    fun put(key: String, value: ByteArray?) {
        map[key] = "${value?.toList()}"
    }

    override fun toString() = "$map"
}