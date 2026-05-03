package pl.edu.ur.coopspace.user_module

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import pl.edu.ur.coopspace.auth.AuthSessionStore
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

data class RawCharge(
    val id: Int,
    val periodStart: String,
    val periodEnd: String,
    val totalAmount: Double,
    val status: String
)

data class RawPayment(
    val id: Int,
    val chargeId: Int,
    val amount: Double
)

data class RawChargeItem(
    val id: Int,
    val chargeId: Int,
    val typeId: Int,
    val total: Double
)

data class RawChargeItemType(
    val id: Int,
    val name: String
)

data class UserFinanceSummary(
    val balance: Double
)

data class UserCharge(
    val id: Int,
    val amount: Double,
    val month: String,
    val isPaid: Boolean
)

data class ChargeItemDto(
    val name: String,
    val amount: Double
)

data class UserChargeDetails(
    val id: Int,
    val month: String,
    val periodStart: String,
    val periodEnd: String,
    val statusText: String,
    val amountToPay: Double,
    val items: List<ChargeItemDto>
)

object UserFinanceApiClient {

    private fun getBaseUrl() = BuildConfig.BASE_URL.trimEnd('/')

    private fun setupConnection(urlString: String, context: Context): HttpURLConnection {
        val url = URL(urlString)
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json")
            val token = AuthSessionStore.getToken(context)
            if (!token.isNullOrEmpty()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        return stream?.use { BufferedReader(InputStreamReader(it)).readText() }.orEmpty()
    }

    private suspend fun fetchCharges(context: Context): List<RawCharge> = withContext(Dispatchers.IO) {
        val conn = setupConnection("${getBaseUrl()}/api/user/finances/charges", context)
        if (conn.responseCode !in 200..299) throw IllegalStateException("Błąd pobierania opłat")
        val arr = JSONArray(readResponseBody(conn))
        val list = mutableListOf<RawCharge>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                RawCharge(
                    id = obj.optInt("id"),
                    periodStart = obj.optString("periodStart", ""),
                    periodEnd = obj.optString("periodEnd", ""),
                    totalAmount = obj.optDouble("totalAmount", 0.0),
                    status = obj.optString("status", "")
                )
            )
        }
        list
    }

    private suspend fun fetchPayments(context: Context): List<RawPayment> = withContext(Dispatchers.IO) {
        val conn = setupConnection("${getBaseUrl()}/api/user/finances/payments", context)
        if (conn.responseCode !in 200..299) throw IllegalStateException("Błąd pobierania płatności")
        val arr = JSONArray(readResponseBody(conn))
        val list = mutableListOf<RawPayment>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                RawPayment(
                    id = obj.optInt("id"),
                    chargeId = obj.optInt("chargeId"),
                    amount = obj.optDouble("amount", 0.0)
                )
            )
        }
        list
    }

    private suspend fun fetchChargeItems(context: Context): List<RawChargeItem> = withContext(Dispatchers.IO) {
        val conn = setupConnection("${getBaseUrl()}/api/user/finances/charge-items", context)
        if (conn.responseCode !in 200..299) throw IllegalStateException("Błąd pobierania składników opłat")
        val arr = JSONArray(readResponseBody(conn))
        val list = mutableListOf<RawChargeItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                RawChargeItem(
                    id = obj.optInt("id"),
                    chargeId = obj.optInt("chargeId"),
                    typeId = obj.optInt("typeId"),
                    total = obj.optDouble("total", 0.0)
                )
            )
        }
        list
    }

    private suspend fun fetchChargeItemTypes(context: Context): List<RawChargeItemType> = withContext(Dispatchers.IO) {
        val conn = setupConnection("${getBaseUrl()}/api/user/finances/charge-item-types", context)
        if (conn.responseCode !in 200..299) throw IllegalStateException("Błąd pobierania typów składników")
        val arr = JSONArray(readResponseBody(conn))
        val list = mutableListOf<RawChargeItemType>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                RawChargeItemType(
                    id = obj.optInt("id"),
                    name = obj.optString("name", "")
                )
            )
        }
        list
    }

    private fun formatMonth(dateStr: String): String {
        if (dateStr.isEmpty() || dateStr == "null") return ""
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            val monthNames = arrayOf(
                "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
            )
            val cal = java.util.Calendar.getInstance()
            cal.time = date!!
            "${monthNames[cal.get(java.util.Calendar.MONTH)]} ${cal.get(java.util.Calendar.YEAR)}"
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatDate(dateStr: String): String {
        if (dateStr.isEmpty() || dateStr == "null") return ""
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    suspend fun getSummary(context: Context): Result<UserFinanceSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val charges = fetchCharges(context)
            val payments = fetchPayments(context)
            
            val totalCharges = charges.sumOf { it.totalAmount }
            val totalPayments = payments.sumOf { it.amount }
            
            UserFinanceSummary(balance = totalPayments - totalCharges)
        }
    }

    suspend fun getCharges(context: Context): Result<List<UserCharge>> = withContext(Dispatchers.IO) {
        runCatching {
            val charges = fetchCharges(context)
            val payments = fetchPayments(context)
            
            charges.sortedByDescending { it.id }.map { charge ->
                val paidAmount = payments.filter { it.chargeId == charge.id }.sumOf { it.amount }
                UserCharge(
                    id = charge.id,
                    amount = charge.totalAmount,
                    month = formatMonth(charge.periodStart),
                    isPaid = paidAmount >= charge.totalAmount
                )
            }
        }
    }

    suspend fun getChargeDetails(context: Context, chargeId: Int): Result<UserChargeDetails> = withContext(Dispatchers.IO) {
        runCatching {
            val charges = fetchCharges(context)
            val payments = fetchPayments(context)
            val items = fetchChargeItems(context)
            val types = fetchChargeItemTypes(context)
            
            val charge = if (chargeId == 0) {
                val today = java.time.LocalDate.now()
                charges.find { 
                    try {
                        val start = java.time.LocalDate.parse(it.periodStart)
                        val end = java.time.LocalDate.parse(it.periodEnd)
                        !today.isBefore(start) && !today.isAfter(end)
                    } catch (e: Exception) {
                        false
                    }
                } ?: charges.maxByOrNull { it.id }
            } else {
                charges.find { it.id == chargeId }
            } ?: throw IllegalStateException("Brak aktywnych opłat.")
            
            val activeChargeId = charge.id
            val chargePayments = payments.filter { it.chargeId == activeChargeId }
            val paidAmount = chargePayments.sumOf { it.amount }
            
            var amountToPay = charge.totalAmount - paidAmount
            if (amountToPay < 0) amountToPay = 0.0
            
            val statusText = when {
                paidAmount <= 0.0 -> "Do zapłaty"
                paidAmount >= charge.totalAmount -> "Opłacone"
                else -> "Częściowo Opłacone"
            }
            
            val typeMap = types.associateBy({ it.id }, { it.name })
            
            val chargeItemsMapped = items.filter { it.chargeId == activeChargeId }.map { item ->
                ChargeItemDto(
                    name = typeMap[item.typeId] ?: "Inne",
                    amount = item.total
                )
            }
            
            UserChargeDetails(
                id = charge.id,
                month = formatMonth(charge.periodStart),
                periodStart = formatDate(charge.periodStart),
                periodEnd = formatDate(charge.periodEnd),
                statusText = statusText,
                amountToPay = amountToPay,
                items = chargeItemsMapped
            )
        }
    }

    suspend fun postPayment(context: Context, chargeId: Int?, amount: Double): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${getBaseUrl()}/api/user/finances/payments")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                val token = AuthSessionStore.getToken(context)
                if (!token.isNullOrEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }
            
            val json = JSONObject().apply {
                put("amount", amount)
                if (chargeId != null && chargeId > 0) {
                    put("chargeId", chargeId)
                }
            }
            
            connection.outputStream.use { it.write(json.toString().toByteArray()) }
            
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val error = readResponseBody(connection)
                throw IllegalStateException("Nie udało się dokonać wpłaty: $error")
            }
        }
    }
}
