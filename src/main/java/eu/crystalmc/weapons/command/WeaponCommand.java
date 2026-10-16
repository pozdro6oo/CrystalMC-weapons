package eu.crystalmc.weapons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tr7zw.nbtapi.NBT;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.WeaponsRegistry;
import eu.crystalmc.weapons.config.Config;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WeaponCommand {
    public LiteralCommandNode<CommandSourceStack> getCommand(CrystalWeapons plugin) {
        Config.Data config = plugin.getConfiguration();
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("weapon")
            .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.command))
            .then(Commands.literal("give")
                    .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.give))
                    .then(Commands.argument("entities", ArgumentTypes.entities())
                        .then(Commands.argument("weapon", StringArgumentType.word())
                                .suggests( (_, builder) -> suggestWeapons(builder))
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    String weapon = context.getArgument("weapon", String.class);
                                    List<Entity> entities = context.getArgument("entities", EntitySelectorArgumentResolver.class).resolve(context.getSource());

                                    if (!WeaponsRegistry.getWeapons().containsKey(weapon)) {
                                        sender.sendMessage(Component.text("Unknown weapon '" + weapon + "'").color(NamedTextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    giveWeapon(sender, weapon, entities);

                                    return Command.SINGLE_SUCCESS;
                                })
                    ))
            )
            .then(getModifyArgument(plugin, config));


        return command.build();
    }


    private LiteralArgumentBuilder<CommandSourceStack> getModifyArgument(CrystalWeapons plugin, Config.Data config) {
        LiteralArgumentBuilder<CommandSourceStack> argument = Commands.literal("modify")
                .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.modify));

        for (Property propery : Property.values()) {
            argument.then(propery.getArgument(plugin));
        }

        return argument;
    }



    private void giveWeapon(CommandSender sender, String weapon, List<Entity> entities) {

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
            sender.sendMessage(Component.text("Gave ").append(getWeaponToComponent(item)).append(Component.text(" to " + entities.size() + " entities")));
        }
        if (entities.size() == 1) {
            sender.sendMessage("Gave " + weapon + " to " + entities.getFirst().getName());
        }
        if (entities.isEmpty()) {
            sender.sendMessage("&cNo entity was found.");
        }
    }

    private CompletableFuture<Suggestions> suggestWeapons(SuggestionsBuilder builder) {
        WeaponsRegistry.getWeapons().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static Component getWeaponToComponent(ItemStack weapon) {
        return Component.text("")
                .append(weapon.displayName().hoverEvent(HoverEvent.showText(weapon.getItemMeta().customName().appendNewline().append(
                                Component.join(
                                        JoinConfiguration.separator(Component.newline()),
                                        weapon.lore()
                                ))
                        ))
                )
                .append(Component.text(""));
    }


    private enum Property {
        infinite_ammunition(BoolArgumentType.bool(), Boolean.class),
        automatic(BoolArgumentType.bool(), Boolean.class),
        max_ammunition(IntegerArgumentType.integer(1), int.class),
        max_distance(IntegerArgumentType.integer(1), int.class),
        shot_delay(IntegerArgumentType.integer(1),int.class),
        recoil(FloatArgumentType.floatArg(),float.class),
        damage(DoubleArgumentType.doubleArg(1), double.class),
        inaccuracy(DoubleArgumentType.doubleArg(0), double.class),
        reload_time(IntegerArgumentType.integer(0),int.class),
        bullets_per_shot(IntegerArgumentType.integer(1), int.class);


        private final String path;
        private final Class<?> tClass;
        private final ArgumentType<?> type;


        <T> Property(String path, ArgumentType<T> type, Class<T> tClass) {
            this.path = path;
            this.type = type;
            this.tClass = tClass;
        }

        <T> Property(ArgumentType<T> type, Class<T> tClass) {
            this.type = type;
            this.path = this.name();
            this.tClass = tClass;
        }



        private <T> LiteralArgumentBuilder<CommandSourceStack> getArgument(CrystalWeapons plugin) {

            return Commands.literal(this.name())
                    .then(Commands.argument("value", this.type)
                    .executes(context -> {

                        CommandSender sender = context.getSource().getSender();

                        @SuppressWarnings("unchecked cast")
                        T object = (T) context.getArgument("value", this.tClass);

                        if (sender instanceof Player) {
                            ItemStack item = ((Player) sender).getEquipment().getItem(EquipmentSlot.HAND);
                            if (item.getType() == Material.AIR) {
                                sender.sendMessage(Component.text("You are not holding an item").color(NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                            NBT.modify(item, nbt -> {
                                var compound = nbt.getCompound(plugin.COMPOUND);
                                if (compound != null) {
                                    if (tClass == int.class || tClass == Integer.class) {
                                        compound.setInteger(this.path, ((Number) object).intValue());
                                    } else if (tClass == double.class || tClass == Double.class) {
                                        compound.setDouble(this.path, ((Number) object).doubleValue());
                                    } else if (tClass == float.class || tClass == Float.class) {
                                        compound.setFloat(this.path, ((Number) object).floatValue());
                                    } else if (tClass == boolean.class || tClass == Boolean.class) {
                                        compound.setBoolean(this.path, (Boolean) object);
                                    } else if (tClass == String.class) {
                                        compound.setString(this.path, (String) object);
                                    } else {
                                        return;
                                    }


                                } else {
                                    sender.sendMessage(Component.text("Item is not a weapon").color(NamedTextColor.RED));
                                    return;
                                }
                            });

                            sender.sendMessage(Component.text("Property ")
                                    .append(Component.text(this.name()).hoverEvent(HoverEvent.showText(Component.text(this.path))))
                                    .append(Component.text(" of "))
                                    .append(WeaponCommand.getWeaponToComponent(item))
                                    .append(Component.text(" is now set to "))
                                    .append(Component.text(object.toString()))

                            );

                        } else {
                            sender.sendMessage(Component.text("A player is required to run this command here").color(NamedTextColor.RED));
                        }


                        return Command.SINGLE_SUCCESS;
                    }));
        }

    }

}
