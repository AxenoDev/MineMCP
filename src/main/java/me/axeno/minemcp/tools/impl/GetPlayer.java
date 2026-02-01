package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GetPlayer extends McpTool
{
    public GetPlayer()
    {
        super("get_player", "Get detailed information about a player");
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("required", mapper.createArrayNode().add("player"));
        ObjectNode props = mapper.createObjectNode();
        props.set("player", mapper.createObjectNode().put("type", "string").put("description",
                "The player to get information"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        Player player = Bukkit.getPlayer(args.get("player").asText());

        if (player == null) return createTextResult("Player not found");

        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(player.getName()).append("\n");
        info.append("Display Name: ").append(player.displayName()).append("\n");
        info.append("UUID: ").append(player.getUniqueId()).append("\n");

        var maxHealthAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
        info.append("Health: ").append(player.getHealth()).append("/").append(maxHealth).append("\n");
        info.append("Food Level: ").append(player.getFoodLevel()).append("/20\n");
        info.append("Level: ").append(player.getLevel()).append(" (XP: ").append(player.getTotalExperience()).append(")\n");
        info.append("Game Mode: ").append(player.getGameMode()).append("\n");
        info.append("Flying: ").append(player.isFlying()).append(" (Can Fly: ").append(player.getAllowFlight()).append(")\n");
        info.append("Location: ").append(player.getLocation().getWorld().getName())
                .append(" (X: ").append(String.format("%.2f", player.getLocation().getX()))
                .append(", Y: ").append(String.format("%.2f", player.getLocation().getY()))
                .append(", Z: ").append(String.format("%.2f", player.getLocation().getZ())).append(")\n");
        info.append("Rotation: ").append("Yaw: ").append(String.format("%.2f", player.getLocation().getYaw()))
                .append(" Pitch: ").append(String.format("%.2f", player.getLocation().getPitch()));
        info.append("IP Address: ").append(player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "N/A").append("\n");
        info.append("Op: ").append(player.isOp()).append("\n");
        info.append("Online: ").append(player.isOnline()).append("\n");
        info.append("Ping: ").append(player.getPing()).append("ms\n");
        info.append("Walk Speed: ").append(player.getWalkSpeed()).append("\n");
        info.append("Fly Speed: ").append(player.getFlySpeed()).append("\n");
        info.append("Item in Hand: ").append(player.getInventory().getItemInMainHand().getType()).append("\n");
        info.append("Off Hand: ").append(player.getInventory().getItemInOffHand().getType()).append("\n");
        info.append("Armor: ");
        if (player.getInventory().getHelmet() != null)
            info.append("Helmet(").append(player.getInventory().getHelmet().getType()).append(") ");
        if (player.getInventory().getChestplate() != null)
            info.append("Chestplate(").append(player.getInventory().getChestplate().getType()).append(") ");
        if (player.getInventory().getLeggings() != null)
            info.append("Leggings(").append(player.getInventory().getLeggings().getType()).append(") ");
        if (player.getInventory().getBoots() != null)
            info.append("Boots(").append(player.getInventory().getBoots().getType()).append(")");
        info.append("\n");

        if (!player.getActivePotionEffects().isEmpty())
        {
            info.append("\n=== Active Effects ===\n");
            player.getActivePotionEffects().forEach(effect ->
                    info.append("- ").append(effect.getType().getKey().getKey())
                            .append(" (Level ").append(effect.getAmplifier() + 1)
                            .append(", Duration: ").append(effect.getDuration() / 20).append("s)\n")
            );
        }

        return createTextResult(info.toString());
    }
}
