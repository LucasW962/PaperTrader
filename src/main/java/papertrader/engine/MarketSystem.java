package papertrader.engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Random;

public class MarketSystem {

    private static final MarketSystem marketSystem = new MarketSystem();

    public HashMap<String, Stock> stockList = new HashMap<>();

    public static MarketSystem get() {
        return marketSystem;
    }

    public void saveData() {
        try {
            if (getStockData().createNewFile()) {
                System.out.println("Created Player Data File.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(getStockData())) {
            gson.toJson(this.stockList, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadDefaultData() {
        Gson gson = new Gson();

        Type mapType = new TypeToken<HashMap<String, Stock>>(){}.getType();

        try {
            Reader reader = new FileReader(getDefaultStockData());
            JsonElement element = JsonParser.parseReader(reader);
            this.stockList = gson.fromJson(element, mapType);

            this.stockList.forEach((string, stock) -> {
                stock.averageGrowth += 0.025; // Skew average growth
            });

        } catch (FileNotFoundException e) {
            // If no data, create an empty stock list
            System.out.println("No Stock data found!");
            throw new RuntimeException();
        }
    }

    public void loadData() {
        Gson gson = new Gson();

        Type mapType = new TypeToken<HashMap<String, Stock>>(){}.getType();

        try {
            Reader reader = new FileReader(getStockData());
            JsonElement element = JsonParser.parseReader(reader);
            this.stockList = gson.fromJson(element, mapType);
        } catch (FileNotFoundException e) {
            // If no data, create an empty stock list
            System.out.println("No Stock data found!");
            throw new RuntimeException();
        }
    }

    public void incrementStocks() {
        Random random = new Random();

        MarketSystem.get().stockList.forEach((string, stock) -> {
            double value = stock.averageGrowth + random.nextGaussian() * stock.deviation;
            stock.shareValue += value;

            if (random.nextFloat() < 0.025) { // Small chance to grow or shrink stock
                stock.averageGrowth += (random.nextDouble() * 2.0 - 1.0) * 0.01;
            }
        });
    }

    private File getStockData() {
        String workingDirectory = System.getProperty("user.dir");
        return new File(workingDirectory + "/data/StockData.json");
    }

    private File getDefaultStockData() {
        String workingDirectory = System.getProperty("user.dir");
        return new File(workingDirectory + "/data/DefaultStockData.json");
    }

    public static class Stock {
        public double averageGrowth;
        public double deviation;
        public double shareValue;
        public double shares;

        @Override
        public String toString() {
            return "Average Growth: " + averageGrowth + "\n" +
                    "Deviation: " + deviation + "\n" +
                    "Price: " + shareValue + "\n" +
                    "Volume: " + shares;
        }
    }

    public enum TradeType {
        BUY,
        SELL,
        SHORT
    }

    public static class Trade {
        public String name;
        public double shares;
        public double shareValue;
        public TradeType type;

        @Override
        public String toString() {
            return "Stock Name: " + name + "\n" +
                    "Amount of Shares: " + shares + "\n" +
                    "Share Value: " + shareValue + "\n" +
                    "Type: " + type;
        }
    }

}