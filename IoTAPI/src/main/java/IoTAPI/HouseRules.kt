package IoTAPI

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import repositories.Rulee
import java.util.*


class HouseRules {

    private val ruleRep: ObjectRepository<Rulee>

    init {
        val db = Nitrite.builder()
            .filePath("rule.db")
            .openOrCreate()
        ruleRep = db.getRepository(Rulee::class.java)
    }

    fun getRules() = ruleRep.find().firstOrNull<Rulee>() ?: Rulee(UUID.randomUUID().toString(), "")

    fun saveRules(ruleString: String) {
        val rule = ruleRep.find()
        if (rule.size() > 0) {
            ruleRep.update(rule.first<Rulee>().run { Rulee(this.id, ruleString) })
        } else {
            ruleRep.insert(Rulee(UUID.randomUUID().toString(), ruleString))
        }
    }


}