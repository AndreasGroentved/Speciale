package IoTAPI

import DeviceManager.DeviceManager
import Tangle.TangleController
import com.google.gson.Gson
import com.jayway.jsonpath.JsonPath
import datatypes.ClientResponse
import datatypes.ErrorResponse
import datatypes.Response
import datatypes.iotdevices.PostMessage
import helpers.LogE
import helpers.LogI
import hest.HestLexer
import hest.HestParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


fun main() {
    val deviceManager = DeviceManager()
    deviceManager.startDiscovery()
    val ruleManager = RuleManager(deviceManager, tangleController = TangleController(""))
    ruleManager.updateDsl(RuleManager.sampleDsl)
}

class RuleManager(private val deviceManager: DeviceManager = DeviceManager(), private val tangleDeviceCallback: ((postMessage: PostMessage, result: (datatypes.Response) -> (Unit)) -> (Unit))? = null, private val gson: Gson = Gson(), private val tangleController: TangleController) {

    private var threadPoolExecutor = ScheduledThreadPoolExecutor(10)
    private lateinit var content: Content
    var parse = ParseDsl()
    val variables = mutableMapOf<String, Expression>()

    companion object {
        val energiNetPath = "\$.result.records[0].CO2Emission"
        val nordPolPath = "\$.PublicationTimeSeries.Period.Interval[0].Price._v"

        val sampleDsl = "dataset{\n" +
                "    ta = \"EN\"\n" +
                "    key = 1234 \n" +
                "    name = \"Din mor\"\n" +
                "    format = JSON\n" +
                "    var co2 = \"\$.result.records[0].CO2Emission\"\n" +
                "}\n" +
                "rule   {\n" +
                "    config every 4 seconds from 2019:03:29 13:03 to 2019:03:29 18:30 \n" +
                "    var a = 27.0\n" +
                "    var c = device hest path temperature get\n" +
                "    run { \n" +
                "        a>c \n" +
                "        device hest path temperature post \"temperature\" 27.0 \n" +
                "    }\n" +
                "}\n"
    }


    private fun getDataFromPathAndDataSet(dataSet: String, path: String): String {
        val a: Any = JsonPath.read(dataSet, path)
        return a.toString()
    }


    fun updateDsl(dslString: String): Response {
        threadPoolExecutor.shutdownNow() //Boom
        threadPoolExecutor = ScheduledThreadPoolExecutor(10)
        val hParse = HestParser(CommonTokenStream(HestLexer(CharStreams.fromStream(dslString.byteInputStream()))))
        val pWalker = ParseTreeWalker()
        parse = ParseDsl()
        hParse.interpreter.predictionMode = PredictionMode.LL_EXACT_AMBIG_DETECTION;
        var error = ""
        hParse.addErrorListener(object : ANTLRErrorListener {
            override fun reportAttemptingFullContext(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int, conflictingAlts: BitSet, configs: ATNConfigSet) {}
            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
                error = msg ?: ""
                LogE("error occurred")
                LogE(msg)
            }

            override fun reportAmbiguity(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int, exact: Boolean, ambigAlts: BitSet, configs: ATNConfigSet) {}
            override fun reportContextSensitivity(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int, prediction: Int, configs: ATNConfigSet) {
            }
        })

