import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class User {
    val amount = AtomicReference(0.0)
    val shares = ConcurrentHashMap<Int, Int>()

    fun getCount(sid: Int): Int = shares[sid] ?: 0

    fun changeAmount(incValue: Double): Boolean {
        while (true) {
            val was = amount.get()
            val willBe = was + incValue
            if (willBe < 0) return false
            if (amount.compareAndSet(was, willBe)) return true
        }
    }

    fun trade(sid: Int, cntToSell: Int, price: Double): Boolean =
        shares.myCompute(sid) { cntWas ->
            val cntWillBe = (cntWas ?: 0) - cntToSell
            if (cntWillBe < 0 || !changeAmount(-price)) {
                return@myCompute null
            }
            return@myCompute cntWillBe
        }
}
