package org.fr.money;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class MoneyCommand {

    public static LiteralCommandNode<CommandSourceStack> build(Money plugin) {
        return literal("money").requires(source -> source.getSender().hasPermission("money.balance"))
                .executes(ctx -> {
                    var sender = ctx.getSource().getSender();

                    if (!(sender instanceof Player player)) {
                        return 0;
                    }

                    double money = plugin.getMoney(player);
                    String formattedMoney = MoneyFormatter.format(money);
                    player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.balance", "<gold>Bank</gold> <gray>-></gray> You have <gold>%MONEY%</gold>.")
                            .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())));

                    return 1;
                })
                .then(literal("send").requires(source -> source.getSender().hasPermission("money.send"))
                        .then(argument("player", ArgumentTypes.player())
                                .then(argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(ctx -> {
                                            var sender = ctx.getSource().getSender();

                                            if (!(sender instanceof Player from)) {
                                                return 0;
                                            }

                                            PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);

                                            Player to;
                                            try {
                                                to = resolver.resolve(ctx.getSource()).getFirst();
                                            } catch (Exception e) {
                                                from.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.player.not.found", "<gold>Bank</gold> <gray>-></gray> <red>Player %PLAYER% not found.</red>")
                                                        .replace("%PLAYER%", ctx.getArgument("player", String.class))));
                                                return 0;
                                            }

                                            if (to.getUniqueId().equals(from.getUniqueId())) {
                                                from.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.cannot.send.to.self", "<gold>Bank</gold> <gray>-></gray> <red>You cannot send money to yourself.</red>")));
                                                return 0;
                                            }

                                            double money = DoubleArgumentType.getDouble(ctx, "amount");
                                            String formattedMoney = MoneyFormatter.format(money);

                                            boolean success = plugin.removeMoney(from, money);

                                            if (!success) {
                                                from.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.not.enough.money", "<gold>Bank</gold> <gray>-></gray> <red>You do not have enough money to send %MONEY%.</red>")
                                                        .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())));
                                                return 0;
                                            }

                                            plugin.addMoney(to, money);

                                            from.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.send.success.sender", "<gold>Bank</gold> <gray>-></gray> You sent <gold>%MONEY%</gold> to <gold>%PLAYER%</gold>!")
                                                    .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())
                                                    .replace("%PLAYER%", to.getName())));

                                            to.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.send.success.receiver", "<gold>Bank</gold> <gray>-></gray> You received <gold>%MONEY%</gold> from <gold>%PLAYER%</gold>!")
                                                    .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())
                                                    .replace("%PLAYER%", from.getName())));

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(literal("give").requires(source -> source.getSender().hasPermission("money.give"))
                        .then(argument("player", ArgumentTypes.player())
                                .then(argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(ctx -> {
                                            var sender = ctx.getSource().getSender();

                                            if (!(sender instanceof Player player)) {
                                                return 0;
                                            }

                                            PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);

                                            Player to;
                                            try {
                                                to = resolver.resolve(ctx.getSource()).getFirst();
                                            } catch (Exception e) {
                                                player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.player.not.found.offline", "<gold>Bank</gold> <gray>-></gray> <red>Player not found or offline.</red>")));
                                                return 0;
                                            }

                                            double money = DoubleArgumentType.getDouble(ctx, "amount");
                                            String formattedMoney = MoneyFormatter.format(money);

                                            plugin.addMoney(to, money);

                                            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.give.success.sender", "<gold>Bank</gold> <gray>-></gray> You gave <gold>%MONEY%</gold> to <gold>%PLAYER%</gold>!")
                                                    .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())
                                                    .replace("%PLAYER%", to.getName())));

                                            to.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.give.success.receiver", "<gold>Bank</gold> <gray>-></gray> You received <gold>%MONEY%</gold>!")
                                                    .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())));

                                            return 1;
                                        })
                                )
                        )
                )
                .then(literal("set").requires(source -> source.getSender().hasPermission("money.set"))
                        .then(argument("player", ArgumentTypes.player())
                                .then(argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(ctx -> {
                                            var sender = ctx.getSource().getSender();

                                            if (!(sender instanceof Player player)) {
                                                return 0;
                                            }

                                            PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);

                                            Player to;
                                            try {
                                                to = resolver.resolve(ctx.getSource()).getFirst();
                                            } catch (Exception e) {
                                                player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.player.not.found.offline", "<gold>Bank</gold> <gray>-></gray> <red>Player not found or offline.</red>")));
                                                return 0;
                                            }

                                            double money = DoubleArgumentType.getDouble(ctx, "amount");
                                            String formattedMoney = MoneyFormatter.format(money);

                                            plugin.setMoney(to, money);

                                            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.set.success.sender", "<gold>Bank</gold> <gray>-></gray> You set <gold>%PLAYER%</gold>'s money to <gold>%MONEY%</gold>!")
                                                    .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())
                                                    .replace("%PLAYER%", to.getName())));

                                            to.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("texts.set.success.receiver", "<gold>Bank</gold> <gray>-></gray> Your money is now <gold>%MONEY%</gold>!")
                                                    .replace("%MONEY%", formattedMoney + plugin.getMoneySymbol())));

                                            return 1;
                                        })
                                )
                        )
                ).build();
    }
}