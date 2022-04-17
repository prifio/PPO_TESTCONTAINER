import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URL
import kotlin.test.Test

@Testcontainers
class Test {
    private lateinit var address: String

    @Container
    var container = GenericContainer("exchange-container")
        .withExposedPorts(8080)

    @BeforeEach
    fun setUp() {
        address = "http://127.0.0.1:${container.firstMappedPort}"
    }

    private fun addCompany(sid: Int, cnt: Int, price: Int): Boolean =
        URL("$address/addCompany?sid=$sid&cnt=$cnt&price=$price").readText().toBoolean()

    private fun getShareInfo(sid: Int): Pair<Double, Int> =
        URL("$address/getShareInfo?sid=$sid").readText().split(';').let { (a, b) ->
            a.toDouble() to b.toInt()
        }

    private fun trade(uid: Int, sid: Int, cntToPut: Int): Boolean =
        URL("$address/trade?uid=$uid&sid=$sid&cntToPut=$cntToPut").readText().toBoolean()

    private fun changeRate(sid: Int, rateNew: Int): Boolean =
        URL("$address/changeRate?sid=$sid&rateNew=$rateNew").readText().toBoolean()

    private fun createUser(uid: Int): Boolean =
        URL("$address/createUser?uid=$uid").readText().toBoolean()

    private fun changeAmount(uid: Int, incValue: Int): Boolean =
        URL("$address/changeAmount?uid=$uid&incValue=$incValue").readText().toBoolean()

    private fun getUserShareCount(uid: Int, sid: Int): Int =
        URL("$address/getUserShareCount?uid=$uid&sid=$sid").readText().toInt()

    private fun getUserTotalValue(uid: Int): Double =
        URL("$address/getUserTotalValue?uid=$uid").readText().toDouble()

    @Test
    fun exchangeTest() {
        Assertions.assertTrue(addCompany(1, 3, 6))
        Assertions.assertFalse(addCompany(1, 2, 5))
        Assertions.assertEquals(6.0 to 3, getShareInfo(1))

        Assertions.assertTrue(changeRate(1, 7))
        Assertions.assertFalse(changeRate(2, 8))
        Assertions.assertTrue(addCompany(2, 4, 9))
        Assertions.assertEquals(7.0 to 3, getShareInfo(1))
        Assertions.assertEquals(9.0 to 4, getShareInfo(2))
    }

    @Test
    fun tradeTest() {
        Assertions.assertTrue(addCompany(11, 10, 100))
        Assertions.assertTrue(createUser(11))
        Assertions.assertTrue(changeAmount(11, 200))
        Assertions.assertFalse(trade(11, 11, -3))
        Assertions.assertTrue(trade(11, 11, -2))
        Assertions.assertFalse(trade(11, 11, 3))
        Assertions.assertTrue(trade(11, 11, 1))
        Assertions.assertEquals(1, getUserShareCount(11, 11))
        Assertions.assertEquals(200.0, getUserTotalValue(11))
        Assertions.assertEquals(9, getShareInfo(11).second)

        Assertions.assertTrue(changeAmount(11, 10000))
        Assertions.assertFalse(trade(11, 11, -10))
        Assertions.assertTrue(trade(11, 11, -8))
        Assertions.assertEquals(1, getShareInfo(11).second)
        Assertions.assertEquals(10200.0, getUserTotalValue(11))
    }

    @Test
    fun userTest() {
        Assertions.assertTrue(createUser(21))
        Assertions.assertFalse(createUser(21))
        Assertions.assertTrue(changeAmount(21, 10000))
        Assertions.assertFalse(changeAmount(21, -10001))
        Assertions.assertTrue(changeAmount(21, -10000))
        Assertions.assertEquals(0.0, getUserTotalValue(21))

        Assertions.assertEquals(0, getUserShareCount(21, 21))
        Assertions.assertTrue(addCompany(21, 10000, 100))
        Assertions.assertEquals(0, getUserShareCount(21, 21))
        Assertions.assertTrue(changeAmount(21, 10000))
        Assertions.assertTrue(trade(21, 21, -10))
        Assertions.assertEquals(10, getUserShareCount(21, 21))
        Assertions.assertTrue(trade(21, 21, 5))
        Assertions.assertEquals(5, getUserShareCount(21, 21))
        Assertions.assertEquals(10000.0, getUserTotalValue(21))
        Assertions.assertTrue(changeRate(21, 200))
        Assertions.assertEquals(10500.0, getUserTotalValue(21))
    }
}
