package me.axeno.minemcp.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class McpCommandSender implements CommandSender
{
    private final Server server;
    private final StringBuilder output = new StringBuilder();
    private final CommandSender wrappedSender;

    public McpCommandSender(CommandSender wrappedSender)
    {
        this.server = Bukkit.getServer();
        this.wrappedSender = wrappedSender;
    }

    public String getOutput()
    {
        return output.toString();
    }

    private final CommandSender.Spigot spigotProxy = new CommandSender.Spigot()
    {
        @Override
        public void sendMessage(BaseComponent... components)
        {
            try
            {
                String text = TextComponent.toLegacyText(components);
                if (!text.isEmpty())
                {
                    output.append(text).append('\n');
                }
            }
            catch (Throwable ignored)
            {
            }

            wrappedSender.spigot().sendMessage(components);
        }
    };

    @Override
    public void sendMessage(@NotNull String message)
    {
        output.append(message).append("\n");
    }

    @Override
    public void sendMessage(@NotNull String... messages)
    {
        for (String message : messages)
        {
            output.append(message).append("\n");
        }
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message)
    {
        output.append(message).append("\n");
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages)
    {
        for (String message : messages)
        {
            output.append(message).append("\n");
        }
    }

    @Override
    public @NotNull Server getServer()
    {
        return server;
    }

    @Override
    public @NotNull String getName()
    {
        return "MineMCP_Console";
    }

    @Override
    public @NotNull Spigot spigot()
    {
        return spigotProxy;
    }

    @Override
    public @NotNull Component name()
    {
        return Component.text(getName());
    }

    public void sendMessage(@NotNull Component message)
    {
        String plain = PlainTextComponentSerializer.plainText().serialize(message);
        if (!plain.isEmpty())
        {
            output.append(plain).append("\n");
        }

        wrappedSender.sendMessage(plain);
    }

    public void sendMessage(@Nullable UUID sender, @NotNull Component message)
    {
        String plain = PlainTextComponentSerializer.plainText().serialize(message);
        if (!plain.isEmpty())
        {
            output.append(plain).append("\n");
        }
        wrappedSender.sendMessage(plain);
    }

    @Override
    public boolean isPermissionSet(@NotNull String name)
    {
        return wrappedSender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm)
    {
        return wrappedSender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(@NotNull String name)
    {
        return wrappedSender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm)
    {
        return wrappedSender.hasPermission(perm);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value)
    {
        return wrappedSender.addAttachment(plugin, name, value);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin)
    {
        return wrappedSender.addAttachment(plugin);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks)
    {
        return wrappedSender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks)
    {
        return wrappedSender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment)
    {
        wrappedSender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions()
    {
        wrappedSender.recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return wrappedSender.getEffectivePermissions();
    }

    @Override
    public boolean isOp()
    {
        return true;
    }

    @Override
    public void setOp(boolean value)
    {
        // No-op
    }
}
