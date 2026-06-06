package pl.edu.ur.dentflow.data

data class Visit(
    val id: Int,
    val patientName: String,
    val doctorName: String,
    val serviceName: String,
    val date: String,
    val status: String
)

