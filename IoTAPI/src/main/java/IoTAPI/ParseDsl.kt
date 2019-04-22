package IoTAPI

import hest.HestParser
import hest.HestParserBaseListener
import org.antlr.v4.runtime.tree.ErrorNode

data class TimeDefinition(var fromDate: String? = null, var fromTime: String? = null, var toDate: String? = null, var toTime: String? = null)
data class Time(var pattern: String = "once", var count: Int = 0, var unit: String = "seconds", var timeDefinition: TimeDefinition = TimeDefinition())
data class Rule(var steps: MutableList<Expression> = mutableListOf(), var time: Time = Time(), var assignmentsFromDevice: MutableList<Pair<String, OutPut>> = mutableListOf(), var deviceCalls: MutableList<OutPut> = mutableListOf())
data class Content(val rules: MutableList<Rule> = mutableListOf(), val dataSets: MutableList<DataSet> = mutableListOf())
data class DataSet(var name: String = "", var tag: String = "", var format: String = "", var vars: MutableMap<String, Expression> = mutableMapOf(), var publicKey: String = "")
data class OutPut(var deviceID: String = "", var path: String = "", var method: String = "", var params: MutableList<Pair<String, String>> = mutableListOf())

class ParseDsl : HestParserBaseListener() {

    val varMap = mutableMapOf<String, Expression>()
    val currentDataSetVarMap = mutableMapOf<String, Expression>()
    var currentDataSet = DataSet()
    var currentRule = Rule()
    val dataSetMap = mutableMapOf<String, DataSet>()
    val rules = mutableListOf<Rule>()
    var currentTime = Time()
    lateinit var content: Content
    var currentOutPut = OutPut()


    override fun exitContent(ctx: HestParser.ContentContext) {
        content = Content(rules, dataSetMap.values.toMutableList())
    }

    override fun enterRulee(ctx: HestParser.RuleeContext?) {
        currentRule = Rule()
    }

    override fun exitRulee(ctx: HestParser.RuleeContext?) {
        rules.add(currentRule)
    }

    override fun enterTime(ctx: HestParser.TimeContext) {
        currentTime = Time()
        currentRule.time = currentTime
        currentTime.pattern = ctx.EVERY()?.text ?: ctx.ONCE()?.text
                ?: throw RuntimeException("time interval not supported")
    }


    override fun enterTimeDefinition(ctx: HestParser.TimeDefinitionContext?) {
        currentTime.count = ctx!!.INTLIT().text.toInt()
        currentTime.unit = ctx.unit.text
    }

    override fun enterInterval(ctx: HestParser.IntervalContext) {
        currentTime.timeDefinition.run { fromTime = ctx.fromTime.text; fromDate = ctx.fromDate.text; toTime = ctx.toTime.text; toDate = ctx.toDate.text }
    }

    override fun enterOutput(ctx: HestParser.OutputContext) {
        currentOutPut = OutPut()
        currentOutPut.method = ctx.method.text
    }

    override fun exitOutput(ctx: HestParser.OutputContext) {
        if (ctx.parent !is HestParser.VarpathContext)
            currentRule.deviceCalls.add(currentOutPut)
    }

    override fun enterDevice(ctx: HestParser.DeviceContext) {
        println(ctx.deviceName.text)
        currentOutPut.deviceID = ctx.deviceName.text.replace("\"", "")
    }

    override fun enterPath(ctx: HestParser.PathContext) {
        currentOutPut.path = ctx.ID().text
    }

    override fun enterParameter(ctx: HestParser.ParameterContext) {
        currentOutPut.params.add(Pair(ctx.parName.text.replace("\"", ""), ctx.parValue.text.replace("\"", "")))
    }

    override fun exitVarpath(ctx: HestParser.VarpathContext) {
        currentRule.assignmentsFromDevice.add(Pair(ctx.varName.text, currentOutPut))
    }

    override fun enterDataset(ctx: HestParser.DatasetContext) {
        currentDataSet = DataSet()
    }

    override fun exitDataset(ctx: HestParser.DatasetContext) {
        currentDataSet.vars = currentDataSetVarMap
        dataSetMap[currentDataSet.name] = currentDataSet
    }

    override fun enterTag(ctx: HestParser.TagContext) {
        currentDataSet.tag = ctx.tagName.text.replace("\"", "")
    }

    override fun enterName(ctx: HestParser.NameContext) {
        println(ctx.text)
        currentDataSet.name = ctx.nameName.text
    }

    override fun enterPkey(ctx: HestParser.PkeyContext) {
        currentDataSet.publicKey = ctx.pKey.text
    }

    override fun enterFormat(ctx: HestParser.FormatContext) {
        currentDataSet.format = when {
            ctx.JSON() != null -> ctx.JSON().text
            ctx.XML() != null -> ctx.XML().text
            else -> throw RuntimeException("Format not supported")
        }
    }

    override fun enterVariable(ctx: HestParser.VariableContext) {
        if (ctx.ID() == null) return
        val isDataSet = ctx.parent is HestParser.DatasetContext
        if (isDataSet) currentDataSetVarMap[ctx.ID().text] = buildExpressionTree(ctx.expression())
        else varMap[ctx.ID().text] = buildExpressionTree(ctx.expression())
    }


    override fun enterRun(ctx: HestParser.RunContext) {
        val exp = try {
            ctx.expression()
        } catch (e: Exception) {
            return
        }
        currentRule.steps.add(buildExpressionTree(exp)) /*= ctx.condition *//*ctx..map { buildExpressionTree(it) }.toMutableList()*/
    }


    private fun buildExpressionTree(expression: HestParser.ExpressionContext): Expression = when (expression) {
        is HestParser.ComparatorExpressionContext -> expression.run { AndOrExp(buildExpressionTree(left), op.text, buildExpressionTree(right)) }
        is HestParser.BinaryExpressionContext -> expression.run { CompareExp(buildExpressionTree(left), op?.text, right?.let { buildExpressionTree(it) }) }
        is HestParser.BoolExpressionContext -> BooleanType(expression.BOOL().text!!.toBoolean())
        is HestParser.IdentifierExpressionContext -> ReferenceType(expression.ID().text!!.toString())
        is HestParser.DecimalExpressionContext -> NumberType(expression.DECLIT().text!!.toDouble())
        is HestParser.StringExpressionContext -> StringType(expression.text!!)
        else -> throw RuntimeException("impossible")
    }

    override fun visitErrorNode(node: ErrorNode) {
        println("error $node")
    }

}

sealed class Expression
data class AndOrExp(var left: Expression, var op: String, var right: Expression) : Expression()
data class CompareExp(var left: Expression, var op: String? = null, var right: Expression? = null) : Expression()
data class StringType(val stringVal: String) : Expression()
data class NumberType(val doubleVal: Double) : Expression()
data class BooleanType(val booleanVal: Boolean) : Expression()
data class ReferenceType(val referenceName: String) : Expression()