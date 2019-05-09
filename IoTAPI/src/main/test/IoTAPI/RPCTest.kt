package IoTAPI

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.JsonPath
import helpers.StatisticsCollector
import org.junit.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RPCTest {

    val gson = Gson()

    val address = arrayOf("GBQSJYPRQTUGFSNVEYSBGLRE9MCUHIDEXNEWFUPIFRGXPMQWVRBCNDBRLCPFCJURBN9DUDLEFPPITBKMW")

    val transaction = arrayOf("ODGADDIDQC9DXCRCUBTCMDGADBGAVA9BZA9BVAZAAB9BUAABCBWABBYAZABBUABBUAABBBZA9BWAWAZAZA9BZAABYAYAXAYAVAABYAZAZABBWAZAYAVAWAUABBZAYAZAZAUAUA9BVAYABB9BWAUACBWACBUAWAABYACBVAABWAXAXAUAXAYAVAVAYABBABBBYAYACBABCBVAYAVABBABXAABYAZAUACB9B9BXAABCBZAXAUAYAZABBVACBUA9BYAABABCBXAVAXAUAUABB9BXAUA9BBBYABBABWABBBBWAVACBYA9BXAYAWAYAXAAB9B9BCBXAABYAWAYAUAABCBBBWACBGAQAGASCTCJDXCRCTCBCDDTCRCXCUCXCRCPCHDXCCDBDGADBODGAXCSCGADBGAQCVACBUCZAPCUCQCRAVAUCVAPCRAYAYA9BSCRABBBBBBWARAUCVARCUAWAQCUCZAABWAYAABGAQAGASCTCJDXCRCTCACTCGDCDIDFDRCTCGDGADBJCODGAFDTCGDCDIDFDRCTCWBTCHDWCCDSCGDGADBJCODGAADTCHDWCCDSCCCMDDDTCGADBGAQBOBCCGAQAGADDPCFDPCADTCHDTCFDGDGADBODGAGDHDPCHDIDGDGADBGALBCDCD9DTCPCBDGAQDQAGASCTCGDRCFDXCDDHDXCCDBDGADBGAQBTCHDGDEAHDWCTCEARCIDFDFDTCBDHDEAGDHDPCHDIDGDGAQDQAODGAADTCHDWCCDSCCCMDDDTCGADBGAZBYBBCCCGAQAGADDPCFDPCADTCHDTCFDGDGADBODGAGDHDPCHDIDGDGADBGALBCDCD9DTCPCBDGAQDQAGASCTCGDRCFDXCDDHDXCCDBDGADBGACCIDFDBDEASCTCJDXCRCTCEACDBDTACDUCUCGAQDLCQAGADDPCHDWCGADBGACDBDYBUCUCGAQAGAHDXCHD9DTCGADBGACDBDYBUCUCGAQDQAODGAFDTCGDCDIDFDRCTCWBTCHDWCCDSCGDGADBJCODGAADTCHDWCCDSCCCMDDDTCGADBGAQBOBCCGAQAGADDPCFDPCADTCHDTCFDGDGADBODGAHDTCADDDTCFDPCHDIDFDTCGADBGASBBDHDTCVCTCFDGAQDQAGASCTCGDRCFDXCDDHDXCCDBDGADBGAQBTCHDGDEAHDWCTCEAHDPCFDVCTCHDEAHDTCADDDTCFDPCHDIDFDTCEACDUCEAHDWCTCEAWCTCPCHDEADDIDADDDGAQDQAODGAADTCHDWCCDSCCCMDDDTCGADBGAZBYBBCCCGAQAGADDPCFDPCADTCHDTCFDGDGADBODGAHDTCADDDTCFDPCHDIDFDTCGADBGASBBDHDTCVCTCFDGAQDQAGASCTCGDRCFDXCDDHDXCCDBDGADBGAKBSCYCIDGDHDGDEAHDPCFDVCTCHDEAHDTCADDDTCFDPCHDIDFDTCEACDUCEAHDWCTCEAWCTCPCHDEADDIDADDDEAQCMDEASCXCUCUCGAQDLCQAGADDPCHDWCGADBGAHDTCADDDTCFDPCHDIDFDTCGAQAGAHDXCHD9DTCGADBGARBTCPCHDEADDIDADDDEAFDTCGDCDIDFDRCTCGAQDLCQDQDNCNCWBMBYAMBPB9CZB9DICTBEDBDXCMDCBUBFD9BUBWADC9CDDKBCCVBWBBCHCUBLBKDYAKDSBECKB9BCDKBMD9DWA9BHDUALDGCICGDGCWBJDUCFDUCVANDYBLDEDQCBBDD99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999GBQSJYPRQTUGFSNVEYSBGLRE9MCUHIDEXNEWFUPIFRGXPMQWVRBCNDBRLCPFCJURBN9DUDLEFPPITBKMW999999999999999999999999999XICCZBOBMB99999999999999999SEXNOAD99999999999999999999OCJJPQTQCKOFYHHPBUKNBFH9YSY9MBZOEKGIAGJLDPX99QNKGYFYQHXHDEJGPRWEHRQ9IDZKGLYRGR9XWTGWJ9WTJMLEVLWGEXSRCMUOUYHCBMPHWCPXKIFGYURXBKNFNHFH9QLZNEUZ9QJVRX9EIWCUUAOBKUQ999ZCRZLWLMFQGFWEVGUPYFRV9ATFKJMOYWSZWDSSMJAHJFZDXSDIEONVTKFSVOUWBKBEN9MMXOTNAKQP999NBBCZBOBMB99999999999999999TANRPYXNF999999999MMMMMMMMMGFIRKVMINQFEDSRQXBUYAGRWPY9")

    val tags = arrayOf("NBBCZBOBMB")


    @Test
    fun testRPC() {
        sendTransfer()
        sendTransfer()
        getTransfer()
    }

    fun sendTransfer() {
        val tipsToApprove = getTipsToApprove(gson.toJson(mapOf(Pair("command", "getTransactionsToApprove"), Pair("depth", 9))))
        val attachToTangle = attachToTangle(tipsToApprove)
        broadcastTransactions(gson.toJson(mapOf(Pair("command","broadcastTransactions"),attachToTangle)))
        storeTransactions(gson.toJson(mapOf(Pair("command","storeTransactions"),attachToTangle)))
    }

    fun getTransfer() {
        val add = getTransactions(gson.toJson(mapOf(Pair("command", "findTransactions"), Pair("addresses", address))), "findTransactionsAddress")
        getTrytes(add)
        val tag = getTransactions(gson.toJson(mapOf(Pair("command", "findTransactions"), Pair("tags", tags))), "findTransactionsTag")
        getTrytes(tag)
    }

    fun getTipsToApprove(params: String): String {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://52.236.182.23:14265"))
            .POST(HttpRequest.BodyPublishers.ofString(params)).header("X-IOTA-API-Version", "1").build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )

        val parse = JsonPath.parse(httpResponse.body())
        val t = parse.read<String>("$.trunkTransaction")
        val b = parse.read<String>("$.branchTransaction")
        val d = parse.read<Int>("$.duration")
        StatisticsCollector.submitDuration("getTipsToApprove", d.toLong())

        return gson.toJson(
            mapOf(
                Pair("command", "attachToTangle"), Pair("trunkTransaction", t),
                Pair("branchTransaction", b), Pair("minWeightMagnitude", 9), Pair("trytes", transaction)
            )
        )
    }

    fun attachToTangle(params: String): Pair<String, List<String>> {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://52.236.182.23:14265"))
            .POST(HttpRequest.BodyPublishers.ofString(params)).header("X-IOTA-API-Version", "1").build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )
        val parse = JsonPath.parse(httpResponse.body())
        val trytes = parse.read<List<String>>("$.trytes")
        val d = parse.read<Int>("$.duration")
        StatisticsCollector.submitDuration("attachToTangle", d.toLong())

        return Pair("trytes", trytes)
    }

    fun broadcastTransactions(params: String) {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://52.236.182.23:14265"))
            .POST(HttpRequest.BodyPublishers.ofString(params)).header("X-IOTA-API-Version", "1").build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )

        val parse = JsonPath.parse(httpResponse.body())
        val d = parse.read<Int>("$.duration")
        StatisticsCollector.submitDuration("broadcastTransactions", d.toLong())
    }

    fun storeTransactions(params: String) {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://52.236.182.23:14265"))
            .POST(HttpRequest.BodyPublishers.ofString(params)).header("X-IOTA-API-Version", "1").build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )

        val parse = JsonPath.parse(httpResponse.body())
        val d = parse.read<Int>("$.duration")
        StatisticsCollector.submitDuration("storeTransactions", d.toLong())
    }

    fun getTransactions(params: String, type: String): String {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://52.236.182.23:14265"))
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    params
                )
            ).header("X-IOTA-API-Version", "1").build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )
        val parse = JsonPath.parse(httpResponse.body())
        val d = parse.read<Int>("$.duration")
        StatisticsCollector.submitDuration("type", d.toLong())
        val h = parse.read<List<String>>("$.hashes")
        return gson.toJson(mapOf(Pair("command", "getTrytes"), Pair("hashes", h)))
    }

    fun getTrytes(params: String) {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://52.236.182.23:14265"))
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    params
                )
            ).header("X-IOTA-API-Version", "1").build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )

        val parse = JsonPath.parse(httpResponse.body())
        val d = parse.read<Int>("$.duration")
        StatisticsCollector.submitDuration("getTrytes", d.toLong())
    }

    private fun getParameterMap(body: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(body, mapType)
    }
}