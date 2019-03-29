package IoTAPI

import DeviceManager.DeviceManager
import Tangle.TangleController
import com.google.gson.Gson
import com.jayway.jsonpath.JsonPath
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.ResponseToClient
import helpers.LogE
import helpers.LogI
import hest.HestLexer
import hest.HestParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


fun main() {
    val deviceManager = DeviceManager()
    deviceManager.startDiscovery()
    val ruleManager = RuleManager(deviceManager)
    ruleManager.updateDsl(RuleManager.sampleDsl)

}

class RuleManager(private val deviceManager: DeviceManager = DeviceManager(), private val gson: Gson = Gson()) {


    private var threadPoolExecutor = ScheduledThreadPoolExecutor(10)
    private lateinit var content: Content
    private val tangleController = TangleController()


    companion object {
        val variables = mutableMapOf<String, Expression>()

        val energiNetPath = "\$.result.records[0].CO2Emission"
        val nordPolPath = "\$.PublicationTimeSeries.Period.Interval[0].Price._v"

        val sampleDsl = "dataset{\n" +
                "    tag = \"EN\"\n" +
                "    key = 1234 \n" +
                "    name = \"Din mor\"\n" +
                "    format = JSON\n" +
                "    var co2 = \"\$.result.records[0].CO2Emission\"\n" +
                "}\n" +
                "rule{\n" +
                "    config every 4 seconds from 2019:03:29 13:03 to 2019:03:29 18:30 \n" +
                "    var a = 27.0\n" +
                "    var c = device hest path temperature get\n" +
                "    run { \n" +
                "        a>c \n" +
                "        device hest path temperature post \"temperature\" 27.0 \n" +
                "    }\n" +
                "}\n"

        val nordPolMock = "{\n  \"PublicationTimeSeries\": {\n    \"Currency\": {\n      \"_v\": \"EUR\"\n    },\n    \"MeasureUnitPrice\": {\n      \"_v\": \"MWH\"\n    },\n    \"Period\": {\n      \"TimeInterval\": {\n        \"_v\": \"2019-03-27T22:00Z/2019-03-28T22:00Z\"\n      },\n      \"Resolution\": {\n        \"_v\": \"PT1H\"\n      },\n      \"Interval\": [\n        {\n          \"Pos\": {\n            \"_v\": \"1\"\n          },\n          \"Price\": {\n            \"_v\": \"18.43\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"2\"\n          },\n          \"Price\": {\n            \"_v\": \"18.43\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"3\"\n          },\n          \"Price\": {\n            \"_v\": \"18.43\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"4\"\n          },\n          \"Price\": {\n            \"_v\": \"19.38\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"5\"\n          },\n          \"Price\": {\n            \"_v\": \"19.35\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"6\"\n          },\n          \"Price\": {\n            \"_v\": \"19.41\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"7\"\n          },\n          \"Price\": {\n            \"_v\": \"20.63\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"8\"\n          },\n          \"Price\": {\n            \"_v\": \"23.19\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"9\"\n          },\n          \"Price\": {\n            \"_v\": \"23.28\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"10\"\n          },\n          \"Price\": {\n            \"_v\": \"23.97\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"11\"\n          },\n          \"Price\": {\n            \"_v\": \"25.97\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"12\"\n          },\n          \"Price\": {\n            \"_v\": \"26.02\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"13\"\n          },\n          \"Price\": {\n            \"_v\": \"25.9\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"14\"\n          },\n          \"Price\": {\n            \"_v\": \"26.02\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"15\"\n          },\n          \"Price\": {\n            \"_v\": \"22.16\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"16\"\n          },\n          \"Price\": {\n            \"_v\": \"20.93\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"17\"\n          },\n          \"Price\": {\n            \"_v\": \"21.04\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"18\"\n          },\n          \"Price\": {\n            \"_v\": \"20.02\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"19\"\n          },\n          \"Price\": {\n            \"_v\": \"19.85\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"20\"\n          },\n          \"Price\": {\n            \"_v\": \"19.82\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"21\"\n          },\n          \"Price\": {\n            \"_v\": \"19.42\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"22\"\n          },\n          \"Price\": {\n            \"_v\": \"19.34\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"23\"\n          },\n          \"Price\": {\n            \"_v\": \"19.34\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"24\"\n          },\n          \"Price\": {\n            \"_v\": \"19.41\"\n          }\n        }\n      ]\n    }\n  }\n}"
    }


