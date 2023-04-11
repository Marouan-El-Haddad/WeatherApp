import com.typesafe.config.{Config, ConfigFactory}
import io.circe.generic.auto._
import sttp.client3._
import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client3.circe._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class WeatherData(city: String, temp_celsius: Double, humidity: Option[Int], pressure: Int, wind_speed: Option[Double])

case class WeatherResponse(name: String, main: MainWeather, wind: Wind)

case class MainWeather(temp: Double, humidity: Option[Int], pressure: Int)

case class Wind(speed: Option[Double])

object WeatherApp {
  private val backend = AsyncHttpClientFutureBackend()

  private val config: Config = ConfigFactory.load()
  private val apiKey: Option[String] = if (config.hasPath("openweathermap.api_key")) {
    Some(config.getString("openweathermap.api_key"))
  } else {
    None
  }

  def main(args: Array[String]): Unit = {
    apiKey match {
      case Some(apiKey) =>
        val cities = if (args.nonEmpty) args.toList else List("New York", "London", "Tokyo", "Sydney", "Mumbai")

        val allCityWeatherDataFuture = fetchMultipleCitiesWeatherData(cities, apiKey)

        allCityWeatherDataFuture onComplete {
          case Success(cityWeatherDataList) =>
            cityWeatherDataList.foreach(displayWeatherData)

            val avgTemperature = averageTemperature(cityWeatherDataList)
            println(f"Average temperature of all cities: $avgTemperature%.1f°C")

            backend.close()
          case Failure(error) =>
            println(s"Unexpected error occurred: ${error.getMessage}")

            backend.close()
        }

      case None =>
        println("API key not found in configuration. Please set 'openweathermap.api_key' in your configuration file.")
    }
  }

  private def averageTemperature(cityWeatherDataList: Seq[WeatherData]): Double = {
    cityWeatherDataList.map(_.temp_celsius).sum / cityWeatherDataList.size
  }

  private def displayWeatherData(weatherData: WeatherData): Unit = {
    println(s"Weather data for ${weatherData.city}:")
    println(f"  Temperature: ${weatherData.temp_celsius}%.1f°C")
    println(s"  Humidity: ${weatherData.humidity.getOrElse("N/A")}%")
    println(s"  Pressure: ${weatherData.pressure} hPa")
    println(s"  Wind Speed: ${weatherData.wind_speed.getOrElse("N/A")} m/s")
  }

  private def fetchMultipleCitiesWeatherData(cities: List[String], apiKey: String): Future[List[WeatherData]] = {
    Future.traverse(cities) { city =>
      fetchWeatherData(city, apiKey)
    }
  }

  private def fetchWeatherData(city: String, apiKey: String): Future[WeatherData] = {
    val apiUrl = s"https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
    val request = basicRequest.get(uri"$apiUrl").response(asJson[WeatherResponse])

    request.send(backend).flatMap {
      case Response(Right(weatherResponse), _, _, _, _, _) =>
        val weatherData = WeatherData(
          city = weatherResponse.name,
          temp_celsius = weatherResponse.main.temp,
          humidity = weatherResponse.main.humidity,
          pressure = weatherResponse.main.pressure,
          wind_speed = weatherResponse.wind.speed
        )
        Future.successful(weatherData)
      case Response(Left(DeserializationException(_, error)), _, _, _, _, _) =>
        Future.failed(new Exception(s"Error fetching weather data for $city. Message: ${error.getMessage}"))
      case Response(Left(HttpError(_, statusCode)), _, statusText, _, _, _) =>
        Future.failed(new Exception(s"Error fetching weather data for $city. HTTP error: ${statusCode.code} $statusText"))
      case Response(Left(error: Throwable), _, _, _, _, _) =>
        Future.failed(new Exception(s"Error fetching weather data for $city. Message: ${error.getMessage}"))
    }
  }
}