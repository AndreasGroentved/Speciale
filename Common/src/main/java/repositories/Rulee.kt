package repositories

import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import java.io.Serializable


@Indices(Index(value = "id"))
data class Rulee(@Id val id: String = "", val rule: String = "") : Serializable