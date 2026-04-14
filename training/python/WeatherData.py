import requests
import datetime
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression
import json  # might need this later for caching

CITY = "Hyderabad"

# get lat/lon for the city using open meteo api
def get_coordinates(city):
    url = f"https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1"
    data = requests.get(url).json()
    return data['results'][0]['latitude'], data['results'][0]['longitude']

# fetch past weather data from archive api
def fetch_weather(lat, lon, days=10):
    end_date = datetime.date.today() - datetime.timedelta(days=1)
    start_date = end_date - datetime.timedelta(days=days-1)

    url = (
        f"https://archive-api.open-meteo.com/v1/archive?"
        f"latitude={lat}&longitude={lon}"
        f"&start_date={start_date}&end_date={end_date}"
        f"&daily=temperature_2m_mean,relative_humidity_2m_mean"
        f"&timezone=auto"
    )

    data = requests.get(url).json()

    dates = data['daily']['time']
    temps = data['daily']['temperature_2m_mean']
    humidity = data['daily']['relative_humidity_2m_mean']

    return dates, temps, humidity

# bar chart for temperature - blue to red gradient
def plot_temperature(dates, temps):
    temps = np.array(temps)

    # normalize to get color gradient
    norm = (temps - temps.min()) / (temps.max() - temps.min())
    colors = plt.cm.coolwarm(norm)

    # dynamic y axis so it looks nice
    ymin = temps.min() - 5
    ymax = temps.max() + 2

    plt.figure(figsize=(10,5))

    # make the bars thinner so they dont overlap
    plt.bar(dates, temps, color=colors, width=0.4)

    plt.ylim(ymin, ymax)
    plt.title("Past 10 Days Temperature Trend")
    plt.xlabel("Date")
    plt.ylabel("Temperature (°C)")
    plt.xticks(rotation=45)

    plt.tight_layout()
    plt.show()

def plot_humidity(dates, humidity):
    humidity = np.array(humidity)

    # dynamic y axis
    ymin = humidity.min() - 5
    ymax = humidity.max() + 5

    plt.figure(figsize=(10,5))
    plt.plot(dates, humidity, marker='o')

    plt.ylim(ymin, ymax)
    plt.title("Past 10 Days Humidity Trend")
    plt.xlabel("Date")
    plt.ylabel("Humidity (%)")
    plt.xticks(rotation=45)

    plt.tight_layout()
    plt.show()

# predict tomorrows value using linear regression
def predict(values):
    X = np.arange(len(values)).reshape(-1,1)
    y = np.array(values)

    model = LinearRegression()
    model.fit(X, y)

    return model.predict([[len(values)]])[0]

# main function
def main():
    print(f"Fetching weather data for {CITY}...")
    lat, lon = get_coordinates(CITY)

    dates, temps, humidity = fetch_weather(lat, lon, days=10)

    print("\nShowing last 10 days graphs...")

    plot_temperature(dates, temps)
    plot_humidity(dates, humidity)

    temp_pred = predict(temps)
    hum_pred = predict(humidity)

    print("\nTomorrow Prediction:")
    print(f"Temperature: {temp_pred:.2f} C")
    print(f"Humidity: {hum_pred:.2f} %")

if __name__ == "__main__":
    main()