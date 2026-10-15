package eu.crystalmc.weapons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.crystalmc.weapons.BulletHitDecals;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class RemoveBulletHitDecalsCommand {

    public LiteralCommandNode<CommandSourceStack> getCommand(String permission) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("removebullethitdecals")
            .requires(source -> source.getSender().hasPermission(permission))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    BulletHitDecals.removeAll();
                    sender.sendMessage(Component.text("Removed all loaded bullet hit decals").color(NamedTextColor.WHITE));
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