        return try {
            println(dslString)
            pWalker.walk(parse, hParse.content())
            if (!error.isBlank()) {
                return ErrorResponse(error)
            }
            content = parse.content
            assignDataSets(content.dataSets)
            content.rules.forEach { scheduleTask(it) }
            ClientResponse("success")
        } catch (e: Exception) {
            e.printStackTrace()
            LogE(e)
            ErrorResponse(e.message ?: "unknown error")
        }
    }


    private fun assignDataSets(dataSets: List<DataSet>): Unit = dataSets.forEach { assignDataSetVariables(it) }

    private fun assignDataSetVariables(dataSet: DataSet) {
        dataSet.vars.forEach {
            val publicKey = if (dataSet.tag == "EN") "energinetPublicKey" else "nordPoolPublicKey"
            val a = tangleController.getNewestBroadcast(dataSet.tag, publicKey)!!
            val value = getDataFromPathAndDataSet(
                a, (it.value as? StringType)?.stringVal?.replace("\"", "")
                    ?: throw RuntimeException("invalid path")
            )
            variables[it.key] = getExpressionFromAnyValue(value)
        }
    }

    private fun getExpressionFromAnyValue(value: String) = when {
        value.toIntOrNull() != null -> NumberType(value.toDouble())
        value.toLowerCase() == "false" || value.toLowerCase() == "true" -> BooleanType(value.toBoolean())
        value.toDoubleOrNull() != null -> NumberType(value.toDouble())
        else -> StringType(value)
    }

    private val map = mutableMapOf<Rule, ScheduledFuture<*>>()


    private fun scheduleTask(rule: Rule) {
        val initialDelay =
            getInitialDelay(rule)
        val lambda = ThreadContext(rule)
        when (rule.time.pattern) {
            "once" -> threadPoolExecutor.schedule(lambda, initialDelay, TimeUnit.MILLISECONDS)
            "every" -> {
                map[rule] = threadPoolExecutor.scheduleAtFixedRate(lambda, initialDelay, getRepeatingInterval(rule.time.unit, rule.time.count), TimeUnit.MILLISECONDS)
            }
            else -> throw NotImplementedError("Pattern not supported")
        }
    }

    private fun getInitialDelay(rule: Rule) =
        ((getMillisOfTime(rule.time.timeDefinition.fromDate, rule.time.timeDefinition.fromTime) ?: 0)
                - System.currentTimeMillis()).run { if (this < 0) 0 else this }


    private inner class ThreadContext(private val rule: Rule) : Runnable {
        override fun run() {
            LogI("run rule")
            LogI(rule)
            try {
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
            map.remove(rule)
            return
        }

        assignDataSets(content.dataSets)
        parse.varMap.forEach { (t, u) ->
            val toAssign = validateExp(u)
            variables[t] = toAssign
        }
        println(variables)


        var numberOfAssignments = 0
        rule.assignmentsFromDevice.forEach { (first, second) ->
            run(second) { expression ->
                if (expression != null) variables[first] = expression
                else {
                    LogE("No value from device")
                    return@run
                }
                numberOfAssignments++
                if (numberOfAssignments == rule.assignmentsFromDevice.size) {
                    val validatePreCondition = validateSteps(rule.steps)
                    if (validatePreCondition) {
                        LogI("condition valid")
                        rule.deviceCalls.forEach { run(it, null) }
                    } else {
                        LogI("condition not valid")
                    }
                }
            }
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

    private fun run(outPut: OutPut, callback: ((Expression?) -> (Unit))?) {
        val postMessage = PostMessage(deviceID = outPut.deviceID, params = outPut.params.toMap(), path = outPut.path, type = outPut.method.toLowerCase())
        val isTangleDevice = deviceManager.getDevice(outPut.deviceID) == null

        if (isTangleDevice) {
            try {
                tangleDeviceCallback?.let {
                    it(postMessage) {
                        callback?.invoke(runResponseToExpression(it.let { response -> response as? ClientResponse }!!.result.toString()))
                    }
                }
            } catch (e: Exception) {
                LogE(e.message)
                callback?.invoke(null)
            }
        } else {
            LogI("getting")
            val resp = if (postMessage.type.toLowerCase() == "get") deviceManager.get(postMessage) else deviceManager.post(postMessage)
            callback?.invoke(
                if (resp is ErrorResponse) null
                else runResponseToExpression((resp as ClientResponse).result.toString())
            )
        }
    }

    private fun runResponseToExpression(value: String?): Expression? = value?.let {
        getExpressionFromAnyValue(value)
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
                else -> BooleanType(true)
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

    private fun evaluateReferenceType(ref: ReferenceType): Expression = variables[ref.referenceName]!!


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
}

