package cs.bsu.edu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import papertrader.engine.MarketSystem;
import papertrader.engine.PaperTrader;
import papertrader.player.*;

import java.util.ArrayList;
import java.util.HashMap;

public class CLI {


    @Test
    public void DefaultPlayer() {
        Player player = Player.get();
        player.loadDefaultData(); // Player class should load default data
        // test case makes sure it will load
        System.out.println(player.portfolio.getMoney());
        Assertions.assertEquals(100000.0, player.portfolio.getMoney());
    }

    @Test
    public void loadPlayer() {
        Player player = Player.get();
        player.loadData();
        System.out.println(player.portfolio.getMoney());
        Assertions.assertTrue(player.portfolio.getMoney() > 1);
    }

    @Test
    public void LoadStocks() {
        MarketSystem market = MarketSystem.get();
        market.loadDefaultData();
        Assertions.assertFalse(market.stockList.isEmpty());
    }


    @Test
    public void IncrementStocks() {
        MarketSystem market = MarketSystem.get();
        market.loadDefaultData();
        double oldShareValue = market.stockList.get("NVDA").shareValue;

        market.incrementStocks();

        Assertions.assertTrue(oldShareValue != market.stockList.get("NVDA").shareValue);
    }

    @Test
    public void BuyStock() {
        Player player = Player.get();
        player.loadDefaultData();
        MarketSystem market = MarketSystem.get();
        market.loadDefaultData();
        player.portfolio.buyStock("NVDA", 1);
        ArrayList<MarketSystem.Trade> trades = player.portfolio.getTrades();
        System.out.println(trades);
        Assertions.assertEquals(1, trades.size());
    }

    @Test
    public void ShortStock() {
        Player player = Player.get();
        player.loadDefaultData();
        MarketSystem market = MarketSystem.get();
        market.loadDefaultData();
        player.portfolio.shortStock("NVDA", 1);
        player.portfolio.sellStock("NVDA", 1);
        Assertions.assertEquals(1, player.portfolio.getTrades().size());
    }

    @Test
    public void NormalSimulation() {
        Player player = Player.get();
        MarketSystem market = MarketSystem.get();

        market.loadDefaultData();
        player.loadDefaultData();

        Double money = player.portfolio.getMoney();

        player.portfolio.buyStock("NVDA", 100);

        for (int i = 1; i < 100; i++) {
            market.incrementStocks();
        }

        player.portfolio.sellStock("NVDA", 100);

        System.out.println(money);
        System.out.println(player.portfolio.getMoney());

        Assertions.assertTrue(player.portfolio.getMoney() != money);
    }
}
