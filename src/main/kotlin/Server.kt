import spark.Service

fun main() {
    val exchange = Exchange()

    val port = 8080
    val httpService = Service.ignite()
    httpService.port(port)
    httpService.threadPool(350)
    httpService.internalServerError("Error : 500 internal error")

    httpService.get("/addCompany") { request, response ->
        val sid = request.queryParams("sid").toInt()
        val cnt = request.queryParams("cnt").toInt()
        val price = request.queryParams("price").toDouble()
        response.body(exchange.createShare(sid, cnt, price).toString())
        return@get response.body()
    }
    httpService.get("/getShareInfo") { request, response ->
        val sid = request.queryParams("sid").toInt()
        val rate = exchange.getRate(sid) ?: run {
            response.status(400)
            response.body("Invalid sid")
            return@get response.body()
        }
        val cnt = exchange.getCount(sid)
        response.body("$rate;$cnt")
        return@get response.body()
    }
    httpService.get("/trade") { request, response ->
        val uid = request.queryParams("uid").toInt()
        val sid = request.queryParams("sid").toInt()
        val cntToPut = request.queryParams("cntToPut").toInt()
        response.body(exchange.trade(uid, sid, cntToPut).toString())
        return@get response.body()
    }
    httpService.get("/changeRate") { request, response ->
        val sid = request.queryParams("sid").toInt()
        val rateNew = request.queryParams("rateNew").toDouble()
        response.body(exchange.changeRate(sid, rateNew).toString())
        return@get response.body()
    }
    httpService.get("/createUser") { request, response ->
        val uid = request.queryParams("uid").toInt()
        response.body(exchange.createUser(uid).toString())
        return@get response.body()
    }
    httpService.get("/changeAmount") { request, response ->
        val uid = request.queryParams("uid").toInt()
        val incValue = request.queryParams("incValue").toDouble()
        val result = exchange.users[uid]?.changeAmount(incValue) ?: false
        response.body(result.toString())
        return@get response.body()
    }
    httpService.get("/getUserShareCount") { request, response ->
        val uid = request.queryParams("uid").toInt()
        val sid = request.queryParams("sid").toInt()
        exchange.users[uid]?.let { user ->
            response.body(user.getCount(sid).toString())
        } ?: run {
            response.status(400)
            response.body("invalid uid")
        }
        return@get response.body()
    }
    httpService.get("/getUserTotalValue") { request, response ->
        val uid = request.queryParams("uid").toInt()
        val result = exchange.calcTotalValue(uid)
        if (result == null) {
            response.status(400)
            response.body("invalid uid")
        } else {
            response.body(result.toString())
        }
        return@get response.body()
    }

    println("Http server running on port : $port")
}
