package papertrader.engine;
import papertrader.player.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class PaperTrader {

    private static final List<Map.Entry<String, Supplier<Integer>>> STARTUP_ACTIONS = List.of(
            Map.entry("0) Exit Program", PaperTrader::exit),
            Map.entry("1) New Game", PaperTrader::newGame),
            Map.entry("2) Continue Game", PaperTrader::continueGame)
    );

    private static final List<Map.Entry<String, Supplier<Integer>>> ACTIONS = List.of(
            Map.entry("0) Exit Program", PaperTrader::exit),
            Map.entry("1) View Portfolio", PaperTrader::viewPortfolio),
            Map.entry("2) View Stock", PaperTrader::viewStock),
            Map.entry("3) Buy Stock", PaperTrader::buyStock),
            Map.entry("4) Sell Stock", PaperTrader::sellStock),
            Map.entry("5) Next Day", PaperTrader::nextDay)
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {

            for (Map.Entry<String, Supplier<Integer>> action : STARTUP_ACTIONS) {
                System.out.println(action.getKey());
            }

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception _) {
                System.out.println("Invalid Choice!");
                continue;
            }

            scanner.nextLine();


            Map.Entry<String, Supplier<Integer>> action = STARTUP_ACTIONS.get(choice);

            if (action.getValue().get() != 0) {
                isRunning = false;
            }
        }
    }

    public static int exit() {
        Player.get().saveData();
        MarketSystem.get().saveData();
        return 1;
    }

    public static int newGame() {
        Player.get().loadDefaultData();
        MarketSystem.get().loadDefaultData();
        return runGame();
    }

    public static int continueGame() {
        Player.get().loadData();
        MarketSystem.get().loadData();
        return runGame();
    }

    public static int runGame() {
        Scanner scanner = new Scanner(System.in);
        while (true) {

            for (Map.Entry<String, Supplier<Integer>> action : ACTIONS) {
                System.out.println(action.getKey());
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            Map.Entry<String, Supplier<Integer>> action = ACTIONS.get(choice);

            if (action.getValue().get() != 0) {
                return action.getValue().get();
            }
        }
    }

    public static int viewPortfolio() {

        Scanner scanner = new Scanner(System.in);

        System.out.println();
        MarketSystem.get().stockList.forEach((string, stock) -> {
            if (Player.get().portfolio.ownsStock(string)) {
                double valueOf = Player.get().portfolio.getMoneyInStock(string);
                double sharesOf = Player.get().portfolio.getNumberOfShares(string);

                System.out.println("You own " + sharesOf + " shares of " + string + " worth $" + valueOf + ".");
            }
        });

        printCurrentPortfolioValue();

        System.out.println("You have $" + Player.get().portfolio.getMoney() + " in liquid money.");
        System.out.println();

        System.out.println("Press ENTER to continue.");
        scanner.nextLine();

        return 0;
    }

    public static int viewStock() {
        System.out.println();
        System.out.print("Enter stock ticker: ");

        Scanner scanner = new Scanner(System.in);

        String ticker = scanner.nextLine();

        if (!MarketSystem.get().stockList.containsKey(ticker)) {
            System.out.println("Stock not found!");
            return 0;
        }

        MarketSystem.Stock stock = MarketSystem.get().stockList.get(ticker);

        String stockData = stock.toString();

        System.out.println();
        System.out.println(stockData);
        System.out.println();

        System.out.println("Press ENTER to continue.");
        scanner.nextLine();

        return 0;
    }

    public static int buyStock() {
        System.out.println();
        System.out.print("Enter stock ticker: ");

        Scanner scanner = new Scanner(System.in);

        String ticker = scanner.nextLine();

        if (!MarketSystem.get().stockList.containsKey(ticker)) {
            System.out.println("Stock not found!");
            return 0;
        }

        System.out.println();
        System.out.println();
        System.out.print("Enter number of shares: ");

        double amount;
        try {
            amount = scanner.nextDouble();
        } catch (Exception _) {
            System.out.println("Not a Number!");
            return 0;
        }
        scanner.nextLine();

        Player.get().portfolio.buyStock(ticker, amount);

        System.out.println();
        System.out.println("Press ENTER to continue.");
        scanner.nextLine();

        return 0;
    }

    public static int sellStock() {
        System.out.println();
        System.out.print("Enter stock ticker: ");

        Scanner scanner = new Scanner(System.in);

        String ticker = scanner.nextLine();

        if (!MarketSystem.get().stockList.containsKey(ticker)) {
            System.out.println("Stock not found!");
            return 0;
        }

        System.out.println();
        System.out.println();
        System.out.print("Enter number of shares: ");

        double amount;
        try {
            amount = scanner.nextDouble();
        } catch (Exception _) {
            System.out.println("Not a Number!");
            return 0;
        }
        scanner.nextLine();

        Player.get().portfolio.sellStock(ticker, amount);

        System.out.println();
        System.out.println("Press ENTER to continue.");
        scanner.nextLine();

        return 0;
    }

    public static void printCurrentPortfolioValue() {
        System.out.println();
        System.out.println("Your portfolio is worth $" + String.format("%.2f", Player.get().portfolio.getTotalMoney()) + ".");
        System.out.println();
    }

    public static int nextDay() {

        System.out.println();

        // If the player has no stocks, do not calculate money made
        if (Player.get().portfolio.isEmpty()) {
            MarketSystem.get().incrementStocks();
            System.out.println("You have no money invested.");
            System.out.println();
            return 0;
        }

        // Get value of all stocks before incrementing them by a day
        AtomicReference<Double> previousValue = new AtomicReference<>();
        previousValue.set(0.0);

        MarketSystem.get().stockList.forEach((string, stock) -> previousValue.set(previousValue.get() + Player.get().portfolio.getMoneyInStock(string)));

        MarketSystem.get().incrementStocks();

        // Get value of all stocks after incrementing them by a day
        AtomicReference<Double> nextValue = new AtomicReference<>();
        nextValue.set(0.0);

        MarketSystem.get().stockList.forEach((string, stock) -> nextValue.set(nextValue.get() + Player.get().portfolio.getMoneyInStock(string)));

        // Calculate how money changed and give a message depending on it.
        double moneyChanged = nextValue.get() - previousValue.get();

        printCurrentPortfolioValue();

        if (moneyChanged > 0.0) {
            System.out.println("You made $" + String.format("%.2f", moneyChanged) + "!");
        } else {
            System.out.println("You lost $" + String.format("%.2f", -moneyChanged) + ".");
        }

        System.out.println();

        return 0;
    }
}
