package com.clinikdb.webviewTest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.gson.Gson
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoctorAppointmentWebView()
        }
    }
}

@Composable
fun DoctorAppointmentWebView() {
    // Load HTML file from assets
    val webViewState = rememberWebViewState("file:///android_asset/calendar_web/consultation_calendar.html")
    val jsBridge = rememberWebViewJsBridge()

    // Register all API handlers
    LaunchedEffect(jsBridge) {
        jsBridge.register(GetDoctorHandler())
        jsBridge.register(SetDoctorHandler())
        jsBridge.register(SelectDateHandler())
        jsBridge.register(QueryPatientHandler())
        jsBridge.register(SaveAppointmentHandler())
        jsBridge.register(DeleteAppointmentHandler())
        jsBridge.register(EditAppointmentHandler())
    }

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
        webViewJsBridge = jsBridge
    )
}

// Data classes
data class Doctor(
    val id: String,
    val name: String,
    val specialization: String,
    val email: String,
    val phone: String
)

data class Patient(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val date_of_birth: String,
    val address: String
)

data class Appointment(
    val id: String,
    val title: String,
    val notes: String,
    val from: Long,
    val duration_minutes: Int,
    val patient_id: String,
    val type: String
)

data class ApiResponse(
    val code: Int,
    val message: String,
    val appointment_id: String? = null
)

// Dummy data
object DummyData {
    private val gson = Gson()

    val doctor = Doctor(
        id = "doc001",
        name = "Dr. Joe Smith",
        specialization = "General Practice",
        email = "dr.joe@hospital.com",
        phone = "+1234567890"
    )

    val patients = listOf(
        Patient(
            id = "1",
            name = "John Smith",
            email = "john.smith@email.com",
            phone = "+1234567890",
            date_of_birth = "1980-01-15",
            address = "123 Main St, City, State"
        ),
        Patient(
            id = "2",
            name = "Sarah Johnson",
            email = "sarah.j@email.com",
            phone = "+1234567891",
            date_of_birth = "1985-05-22",
            address = "456 Oak Ave, City, State"
        ),
        Patient(
            id = "3",
            name = "Michael Brown",
            email = "m.brown@email.com",
            phone = "+1234567892",
            date_of_birth = "1975-11-08",
            address = "789 Pine Rd, City, State"
        ),
        Patient(
            id = "4",
            name = "Emily Davis",
            email = "emily.d@email.com",
            phone = "+1234567893",
            date_of_birth = "1990-03-18",
            address = "321 Elm St, City, State"
        ),
        Patient(
            id = "5",
            name = "David Wilson",
            email = "d.wilson@email.com",
            phone = "+1234567894",
            date_of_birth = "1982-07-25",
            address = "654 Maple Dr, City, State"
        ),
        Patient(
            id = "6",
            name = "Lisa Anderson",
            email = "lisa.a@email.com",
            phone = "+1234567895",
            date_of_birth = "1988-09-12",
            address = "987 Cedar Ln, City, State"
        )
    )

    val appointments = mutableListOf<Appointment>().apply {
        val titles = listOf(
            "John Smith", "Sarah Johnson", "Michael Brown", "Emily Davis", "David Wilson",
            "Lisa Anderson", "James Anderson", "Olivia Martinez", "Daniel Thomas", "Emma Jackson",
            "Liam White", "Isabella Harris", "Ethan Lewis", "Mia Clark", "Noah Hall",
            "Ava Allen", "Lucas Young", "Charlotte King", "Mason Wright", "Amelia Scott",
            "Elijah Green", "Harper Adams", "Logan Baker", "Abigail Nelson", "Jacob Carter",
            "Ella Mitchell", "William Perez", "Grace Roberts", "Benjamin Turner", "Chloe Phillips"
        )

        val notes = listOf("Regular checkup", "Follow-up", "Consultation", "Therapy session", "Routine visit")

        val random = java.util.Random()
        var idCounter = 1001

        for (dayOffset in 0..2) {
            val day = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // Start each day at 8:00 AM
            var currentStart = day.clone() as Calendar

            for (i in 0 until 10) {
                val duration = listOf(30, 45, 60).random()
                val note = notes.random()
                val title = titles[i + (dayOffset * 10)]
                val patientId = if (random.nextBoolean()) (1..6).random().toString() else ""
                val type = if (patientId.isEmpty()) "Other" else "Patient"

                val from = currentStart.timeInMillis / 1000

                add(
                    Appointment(
                        id = (idCounter++).toString(),
                        title = title,
                        notes = note,
                        from = from,
                        duration_minutes = duration,
                        patient_id = patientId,
                        type = type
                    )
                )

                // Move start time forward by duration + random gap (10–30 minutes)
                currentStart.add(Calendar.MINUTE, duration + (10..30).random())

                // Stop adding if we go past 18:00 (6 PM)
                if (currentStart.get(Calendar.HOUR_OF_DAY) >= 18) break
            }
        }
    }
}



