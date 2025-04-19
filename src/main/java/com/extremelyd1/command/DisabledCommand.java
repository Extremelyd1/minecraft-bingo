package com.extremelyd1.command;

import com.extremelyd1.util.ChatUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class DisabledCommand implements BasicCommand {

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        commandSourceStack.getSender().sendMessage(ChatUtil.errorPrefix().append(Component
                .text("This command is currently disabled")
                .color(NamedTextColor.WHITE)
        ));
    }
}
