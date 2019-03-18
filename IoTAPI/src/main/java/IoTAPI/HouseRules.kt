package IoTAPI

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import java.util.*


class HouseRules {

    private val ruleRep: ObjectRepository<Rule>

    init {
        val db = Nitrite.builder()
            .filePath("rule.db")
            .openOrCreate()
        ruleRep = db.getRepository(Rule::class.java)
    }

    fun getRules() = ruleRep.find().firstOrNull<Rule>() ?: Rule(UUID.randomUUID().toString(), "")

    fun saveRules(ruleString: String) {
        val rule = ruleRep.find()
        if (rule.size() > 0) {
            ruleRep.update(rule.first<Rule>().run { Rule(this.id, ruleString) })
        } else {
            ruleRep.insert(Rule(UUID.randomUUID().toString(), ruleString))
        }
    }


}