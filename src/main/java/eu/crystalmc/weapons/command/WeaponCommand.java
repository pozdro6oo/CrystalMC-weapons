package eu.crystalmc.weapons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.crystalmc.weapons.WeaponsRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WeaponCommand {
    public LiteralCommandNode<CommandSourceStack> getCommand(String permission) {

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("weapon")
            .requires(source -> source.getSender().hasPermission(permission))
            .then(Commands.literal("give")
                    .then(Commands.argument("entities", ArgumentTypes.entities())
                        .then(Commands.argument("weapon", StringArgumentType.word())
                                .suggests( (_, builder) -> suggest(builder))
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    String weapon = context.getArgument("weapon", String.class);
                                    List<Entity> entities = context.getArgument("entities", EntitySelectorArgumentResolver.class).resolve(context.getSource());

                                    if (!WeaponsRegistry.getWeapons().containsKey(weapon)) {
                                        sender.sendMessage(Component.text("Unknown weapon '" + weapon + "'").color(NamedTextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (sender instanceof Player) {
                                        giveWeapon(sender, weapon, entities);
                                    } else {
                                        sender.sendMessage(Component.text("Unable to summon item").color(NamedTextColor.RED));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                    ))
            );

        return command.build();
    }


    private void giveWeapon(CommandSender sender,String weapon, List<Entity> entities) {

        ItemStack item = WeaponsRegistry.getWeapon(weapon);

        LivingEntity livingEntity;
        Player player;
        for (Entity entity : entities) {
            if (!entity.isDead()) {
                if (entity instanceof Player) {
                    player = (Player) entity;
                    player.give(item);
                } else if (entity instanceof LivingEntity) {
                    livingEntity = (LivingEntity) entity;
                    livingEntity.getEquipment().setItemInMainHand(item);
                }
            }
        }
        if (entities.size() > 1) {
            sender.sendMessage("Gave weapon to " + entities.size() + " entities");
        }
        if (entities.size() == 1) {
            sender.sendMessage("Gave weapon to " + entities.getFirst().getName());
        }
        if (entities.isEmpty()) {
            sender.sendMessage("&cNo entity was found.");
        }
    }

    private CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder) {
        WeaponsRegistry.getWeapons().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }
}
