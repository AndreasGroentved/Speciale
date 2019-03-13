package datatypes.energinet

data class Result(
    val records: List<RecordsItem?>? = null,
    val fields: List<FieldsItem?>? = null
)
