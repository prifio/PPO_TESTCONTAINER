import java.util.concurrent.ConcurrentHashMap

class Exchange {
    val rate = ConcurrentHashMap<Int, Double>()
    val count = ConcurrentHashMap<Int, Int>()
    val users = ConcurrentHashMap<Int, User>()

    fun getRate(sid: Int): Double? = rate[sid]

    fun getCount(sid: Int): Int = count[sid] ?: 0

    fun trade(uid: Int, sid: Int, cntToPut: Int): Boolean {
        val price = rate[sid] ?: return false
        val user = users[uid] ?: return false
        if ((cntToPut < 0 && user.amount.get() < -cntToPut * price) ||
            (cntToPut > 0 && user.getCount(sid) < cntToPut)
        ) {
            return false
        }
        return count.myCompute(sid) { cntWas ->
            val cntWillBe = (cntWas ?: 0) + cntToPut
            if (cntWillBe < 0 || !user.trade(sid, cntToPut, -price * cntToPut)) {
                return@myCompute null
            }
            return@myCompute cntWillBe
        }
    }

    fun changeRate(sid: Int, rateNew: Double): Boolean =
        rate.myCompute(sid) { rateWas ->
            if (rateWas == null) null else rateNew
        }

    fun createShare(sid: Int, cnt: Int, price: Double): Boolean =
        rate.myCompute(sid) { rateWas ->
            if (rateWas != null) {
                null
            } else {
                count[sid] = cnt
                price
            }
        }

    fun createUser(uid: Int): Boolean =
        users.myCompute(uid) { user ->
            if (user != null) null else User()
        }

    fun calcTotalValue(uid: Int): Double? = users[uid]?.let { user ->
        user.amount.get() + user.shares
            .map { (sid, cnt) -> rate[sid]!! * cnt }
            .sum()
    }
}
