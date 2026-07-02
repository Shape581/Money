package org.fr.money;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MoneyBossBarManager {

    private static final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();

    private MoneyBossBarManager() {

    }

    public static void show(Player player, double balance) {
        BossBar bossBar = BossBar.bossBar(
                formatTitle(balance),
                1.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );

        activeBossBars.put(player.getUniqueId(), bossBar);
        player.showBossBar(bossBar);
    }

    public static void update(Player player, double money) {
        BossBar bossBar = activeBossBars.get(player.getUniqueId());
        if (bossBar != null) {
            bossBar.name(formatTitle(money));
        }
    }

    public static void hide(Player player) {
        BossBar bossBar = activeBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    private static Component formatTitle(double money) {
        String formattedMoney = MoneyFormatter.format(money);
        return Component.text(formattedMoney + "€").color(NamedTextColor.GOLD);
    }
}