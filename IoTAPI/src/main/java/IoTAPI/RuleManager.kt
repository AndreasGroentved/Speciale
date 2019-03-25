package IoTAPI

import DeviceManager.DeviceManager
import com.google.gson.Gson
import com.jayway.jsonpath.JsonPath
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.ResponseToClient
import hest.HestLexer
import hest.HestParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class RuleManager(private val deviceManager: DeviceManager = DeviceManager(), private val gson: Gson = Gson()) {


    private var threadPoolExecutor = ScheduledThreadPoolExecutor(10)
    private lateinit var content: Content


    companion object {
        val variables = mutableMapOf<String, Expression>()
        val sampleDsl = "dataset{\n" +
                "    tag = \"NordPol\"\n" +
                "    key = 1234 \n" +
                "    name = \"Din mor\"\n" +
                "    format = JSON\n" +
                "    var currency = \"\$.PublicationTimeSeries.Period.Interval[0].Price._v\"\n" +
                "}\n" +
                "rule{\n" +
                "    config every 5 min\n" +
                "    var a = 27.0\n" +
                "    var b = 29.0\n" +
                "    var c = device a456 path hest2 get \"Pony\" \"b\" \"Kanye\" \"West\"\n" +
                "    run { \n" +
                "        a < b \n" +
                "        device a123 path hest post \"temp\" \"2\" \"hest\" \"3\"\n" +
                "    }\n" +
                "}\n"
        val nordPolMock = "{\n  \"PublicationTimeSeries\": {\n    \"Currency\": {\n      \"_v\": \"EUR\"\n    },\n    \"MeasureUnitPrice\": {\n      \"_v\": \"MWH\"\n    },\n    \"Period\": {\n      \"TimeInterval\": {\n        \"_v\": \"2019-03-27T22:00Z/2019-03-28T22:00Z\"\n      },\n      \"Resolution\": {\n        \"_v\": \"PT1H\"\n      },\n      \"Interval\": [\n        {\n          \"Pos\": {\n            \"_v\": \"1\"\n          },\n          \"Price\": {\n            \"_v\": \"18.43\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"2\"\n          },\n          \"Price\": {\n            \"_v\": \"18.43\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"3\"\n          },\n          \"Price\": {\n            \"_v\": \"18.43\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"4\"\n          },\n          \"Price\": {\n            \"_v\": \"19.38\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"5\"\n          },\n          \"Price\": {\n            \"_v\": \"19.35\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"6\"\n          },\n          \"Price\": {\n            \"_v\": \"19.41\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"7\"\n          },\n          \"Price\": {\n            \"_v\": \"20.63\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"8\"\n          },\n          \"Price\": {\n            \"_v\": \"23.19\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"9\"\n          },\n          \"Price\": {\n            \"_v\": \"23.28\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"10\"\n          },\n          \"Price\": {\n            \"_v\": \"23.97\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"11\"\n          },\n          \"Price\": {\n            \"_v\": \"25.97\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"12\"\n          },\n          \"Price\": {\n            \"_v\": \"26.02\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"13\"\n          },\n          \"Price\": {\n            \"_v\": \"25.9\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"14\"\n          },\n          \"Price\": {\n            \"_v\": \"26.02\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"15\"\n          },\n          \"Price\": {\n            \"_v\": \"22.16\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"16\"\n          },\n          \"Price\": {\n            \"_v\": \"20.93\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"17\"\n          },\n          \"Price\": {\n            \"_v\": \"21.04\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"18\"\n          },\n          \"Price\": {\n            \"_v\": \"20.02\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"19\"\n          },\n          \"Price\": {\n            \"_v\": \"19.85\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"20\"\n          },\n          \"Price\": {\n            \"_v\": \"19.82\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"21\"\n          },\n          \"Price\": {\n            \"_v\": \"19.42\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"22\"\n          },\n          \"Price\": {\n            \"_v\": \"19.34\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"23\"\n          },\n          \"Price\": {\n            \"_v\": \"19.34\"\n          }\n        },\n        {\n          \"Pos\": {\n            \"_v\": \"24\"\n          },\n          \"Price\": {\n            \"_v\": \"19.41\"\n          }\n        }\n      ]\n    }\n  }\n}"
    }


    init {

    }


    private fun getDataFromPathAndDataSet(dataSet: String, path: String): String = JsonPath.read(dataSet, path)


    fun updateDsl(dslString: String) { //TODO for nu, er det bare forkast gammel DSL
        threadPoolExecutor.shutdownNow() //Boom
        threadPoolExecutor = ScheduledThreadPoolExecutor(10)
        val hParse = HestParser(CommonTokenStream(HestLexer(CharStreams.fromStream(dslString.byteInputStream()))))
        val pWalker = ParseTreeWalker()
        val parse = ParseDsl()
        pWalker.walk(parse, hParse.content())
        content = parse.content
        println(content)
        content.rules.forEach { scheduleTask(it) }

    }


    private fun assignDataSets(dataSets: List<DataSet>) = dataSets.forEach { assignDataSetVariables(it) }

    private fun assignDataSetVariables(dataSet: DataSet) {
        dataSet.vars.forEach {
            val value = getDataFromPathAndDataSet(
                nordPolMock, (it.value as? StringType)?.stringVal/*?.dropLast(1)?.removeRange(0, 1)*/
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


    private fun scheduleTask(rule: Rule) {
        val initialDelay = rule.time.timeDefinition.run { getMillisOfTime(fromDate, fromTime) } - System.currentTimeMillis().run { if (this < 0) 0 else this }
        val lambda = ThreadContext(rule)
        when (rule.time.pattern) {
            "once" -> threadPoolExecutor.schedule(lambda, initialDelay, TimeUnit.MILLISECONDS)
            "every" -> threadPoolExecutor.scheduleAtFixedRate(lambda, initialDelay, getRepeatingInterval(rule.time.unit, rule.time.count), TimeUnit.MILLISECONDS)
            else -> throw NotImplementedError("Pattern not supported")
        }
    }

    private inner class ThreadContext(private val rule: Rule) : Runnable {
        override fun run() = validateConditionAndRun(rule, this)
    }


    private fun validateConditionAndRun(rule: Rule, runnable: Runnable) {
        if (!isInTimeInterval(rule)) {
            threadPoolExecutor.remove(runnable)
            return
        }
        assignDataSets(content.dataSets)
        rule.dassignmentsFromDevice.forEach { variables[it.first] = run(it.second) }
        val validatePreCondition = validateSteps(rule.steps)
        if (validatePreCondition) {
            rule.deviceCalls.forEach { run(it) }
        }
    }

    private fun isInTimeInterval(rule: Rule): Boolean {
        val timeInterval = rule.time
        val timeDef = timeInterval.timeDefinition
        getMillisOfTime(timeDef.fromDate, timeDef.fromTime)
        val endMili = getMillisOfTime(timeDef.toDate, timeDef.toTime)
        val currentMili = System.currentTimeMillis()
        return when {
            (timeInterval.pattern == "once") -> true
            currentMili < endMili -> true
            currentMili > endMili -> false
            else -> false
        }
    }

    private fun run(outPut: OutPut): Expression = runResponseToExpression(
        if (outPut.method.toLowerCase() == "post") {
            deviceManager.post(outPut.deviceID, outPut.path, gson.toJson(outPut.params))
        } else {
            deviceManager.get(PostMessage(deviceID = outPut.deviceID, path = outPut.path))
        }
    )


    private fun runResponseToExpression(value: String): Expression =
        getExpressionFromAnyValue(gson.fromJson<ResponseToClient>(value, ResponseToClient::class.java).result)

    private fun getRepeatingInterval(unit: String, count: Int) =
        when (unit) {
            "day" -> 24 * 60 * 60 * 1000L
            "hour" -> 60 * 60 * 1000L
            "min" -> 60 * 1000L
            "second" -> 1000L
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


    private fun getMillisOfTime(date: String?, time: String?) =
        Calendar.getInstance().run {
            println("date $date time $time")
            if (!date.isNullOrEmpty() && !time.isNullOrEmpty()) {
                val hour = time.split(":").first().toInt()
                val minute = time.split(":").last().toInt()
                val dateArr = date.split(":")
                val year = dateArr[0].toInt()
                val month = dateArr[1].toInt()
                val day = dateArr[2].toInt()
                set(year, month, day, hour, minute, 0)
                timeInMillis
            } else 0L
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
