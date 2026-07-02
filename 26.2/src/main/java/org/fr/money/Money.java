package org.fr.money;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Money extends JavaPlugin {

    private static Money instance;
    private File moneyFile;
    private FileConfiguration moneyConfig;
    private final Object lock = new Object();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        moneyFile = new File(getDataFolder(), "money.yml");
        if (!moneyFile.exists()) {
            getDataFolder().mkdir();
            try {
                moneyFile.createNewFile();
            }
            catch (IOException ex) {
                getLogger().severe("Failed to create money.yml");
            }
        }
        this.moneyConfig = YamlConfiguration.loadConfiguration(moneyFile);
        Bukkit.getPluginManager().registerEvents(new Listener(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    MoneyCommand.build(this),
                    "Argent",
                    java.util.List.of()
            );
        });
    }

    @Override
    public void onDisable() {

    }

    public File getMoneyFile() {
        return moneyFile;
    }

    public FileConfiguration getMoneyConfig() {
        return moneyConfig;
    }

    public void saveMoneyConfig() {
        try {
            moneyConfig.save(moneyFile);
        }
        catch (IOException ex) {
            getLogger().severe("Failed to save money.yml");
        }
    }

    public double getMoney(Player player) {
        synchronized (lock) {
            double money = getMoneyConfig().getDouble("player." + player.getUniqueId().toString() + ".money");
            return money;
        }
    }

    public void  setMoney(Player player, double money) {
        synchronized (lock) {
            getMoneyConfig().set("player." + player.getUniqueId().toString() + ".money", money);
            saveMoneyConfig();
        }

        if (player.isOnline()) {
            MoneyBossBarManager.update(player, money);
        }
    }

    public boolean hasMoney(Player player, double money) {
        synchronized (lock) {
            double playerMoney = getMoney(player);
            if (playerMoney >= money) {
                return true;
            }
            return false;
        }
    }

    public void addMoney(Player player, double addedMoney) {
        synchronized (lock) {
            double money = getMoney(player);
            double newMoney = money + addedMoney;
            setMoney(player, newMoney);
        }
    }

    public boolean removeMoney(Player player, double removedMoney) {
        synchronized (lock) {
            double money = getMoney(player);
            if (money < removedMoney) {
                return false;
            }
            double newMoney = money - removedMoney;
            setMoney(player, newMoney);
            return true;
        }
    }

    public int getMoneyFormatMode() {
        return getConfig().getInt("money-format-mode", 0);
    }

    public static Money getInstance() {
        return instance;
    }

    public String getMoneySymbol() {
        return getConfig().getString("texts.symbol", "$");
    }
}
