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
