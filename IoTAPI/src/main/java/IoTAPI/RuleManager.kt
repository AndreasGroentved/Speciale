package IoTAPI

import com.jayway.jsonpath.JsonPath
import hest.HestLexer
import hest.HestParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class RuleManager {


    val threadPoolExecutor = ScheduledThreadPoolExecutor(10)

    companion object {
        val variables = mutableMapOf<String, Expression>("a" to NumberType(31.0), "b" to NumberType(30.0))
    }


    val nordPolMock = "{\n" +
            "  \"PublicationTimeSeries\": {\n" +
            "    \"Currency\": {\n" +
            "      \"_v\": \"EUR\"\n" +
            "    },\n" +
            "    \"MeasureUnitPrice\": {\n" +
            "      \"_v\": \"MWH\"\n" +
            "    },\n" +
            "    \"Period\": {\n" +
            "      \"TimeInterval\": {\n" +
            "        \"_v\": \"2019-03-27T22:00Z/2019-03-28T22:00Z\"\n" +
            "      },\n" +
            "      \"Resolution\": {\n" +
            "        \"_v\": \"PT1H\"\n" +
            "      },\n" +
            "      \"Interval\": [\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"1\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"18.43\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"2\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"18.43\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"3\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"18.43\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"4\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.38\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"5\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.35\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"6\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.41\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"7\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"20.63\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"8\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"23.19\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"9\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"23.28\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"10\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"23.97\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"11\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"25.97\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"12\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"26.02\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"13\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"25.9\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"14\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"26.02\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"15\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"22.16\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"16\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"20.93\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"17\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"21.04\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"18\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"20.02\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"19\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.85\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"20\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.82\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"21\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.42\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"22\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.34\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"23\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.34\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"Pos\": {\n" +
            "            \"_v\": \"24\"\n" +
            "          },\n" +
            "          \"Price\": {\n" +
            "            \"_v\": \"19.41\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}"
    val sampleDsl = "dataset{\n" +
            "    tag = \"NordPol\"\n" +
            "    key = 1234 \n" +
            "    header = \"spotpriser\"\n" +
            "    name = \"Din mor\"\n" +
            "    format = JSON\n" +
            "    var currency = \"\$.PublicationTimeSeries.Period.Interval[0].Price._v\"\n" +
            "}\n" +
            "rule{\n" +
            "    config every 5 min\n" +
            "    var a = 27.0\n" +
            "    var b = 29.0\n" +
            "    run { \n" +
            "        a < b \n" +
            "        device a123 path hest post \"temp\" \"2\" \"hest\" \"3\"\n" +
            "        var c = device a456 path hest2 get \"Pony\" \"b\" \"Kanye\" \"West\"\n" +
            "    }\n" +
            "}\n"
    //val tangleController = TangleController()

    init {

    }


    private fun getDataFromPathAndDataSet(dataSet: String, path: String): String = JsonPath.read(dataSet, path)


    fun updateDsl(/*dslString: String*/) {

        val hParse = HestParser(CommonTokenStream(HestLexer(CharStreams.fromStream(sampleDsl.byteInputStream()))))
        val pWalker = ParseTreeWalker()
        val pHest = ParseDsl()
        pWalker.walk(pHest, hParse.content())
        println(pHest.content.dataSets)


        assignDataSetVariables(pHest.content.dataSets.first())
        pHest.content.rules.forEach {
            println(it.steps.map {
                it.toString() + "\n" +
                        validateExp(it).toString()
            })

        }
        println(variables)
        println("yo igen")
    }


    private fun assignDataSetVariables(dataSet: DataSet) {
        /* val input = tangleController.getNewestMessageSortedByTimeStamp(dataSet.tag, PropertiesLoader.instance.getProperty("energinetPublicKey"))
         val document = Configuration.defaultConfiguration().jsonProvider().parse(input)

         dataSet.vars.forEach {
             val assignTo = it.key
             val value: String = JsonPath.read(document, it.value.toString());
              variables[assignTo] = parseValue(value)
        }*/
        println(dataSet.vars)
        dataSet.vars.forEach {
            println(it.value)
            val value = getDataFromPathAndDataSet(
                nordPolMock, (it.value as? StringType)?.stringVal?.dropLast(1)?.removeRange(0, 1)
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


    fun scheduleTask(rule: Rule) {

        val initialDelay = rule.time.timeDefinition.run { getInitialDelay(fromDate, fromTime) }


        when (rule.time.pattern) {
            "once" -> threadPoolExecutor.schedule({

            }, initialDelay, TimeUnit.MILLISECONDS)
            "every" -> {
                threadPoolExecutor.scheduleAtFixedRate({

                }, initialDelay, getRepeatingInterval(rule.time.unit, rule.time.count), TimeUnit.MILLISECONDS)
            }
            else -> throw NotImplementedError("Pattern not supported")
        }


        threadPoolExecutor.schedule({}, 1000L, TimeUnit.DAYS)
    }

    private fun getRepeatingInterval(unit: String, count: Int) =
        when (unit) {
            "day" -> 24 * 60 * 60 * 1000L
            "hour" -> 60 * 60 * 1000L
            "min" -> 60 * 1000L
            else -> throw NotImplementedError("unsupported repeating time interval")
        } * count

    /*private fun executeTask(rule: Rule) {
        if (validateSteps(rule.steps)) {

        } else {

        }
    }*/

    private fun validateSteps(steps: List<Step>) =
        steps.all {
            when (it) {
                is Expression -> validateExp(it)
                else -> throw NotImplementedError("invalid step type")
            }
            false
        }


    //todo sealed class for datatyper
    private fun validateExp(exp: Expression): Expression = //todo hax løsning, handle grammar precedence i grammar
        when (exp) {
            /*is AndOrExp -> compare(getDataType(exp.value), exp.op, validateExp(exp.exp))
            else -> compare(getDataType(exp!!.value)) //throw NotImplementedError("operator not supported")*/
            is AndOrExp -> if (exp.op == "&&") exp.left and exp.right else if (exp.op == "||") exp.left or exp.right else validateExp(exp.left)
            is CompareExp -> compare(exp.left, exp.op, exp.right)
            is StringType -> exp
            is NumberType -> exp
            is BooleanType -> exp
            is ReferenceType -> evaluateReferenceType(exp)
        }


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


    private fun getInitialDelay(startDate: String?, startTime: String?) =
        Calendar.getInstance().run {
            if (startDate.isNullOrEmpty() && startTime.isNullOrEmpty()) {
                val hour = startTime?.split(":")?.first()?.toInt() ?: throw RuntimeException("impossible")
                val minute = startTime.split(":").last().toInt()
                val dateArr = startDate?.split(":") ?: throw RuntimeException("impossible")
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
} ?: throw RuntimeException("invalid and comparison"))

infix fun Expression.or(other: Expression): Expression = BooleanType((this as? BooleanType)?.let {
    (other as? BooleanType)?.let { this.booleanVal || other.booleanVal }
} ?: throw RuntimeException("invalid and comparison"))


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

