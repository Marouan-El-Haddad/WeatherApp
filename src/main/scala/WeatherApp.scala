// Import necessary libraries and packages
import com.typesafe.config.{Config, ConfigFactory} // Configuration management library
import io.circe.generic.auto._ // Automatic JSON codec derivation
import sttp.client3._ // HTTP client library
import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend // Backend for asynchronous HTTP requests
import sttp.client3.circe._ // Circe support for sttp
import scala.concurrent.ExecutionContext.Implicits.global // Implicit execution context for Future
import scala.concurrent.Future // Asynchronous computation
import scala.util.{Failure, Success} // Result of a computation

// Case class to represent weather data
case class WeatherData(city: String, temp_celsius: Double, humidity: Option[Int], pressure: Int, wind_speed: Option[Double])

// Case class to represent the structure of the API response
case class WeatherResponse(name: String, main: MainWeather, wind: Wind)

// Case class to represent the main weather data in the API response
case class MainWeather(temp: Double, humidity: Option[Int], pressure: Int)

// Case class to represent the wind data in the API response
case class Wind(speed: Option[Double])

// Main object containing the WeatherApp
object WeatherApp {
  // Initialize the asynchronous HTTP backend
  private val backend = AsyncHttpClientFutureBackend()

  // Load configuration and get the API key
  private val config: Config = ConfigFactory.load()
  private val apiKey: Option[String] = if (config.hasPath("openweathermap.api_key")) {
    Some(config.getString("openweathermap.api_key"))
  } else {
    None
  }


  // Main entry point for the application
  def main(args: Array[String]): Unit = {
    apiKey match {
      case Some(apiKey) =>
        // Get the list of cities from the command line arguments or use the default list
        val cities = if (args.nonEmpty) args.toList else List("New York", "London", "Tokyo", "Sydney", "Mumbai")

        // Fetch weather data for all cities
        val allCityWeatherDataFuture = fetchMultipleCitiesWeatherData(cities, apiKey)

        // Process the results when all requests are completed
        allCityWeatherDataFuture onComplete {
          case Success(cityWeatherDataList) =>
            // Display the weather data for each city
            cityWeatherDataList.foreach(displayWeatherData)

            // Calculate and display the average temperature
            val avgTemperature = averageTemperature(cityWeatherDataList)
            println(f"Average temperature of all cities: $avgTemperature%.1f°C")

            // Close the backend
            backend.close()
          case Failure(error) =>
            // Handle errors and close the backend
            println(s"Unexpected error occurred: ${error.getMessage}")
            backend.close()
        }

      case None =>
        // Handle the case when the API key is not found in the configuration
        println("API key not found in configuration. Please set 'openweathermap.api_key' in your configuration file.")
    }
  }

  // Calculate the average temperature of a sequence of WeatherData
  private def averageTemperature(cityWeatherDataList: Seq[WeatherData]): Double = {
    cityWeatherDataList.map(_.temp_celsius).sum / cityWeatherDataList.size
  }

  // Display the weather data for a city
  private def displayWeatherData(weatherData: WeatherData): Unit = {
    println(s"Weather data for ${weatherData.city}:")
    println(f"  Temperature: ${weatherData.temp_celsius}%.1f°C")
    println(s"  Humidity: ${weatherData.humidity.getOrElse("N/A")}%")
    println(s"  Pressure: ${weatherData.pressure} hPa")
    println(s"  Wind Speed: ${weatherData.wind_speed.getOrElse("N/A")} m/s")
  }

  // Fetch weather data for multiple cities
  private def fetchMultipleCitiesWeatherData(cities: List[String], apiKey: String): Future[List[WeatherData]] = {
    // Use Future.traverse to fetch data for all cities concurrently
    Future.traverse(cities) { city =>
      fetchWeatherData(city, apiKey)
    }
  }

  // Fetch weather data for a single city
  private def fetchWeatherData(city: String, apiKey: String): Future[WeatherData] = {
    // Construct the API URL with the city, API key, and units (metric)
    val apiUrl = s"https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

    // Create a GET request with the API URL and expect a JSON response with the WeatherResponse structure
    val request = basicRequest.get(uri"$apiUrl").response(asJson[WeatherResponse])

    // Send the request and process the response
    request.send(backend).flatMap {
      // In case of a successful deserialization, convert the WeatherResponse to WeatherData
      case Response(Right(weatherResponse), _, _, _, _, _) =>
        val weatherData = WeatherData(
          city = weatherResponse.name,
          temp_celsius = weatherResponse.main.temp,
          humidity = weatherResponse.main.humidity,
          pressure = weatherResponse.main.pressure,
          wind_speed = weatherResponse.wind.speed
        )
        Future.successful(weatherData)
      // Handle errors in JSON deserialization
      case Response(Left(DeserializationException(_, error)), _, _, _, _, _) =>
        Future.failed(new Exception(s"Error fetching weather data for $city. Message: ${error.getMessage}"))
      // Handle HTTP errors
      case Response(Left(HttpError(_, statusCode)), _, statusText, _, _, _) =>
        Future.failed(new Exception(s"Error fetching weather data for $city. HTTP error: ${statusCode.code} $statusText"))
      // Handle other errors
      case Response(Left(error: Throwable), _, _, _, _, _) =>
        Future.failed(new Exception(s"Error fetching weather data for $city. Message: ${error.getMessage}"))
    }
  }
}