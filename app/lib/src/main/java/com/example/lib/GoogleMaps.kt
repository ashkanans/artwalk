import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GoogleMaps {

    private val apiKey =
        "AIzaSyCGygp0SRJldfPq7nWt7kPNtaJ168VZH7E" // Replace "YOUR_API_KEY" with your actual Google Maps API key

    fun searchText(textQuery: String, pageSize: Int) {
        try {
            // Create URL object
            val url = URL("https://places.googleapis.com/v1/places:searchText")

            // Create connection object
            val connection = url.openConnection() as HttpURLConnection

            // Set the request method to POST
            connection.requestMethod = "POST"

            // Set request headers
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-Goog-Api-Key", apiKey)
            connection.setRequestProperty(
                "X-Goog-FieldMask",
                "places.displayName,places.location,places.displayName,places.id,nextPageToken"
            )

            // Enable input/output and disable caching
            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false

            // Create JSON request body
            val requestBody = """
                {
                    "textQuery": "$textQuery",
                    "pageSize": $pageSize
                }
            """.trimIndent()

            // Send request body
            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
            writer.write(requestBody)
            writer.flush()
            writer.close()
            outputStream.close()

            // Get response code
            val responseCode = connection.responseCode

            // Read response
            val inputStream: InputStream
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
            } else {
                inputStream = connection.errorStream
            }
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Print response
            println("Response: $response")

            // Close the connection
            connection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun main() {
    val googleMaps = GoogleMaps()
    googleMaps.searchText("touristic places in Rome", 10)
}
