package papertrader.player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import papertrader.engine.MarketSystem;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Player {

    private static final Player player = new Player();

    public Portfolio portfolio = new Portfolio();

    public static Player get() {
        return player;
    }

    public static File getSaveFile() {
        String workingDirectory = System.getProperty("user.dir");
        return new File(workingDirectory + "\\data\\PlayerData.json");
    }

    public void saveData() {
        File playerData = getSaveFile();
        try {
            if (playerData.createNewFile()) {
                System.out.println("Created Player Data File.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(playerData)) {
            gson.toJson(this.portfolio, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadData() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(getSaveFile())) {
            this.portfolio = gson.fromJson(reader, Portfolio.class);
        } catch (IOException e) {
            System.out.println("Error loading portfolio!");
        }
    }

    public void loadDefaultData() {
        this.portfolio.money = 100000;
        this.portfolio.trades.clear();
        this.portfolio.ownedStocks.clear();
    }



    public static class Portfolio {
        private double money;
        private final ArrayList<MarketSystem.Trade> trades = new ArrayList<>();
        private final HashMap<String, Double> ownedStocks = new HashMap<>();

        public double getMoney() { return this.money; }

        public double getTotalMoney() {

            // Get value of all stocks after incrementing them by a day
            AtomicReference<Double> totalInvestment = new AtomicReference<>();
            totalInvestment.set(0.0);

            MarketSystem.get().stockList.forEach((string, stock) -> totalInvestment.set(totalInvestment.get() + Player.get().portfolio.getMoneyInStock(string)));

            return this.money + totalInvestment.get();
        }

        public void addMoney(int amount) {
            this.money += amount;
        }

        public ArrayList<MarketSystem.Trade> getTrades() {
            return trades;
        }

        public double getNumberOfShares(String stockName) {
            return ownedStocks.getOrDefault(stockName, 0.0);
        }

        public double getMoneyInStock(String stockName) {
            return ownedStocks.getOrDefault(stockName, 0.0) * MarketSystem.get().stockList.get(stockName).shareValue;
        }

        public boolean ownsStock(String stockName) {
            return ownedStocks.containsKey(stockName);
        }

        public boolean isEmpty() {
            return ownedStocks.isEmpty();
        }

        public void buyStock(String stockName, double amountOfShares) {
            if (!MarketSystem.get().stockList.containsKey(stockName)) {
                System.out.println("Stock " + stockName + " does not exist!");
                return;
            }

            MarketSystem.Stock stock = MarketSystem.get().stockList.get(stockName);

            double moneyRequired = amountOfShares * stock.shareValue;

            if (this.money < moneyRequired) {
                System.out.println("Not enough money!");
                return;
            }

            this.money -= moneyRequired;

            // Add to owned stocks
            {
                double value = amountOfShares;

                if (ownedStocks.containsKey(stockName)) {
                    value += ownedStocks.get(stockName);
                }

                ownedStocks.put(stockName, value);
            }

            // Add trade
            {
                MarketSystem.Trade trade = new MarketSystem.Trade();
                trade.name = stockName;
                trade.shares = amountOfShares;
                trade.shareValue = stock.shareValue;
                trade.type = MarketSystem.TradeType.BUY;

                trades.add(trade);
            }
        }

        public void sellStock(String stockName, double amountOfShares) {
            if (!MarketSystem.get().stockList.containsKey(stockName)) {
                System.out.println("Stock " + stockName + " does not exist!");
                return;
            }

            if (!this.ownedStocks.containsKey(stockName)) {
                System.out.println("You do not own any of " + stockName + "!");
                return;
            }

            if (this.ownedStocks.get(stockName) < amountOfShares) {
                System.out.println("You do not have enough of " + stockName + "!");
                return;
            }

            MarketSystem.Stock stock = MarketSystem.get().stockList.get(stockName);

            double moneyGained = amountOfShares * stock.shareValue;

            this.money += moneyGained;

            // Remove from owned stocks
            {
                ownedStocks.compute(stockName, (_, amount) -> amount - amountOfShares);
            }

            // Add trade
            {
                MarketSystem.Trade trade = new MarketSystem.Trade();
                trade.name = stockName;
                trade.shares = amountOfShares;
                trade.shareValue = stock.shareValue;
                trade.type = MarketSystem.TradeType.SELL;

                trades.add(trade);
            }
        }

        public void shortStock(String stockName, double amountOfShares) {

        }

        public void removeMoney(int amount) {
            this.money -= amount;
        }
    }
}
