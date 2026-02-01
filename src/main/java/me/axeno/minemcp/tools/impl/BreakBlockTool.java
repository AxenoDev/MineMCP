package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.MineMCP;
import me.axeno.minemcp.tools.McpTool;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

public class BreakBlockTool extends McpTool
{

    public BreakBlockTool()
    {
        super("break_block", "Break a block at specified coordinates");
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("required", mapper.createArrayNode().add("position").add("block_type"));
        ObjectNode props = mapper.createObjectNode();
        props.set("position", mapper.createObjectNode().put("type", "string").put("description",
                "Comma-separated x,y,z coordinates"));
        props.set("block_type", mapper.createObjectNode().put("type", "string").put("description",
                "Minecraft block type (e.g., 'stone')"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        String position = args.get("position").asText();

        Bukkit.getScheduler().runTask(MineMCP.getInstance(), () ->
        {
            World world = Bukkit.getWorlds().get(0);
            String[] parts = position.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            world.getBlockAt(x, y, z).setType(Material.AIR);
        });

        return createTextResult("Block removed at " + position + ".");
    }
}
