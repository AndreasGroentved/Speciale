package IoTAPI

import hest.HestParser
import hest.HestParserBaseListener
import org.antlr.v4.runtime.tree.ErrorNode


sealed class Step


data class Rule(val steps: MutableList<Step> = mutableListOf(), var config: String = "")
data class Assignment(val toBeAssigned: String, val valueToAssign: String) : Step()
data class MethodCall(val getPost: String, val parameters: List<Pair<String, String>>, val path: String, val deviceID: String) : Step()
data class Exp(var value: String = "", var op: String? = null, var exp: Exp? = null) : Step()
data class DataSet(var name: String = "", var tag: String = "", var header: String = "", var format: String = "", val vars: MutableMap<String, String> = mutableMapOf())

//class VarRes(val name: String, val type: String, val value: String)

class ParseHest : HestParserBaseListener() {


    val varMap = mutableMapOf<String, Any>() //todo map med rigtige typer
    val currentDataSetVarMap = mutableMapOf<String, Any>()
    var currentExp: Exp? = null
    val conditions = mutableListOf<Step>()
    var currentDataSet = DataSet()
    var currentRule = Rule()
    val dataSetMap = mutableMapOf<String, DataSet>()
    val rules = mutableListOf<Rule>()

    init {

    }

    override fun enterContent(ctx: HestParser.ContentContext?) {
        super.enterContent(ctx)
    }

    override fun exitContent(ctx: HestParser.ContentContext?) {
        super.exitContent(ctx)
    }

    override fun enterRulee(ctx: HestParser.RuleeContext?) {
        currentRule = Rule()
    }

    override fun exitRulee(ctx: HestParser.RuleeContext?) {
        rules.add(currentRule)
    }


    override fun enterConfig(ctx: HestParser.ConfigContext?) {
        super.enterConfig(ctx)
    }

    override fun exitConfig(ctx: HestParser.ConfigContext?) {
        super.exitConfig(ctx)
    }

    override fun enterTime(ctx: HestParser.TimeContext?) {
        super.enterTime(ctx)
    }

    override fun exitTime(ctx: HestParser.TimeContext?) {
        super.exitTime(ctx)
    }

    override fun enterTimeDefinition(ctx: HestParser.TimeDefinitionContext?) {
        super.enterTimeDefinition(ctx)
    }

    override fun exitTimeDefinition(ctx: HestParser.TimeDefinitionContext?) {
        super.exitTimeDefinition(ctx)
    }

    override fun enterInterval(ctx: HestParser.IntervalContext?) {
        super.enterInterval(ctx)
    }

    override fun exitInterval(ctx: HestParser.IntervalContext?) {
        super.exitInterval(ctx)
    }

    override fun enterRun(ctx: HestParser.RunContext?) {
        super.enterRun(ctx)
    }

    override fun exitRun(ctx: HestParser.RunContext?) {
        super.exitRun(ctx)
    }


    private fun updateExpression(exp: Exp?, ctx: HestParser.ConditionContext): Exp {
        return if (exp != null) {
            exp.exp = updateExpression(exp.exp, ctx)
            exp
        } else {
            val expression = Exp()
            expression.value = ctx.cName.text
            if (ctx.eqOperator() != null) {
                expression.op = ctx.eqOperator().text
            }
            expression
        }
    }


    override fun enterCondition(ctx: HestParser.ConditionContext) {
        currentExp = updateExpression(currentExp, ctx)
    }

    override fun exitCondition(ctx: HestParser.ConditionContext) {
        if (currentExp != null) currentRule.steps.add(currentExp!!)
        currentExp = null
    }

    override fun enterEqOperator(ctx: HestParser.EqOperatorContext?) {
        super.enterEqOperator(ctx)
    }

    override fun exitEqOperator(ctx: HestParser.EqOperatorContext?) {
        super.exitEqOperator(ctx)
    }

    override fun enterOutput(ctx: HestParser.OutputContext?) {
        super.enterOutput(ctx)
    }

    override fun exitOutput(ctx: HestParser.OutputContext?) {
        super.exitOutput(ctx)
    }

    override fun enterDevice(ctx: HestParser.DeviceContext?) {
        super.enterDevice(ctx)
    }

    override fun exitDevice(ctx: HestParser.DeviceContext?) {
        super.exitDevice(ctx)
    }

    override fun enterPath(ctx: HestParser.PathContext?) {
        super.enterPath(ctx)
    }

    override fun exitPath(ctx: HestParser.PathContext?) {
        super.exitPath(ctx)
    }

    override fun enterParameter(ctx: HestParser.ParameterContext?) {
        super.enterParameter(ctx)
    }

    override fun exitParameter(ctx: HestParser.ParameterContext?) {
        super.exitParameter(ctx)
    }

    override fun enterVarpath(ctx: HestParser.VarpathContext?) {
        super.enterVarpath(ctx)
    }

    override fun exitVarpath(ctx: HestParser.VarpathContext?) {
        super.exitVarpath(ctx)
    }


    override fun enterDataset(ctx: HestParser.DatasetContext) {
        currentDataSet = DataSet()
    }

    override fun exitDataset(ctx: HestParser.DatasetContext) {
        currentDataSetVarMap[currentDataSet.name] = currentDataSet
    }

    override fun enterTag(ctx: HestParser.TagContext) {
        currentDataSet.tag = ctx.TAG().text
    }


    override fun enterHeader(ctx: HestParser.HeaderContext) {
        currentDataSet.header = ctx.HEADER().text
    }

    override fun enterName(ctx: HestParser.NameContext) {
        currentDataSet.name = ctx.NAME().text
    }

    override fun enterFormat(ctx: HestParser.FormatContext) {
        currentDataSet.format = when {
            ctx.JSON() != null -> ctx.JSON().text
            ctx.XML() != null -> ctx.XML().text
            else -> throw NotImplementedError("Format not supported")
        }
    }


    override fun enterVariable(ctx: HestParser.VariableContext?) {
        if (ctx?.ID() == null) return

        val isDataSet = ctx.parent is HestParser.DatasetContext
        val value: Any = when {
            ctx.varTypes().BOOL()?.text != null -> ctx.varTypes().BOOL().text!!.toBoolean()
            ctx.varTypes().INTLIT()?.text != null -> ctx.varTypes().INTLIT().text!!.toInt()
            ctx.varTypes().STRINGLIT()?.text != null -> ctx.varTypes().STRINGLIT().text!!
            ctx.varTypes().DECLIT()?.text != null -> ctx.varTypes().DECLIT().text!!.toFloat()
            else -> throw RuntimeException("Yo ikke gyldig type")
        }
        if (isDataSet) currentDataSetVarMap[ctx.ID().text] = value
        else varMap[ctx.ID().text] = value

    }

    override fun visitErrorNode(node: ErrorNode) {
        println("error $node")
    }

}