    init {

    }


    private fun getDataFromPathAndDataSet(dataSet: String, path: String): String {
        val a: Any = JsonPath.read(dataSet, path)
        return a.toString()
    }

    var parse = ParseDsl()

    fun updateDsl(dslString: String) { //TODO for nu, er det bare forkast gammel DSL
        threadPoolExecutor.shutdownNow() //Boom
        threadPoolExecutor = ScheduledThreadPoolExecutor(10)
        val hParse = HestParser(CommonTokenStream(HestLexer(CharStreams.fromStream(dslString.byteInputStream()))))
        val pWalker = ParseTreeWalker()
        parse = ParseDsl()
        pWalker.walk(parse, hParse.content())
        content = parse.content
        println(content)
        assignDataSets(content.dataSets)
        content.rules.forEach { scheduleTask(it) }

    }


    private fun assignDataSets(dataSets: List<DataSet>) = dataSets.forEach { assignDataSetVariables(it) }

    private fun assignDataSetVariables(dataSet: DataSet) {
        dataSet.vars.forEach {
            val publicKey = if (dataSet.tag == "EN") "energinetPublicKey" else "nordPoolPublicKey"
            println("yo non")
            println(dataSet.tag)
            val a = tangleController.getNewestBroadcast(dataSet.tag, publicKey)
            println(tangleController.getASCIIFromTrytes(a!!.signatureFragments)!!.substringBefore("__"))

            println((it.value as? StringType)?.stringVal)
            val value = getDataFromPathAndDataSet(
                tangleController.getASCIIFromTrytes(a.signatureFragments)!!.substringBefore("__"), (it.value as? StringType)?.stringVal?.replace("\"", "")/*?.dropLast(1)?.removeRange(0, 1)*/
                    ?: throw RuntimeException("invalid path")
            )
            variables[it.key] = getExpressionFromAnyValue(value)
        }
    }

    private fun getExpressionFromAnyValue(value: String) = when {
        value.toIntOrNull() != null -> NumberType(value.toDouble())
        value == "false" || value == "true" -> BooleanType(value.toBoolean())
        value.toDoubleOrNull() != null -> NumberType(value.toDouble())
        else -> StringType(value)
    }

    private val map = mutableMapOf<Rule, ScheduledFuture<Any>>()


    private fun scheduleTask(rule: Rule) {
        val initialDelay =
            ((getMillisOfTime(rule.time.timeDefinition.fromDate, rule.time.timeDefinition.fromTime) ?: 0)
                    - System.currentTimeMillis()).run { if (this < 0) 0 else this }
        val lambda = ThreadContext(rule)
        when (rule.time.pattern) {
            "once" -> threadPoolExecutor.schedule(lambda, initialDelay, TimeUnit.MILLISECONDS)
            "every" -> {
                val future = threadPoolExecutor.scheduleAtFixedRate(lambda, initialDelay, getRepeatingInterval(rule.time.unit, rule.time.count), TimeUnit.MILLISECONDS)
                map[rule] = future as ScheduledFuture<Any>
            }
            else -> throw NotImplementedError("Pattern not supported")
        }
    }


