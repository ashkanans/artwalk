package services.api.predict

data class PredictRequest(
    val start_id: String,
    val end_id: String,
    val time_span: Int
)