// Message Handlers
class GetDoctorHandler : IJsMessageHandler {
    private val gson = Gson()

    override fun methodName(): String = "getDoctor"
    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) //simulate sql execution
            val response = gson.toJson(DummyData.doctor)
            callback(response)
        }
    }


}

class SetDoctorHandler : IJsMessageHandler {
    override fun methodName(): String = "setDoctor"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            // In real app, would store doctor data
            callback("{\"success\": true}")
        }
    }
}

class SelectDateHandler : IJsMessageHandler {
    private val gson = Gson()

    override fun methodName(): String = "selectDate"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) //simulate sql execution

            val params = gson.fromJson(message.params, Map::class.java)
            val from = (params["from"] as? Double)?.toLong() ?: 0
            val to = (params["to"] as? Double)?.toLong() ?: Long.MAX_VALUE

            val filteredAppointments = DummyData.appointments.filter { apt ->
                apt.from >= from && apt.from < to
            }

            val response = gson.toJson(filteredAppointments)
            callback(response)
        }
    }
}

class QueryPatientHandler : IJsMessageHandler {
    private val gson = Gson()

    override fun methodName(): String = "queryPatient"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(200) //simulate sql execution

            val params = gson.fromJson(message.params, Map::class.java)
            val query = (params["query"] as? String)?.lowercase() ?: ""

            val filteredPatients = DummyData.patients.filter { patient ->
                patient.name.lowercase().contains(query) ||
                        patient.email.lowercase().contains(query)
            }

            val response = gson.toJson(filteredPatients)
            callback(response)
        }
    }
}

class SaveAppointmentHandler : IJsMessageHandler {
    private val gson = Gson()

    override fun methodName(): String = "saveAppointment"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(400) //simulate sql execution

            val appointmentData = gson.fromJson(message.params, Map::class.java)
            val newId = System.currentTimeMillis().toString()

            val newAppointment = Appointment(
                id = newId,
                title = appointmentData["title"] as? String ?: "",
                notes = appointmentData["notes"] as? String ?: "",
                from = (appointmentData["from"] as? Double)?.toLong() ?: 0,
                duration_minutes = (appointmentData["duration_minutes"] as? Double)?.toInt() ?: 30,
                patient_id = appointmentData["patient_id"] as? String ?: "",
                type = if ((appointmentData["patient_id"] as? String)?.isNotEmpty() == true) "Patient" else "Other"
            )

            DummyData.appointments.add(newAppointment)

            val response = ApiResponse(
                code = 200,
                message = "OK",
                appointment_id = newId
            )

            callback(gson.toJson(response))
        }
    }
}

class DeleteAppointmentHandler : IJsMessageHandler {
    private val gson = Gson()

    override fun methodName(): String = "deleteAppointment"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) //simulate sql execution

            val params = gson.fromJson(message.params, Map::class.java)
            val appointmentId = params["appointmentId"] as? String ?: ""

            val removed = DummyData.appointments.removeIf { it.id == appointmentId }

            val response = if (removed) {
                ApiResponse(code = 200, message = "OK")
            } else {
                ApiResponse(code = 400, message = "Appointment not found")
            }

            callback(gson.toJson(response))
        }
    }
}

class EditAppointmentHandler : IJsMessageHandler {
    private val gson = Gson()

    override fun methodName(): String = "editAppointment"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(400) //simulate sql execution

            val appointmentData = gson.fromJson(message.params, Map::class.java)
            val appointmentId = appointmentData["appointment_id"] as? String ?: ""

            val index = DummyData.appointments.indexOfFirst { it.id == appointmentId }

            val response = if (index >= 0) {
                val existing = DummyData.appointments[index]
                DummyData.appointments[index] = Appointment(
                    id = existing.id,
                    title = appointmentData["title"] as? String ?: existing.title,
                    notes = appointmentData["notes"] as? String ?: existing.notes,
                    from = (appointmentData["from"] as? Double)?.toLong() ?: existing.from,
                    duration_minutes = (appointmentData["duration_minutes"] as? Double)?.toInt() ?: existing.duration_minutes,
                    patient_id = appointmentData["patient_id"] as? String ?: existing.patient_id,
                    type = if ((appointmentData["patient_id"] as? String)?.isNotEmpty() == true) "Patient" else "Other"
                )
                ApiResponse(code = 200, message = "OK")
            } else {
                ApiResponse(code = 400, message = "Appointment not found")
            }

            callback(gson.toJson(response))
        }
    }
}