    private inner class ThreadContext(private val rule: Rule) : Runnable {
        override fun run() {
            LogI("run")
            try {
                LogE(RuleManager.variables)
                validateConditionAndRun(rule)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun validateConditionAndRun(rule: Rule) {
        if (!isInTimeInterval(rule)) {
            LogI("no longer in time interval")
            map[rule]?.cancel(true)
            return
        }
        assignDataSets(content.dataSets)
        parse.varMap.forEach { t, u ->
            val toAssign = validateExp(u)
            variables[t] = toAssign
        }
        println(variables)

        rule.assignmentsFromDevice.forEach { p ->
            val toAssign = run(p.second)
            if (toAssign != null) variables[p.first] = toAssign
            else {
                LogE("No value from device")
                return
            }
        }
        val validatePreCondition = validateSteps(rule.steps)
        if (validatePreCondition) {
            LogI("condition valid")
            rule.deviceCalls.forEach { run(it) }
        } else {
            LogI("condition not valid")
        }
    }

    private fun isInTimeInterval(rule: Rule): Boolean {
        val timeInterval = rule.time
        val timeDef = timeInterval.timeDefinition
        getMillisOfTime(timeDef.fromDate, timeDef.fromTime)
        val endMili = getMillisOfTime(timeDef.toDate, timeDef.toTime)
        val currentMili = System.currentTimeMillis()
        return when {
            endMili == null -> true
            timeInterval.pattern == "once" -> true
            currentMili < endMili -> true
            currentMili > endMili -> false
            else -> false
        }
    }

    private fun run(outPut: OutPut): Expression? = runResponseToExpression(
        //TODO set path parametre?
        if (outPut.method.toLowerCase() == "post") {
            LogI("posting")
            deviceManager.post(outPut.deviceID, outPut.path, gson.toJson(PostMessage(params = outPut.params.toMap())))
        } else {
            LogI("getting")
            val resp = deviceManager.get(PostMessage(deviceID = outPut.deviceID, path = outPut.path))
            if (resp.contains("error")) null
            else resp
        }
    )


    private fun runResponseToExpression(value: String?): Expression? = value?.let {
        getExpressionFromAnyValue(gson.fromJson(value, ResponseToClient::class.java).result)
    }

    private fun getRepeatingInterval(unit: String, count: Int) =
        when (unit) {
            "day" -> 24 * 60 * 60 * 1000L
            "hour" -> 60 * 60 * 1000L
            "min" -> 60 * 1000L
            "seconds" -> 1000L
            else -> throw NotImplementedError("unsupported repeating time interval")
        } * count


    private fun validateExp(exp: Expression): Expression = //todo handle grammar precedence i grammar
        when (exp) {
            is AndOrExp -> if (exp.op == "&&") exp.left and exp.right else if (exp.op == "||") exp.left or exp.right else validateExp(exp.left)
            is CompareExp -> compare(exp.left, exp.op, exp.right)
            is StringType -> exp
            is NumberType -> exp
            is BooleanType -> exp
            is ReferenceType -> evaluateReferenceType(exp)
        }


    private fun validateSteps(steps: List<Expression>) =
        steps.all { (validateExp(it) as? BooleanType)?.booleanVal ?: throw RuntimeException("Invalid condition") }


    private fun compare(left: Expression, op: String? = null, right: Expression? = null): Expression {
        return if (op != null) {
            requireNotNull(right)
            when (op) {
                "<" -> BooleanType(left < right)
                ">" -> BooleanType(left > right)
                "<=" -> BooleanType(left <= right)
                ">=" -> BooleanType(left >= right)
                "==" -> BooleanType(left == right)
                "!=" -> BooleanType(left != right)
                else -> BooleanType(true) //yo, yo, yo
            }
        } else {
            left
        }
    }


    private fun getMillisOfTime(date: String?, time: String?): Long? =
        Calendar.getInstance().run {
            if (!date.isNullOrEmpty() && !time.isNullOrEmpty()) {
                val hour = time.split(":").first().toInt()
                val minute = time.split(":").last().toInt()
                val dateArr = date.split(":")
                val year = dateArr[0].toInt()
                val month = dateArr[1].toInt()
                val day = dateArr[2].toInt()
                set(year, month - 1, day, hour, minute, 0)
                timeInMillis
            } else null
        }
}

private fun evaluateReferenceType(ref: ReferenceType): Expression = RuleManager.variables[ref.referenceName]!!


infix fun Expression.and(other: Expression): Expression = BooleanType((this as? BooleanType)?.let {
    (other as? BooleanType)?.let { this.booleanVal && other.booleanVal }
} ?: throw RuntimeException("invalid \"and\" comparison"))

infix fun Expression.or(other: Expression): Expression = BooleanType((this as? BooleanType)?.let {
    (other as? BooleanType)?.let { this.booleanVal || other.booleanVal }
} ?: throw RuntimeException("invalid \"or\" comparison"))


operator fun Expression.compareTo(other: Expression): Int = when {
    this is BooleanType && other is BooleanType -> this.booleanVal.compareTo(other.booleanVal)
    this is ReferenceType -> evaluateReferenceType(this).compareTo(other)
    other is ReferenceType -> this.compareTo(evaluateReferenceType(other))
    this is NumberType && other is NumberType -> this.doubleVal.compareTo(other.doubleVal)
    else -> {
        if (this as? StringType != null && other as? StringType != null) {
            this.stringVal.compareTo(other.stringVal)
        } else throw RuntimeException("invalid comparison")

    }
}
