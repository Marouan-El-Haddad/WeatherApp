# WeatherApp

WeatherApp is a simple command-line application that fetches and displays weather data for a list of cities using the OpenWeatherMap API. The app concurrently fetches weather data for multiple cities and calculates the average temperature for all cities.

## Prerequisites

To use WeatherApp, you need to obtain an API key from OpenWeatherMap. You can sign up for a free API key at [OpenWeatherMap's website](https://openweathermap.org/appid).

## Configuration

Create a file named `application.conf` in the project's root directory or the `src/main/resources` directory. Add the following content, replacing `your_api_key_here` with your actual OpenWeatherMap API key:

```hocon
openweathermap {
  api_key = "your_api_key_here"
}
``` 
## Running the App

To run the WeatherApp, use the following command in your terminal or command prompt:

```shell
sbt "run [city1 city2 city3 ...]"
```

Replace [city1 city2 city3 ...] with a space-separated list of city names. If you don't provide any city names, the app will use a default list of cities: New York, London, Tokyo, Sydney, and Mumbai.

For example, to fetch weather data for Paris, Berlin, and Rome, use the following command:
```shell
sbt "run Paris Berlin Rome"
``` 

## Sample Output
WeatherApp displays weather data for each city, including temperature, humidity, pressure, and wind speed. Here's a sample output:
```yaml
Weather data for New York:
  Temperature: 22.1°C
  Humidity: 49%
  Pressure: 1012 hPa
  Wind Speed: 4.2 m/s

Weather data for London:
  Temperature: 18.3°C
  Humidity: 62%
  Pressure: 1018 hPa
  Wind Speed: 3.6 m/s

Average temperature of all cities: 20.2°C
```

## Contributing
Feel free to submit issues or pull requests for improvements or additional features. Contributions are welcome!

## License
This project is available under the MIT License.
