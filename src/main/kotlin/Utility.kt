import java.util.concurrent.ConcurrentMap

fun <K, V> ConcurrentMap<K, V>.myCompute(k: K, updater: (V?) -> V?): Boolean {
    var status = true
    compute(k) { _, v ->
        val newValue = updater(v)
        if (newValue == null) {
            status = false
            return@compute v
        }
        return@compute newValue
    }
    return status
}
