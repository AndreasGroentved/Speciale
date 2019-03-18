package IoTAPI

import hest.HestParser
import hest.HestParserBaseListener
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode


sealed class Step


data class Rule(val steps: List<Step>, val config: String)
data class Assignment(val toBeAssigned: String, val valueToAssign: String) : Step()
data class MethodCall(val getPost: String, val parameters: List<Pair<String, String>>, val path: String, val deviceID: String) : Step()
data class Exp(val value: String, val op: String?, val exp: Exp?) : Step()
data class DataSet(val tag: String, val header: String, val format: String, val vars: MutableMap<String, String>)

class ParseHest : HestParserBaseListener() {


    val varMap = mutableMapOf<String, String>()
    val conditions = mutableListOf<Step>()
    val dataSetMap = mutableMapOf<String, DataSet>()

    init {

    }

    override fun enterContent(ctx: HestParser.ContentContext?) {
        super.enterContent(ctx)
    }

    override fun exitContent(ctx: HestParser.ContentContext?) {
        super.exitContent(ctx)
    }

    override fun enterRulee(ctx: HestParser.RuleeContext?) {
        super.enterRulee(ctx)
    }

    override fun exitRulee(ctx: HestParser.RuleeContext?) {
        super.exitRulee(ctx)
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

    override fun enterCondition(ctx: HestParser.ConditionContext?) {
        super.enterCondition(ctx)
    }

    override fun exitCondition(ctx: HestParser.ConditionContext?) {
        super.exitCondition(ctx)
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


    override fun enterDataset(ctx: HestParser.DatasetContext?) {
        requireNotNull(ctx)


        super.enterDataset(ctx)
    }

    override fun exitDataset(ctx: HestParser.DatasetContext?) {
        super.exitDataset(ctx)
    }

    override fun enterTag(ctx: HestParser.TagContext?) {
        super.enterTag(ctx)
    }

    override fun exitTag(ctx: HestParser.TagContext?) {
        super.exitTag(ctx)
    }

    override fun enterHeader(ctx: HestParser.HeaderContext?) {
        super.enterHeader(ctx)
    }

    override fun exitHeader(ctx: HestParser.HeaderContext?) {
        super.exitHeader(ctx)
    }

    override fun enterName(ctx: HestParser.NameContext?) {
        super.enterName(ctx)
    }

    override fun exitName(ctx: HestParser.NameContext?) {
        super.exitName(ctx)
    }

    override fun enterFormat(ctx: HestParser.FormatContext?) {
        super.enterFormat(ctx)
    }

    override fun exitFormat(ctx: HestParser.FormatContext?) {
        super.exitFormat(ctx)
    }

    override fun enterFormatType(ctx: HestParser.FormatTypeContext?) {
        super.enterFormatType(ctx)
    }

    override fun exitFormatType(ctx: HestParser.FormatTypeContext?) {
        super.exitFormatType(ctx)
    }

    override fun enterVariable(ctx: HestParser.VariableContext?) {
        requireNotNull(ctx)
        val isDataSet = ctx.parent is HestParser.DatasetContext
        println(ctx.ID())


        super.enterVariable(ctx)
    }

    override fun exitVariable(ctx: HestParser.VariableContext?) {
        super.exitVariable(ctx)
    }

    override fun enterVarTypes(ctx: HestParser.VarTypesContext?) {
        super.enterVarTypes(ctx)
    }

    override fun exitVarTypes(ctx: HestParser.VarTypesContext?) {
        super.exitVarTypes(ctx)
    }


    override fun enterEveryRule(ctx: ParserRuleContext?) {
        super.enterEveryRule(ctx)
    }

    override fun exitEveryRule(ctx: ParserRuleContext?) {
        super.exitEveryRule(ctx)
    }

    override fun visitTerminal(node: TerminalNode?) {
        super.visitTerminal(node)
    }

    override fun visitErrorNode(node: ErrorNode?) {
        super.visitErrorNode(node)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }
}