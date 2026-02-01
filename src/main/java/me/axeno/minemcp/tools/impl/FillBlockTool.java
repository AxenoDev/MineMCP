package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.MineMCP;
import me.axeno.minemcp.tools.McpTool;
import me.axeno.minemcp.utils.BlockTypeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

public class FillBlockTool extends McpTool
{

    public FillBlockTool()
    {
        super("fill_block", "Fill a block at specified coordinates");
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("required", mapper.createArrayNode().add("start_position").add("end_position").add("block_type"));
        ObjectNode props = mapper.createObjectNode();
        props.set("start_position", mapper.createObjectNode().put("type", "string").put("description",
                "Comma-separated x,y,z coordinates"));
        props.set("end_position", mapper.createObjectNode().put("type", "string").put("description",
                "Comma-separated x,y,z coordinates"));
        props.set("block_type", mapper.createObjectNode().put("type", "string").put("description",
                "Minecraft block type (e.g., 'stone')"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        String startPos = args.get("start_position").asText();
        String endPos = args.get("end_position").asText();
        String blockType = args.get("block_type").asText();

        Material material = Material.getMaterial(BlockTypeUtil.baseBlock(blockType.toUpperCase()));
        if (material == null || !material.isBlock())
        {
            throw new IllegalArgumentException("Invalid block type: " + blockType);
        }

        Bukkit.getScheduler().runTask(MineMCP.getInstance(), () ->
        {
            World world = Bukkit.getWorlds().get(0);
            String[] startParts = startPos.split(",");
            String[] endParts = endPos.split(",");

            // Pos 1
            int x1 = Integer.parseInt(startParts[0]);
            int y1 = Integer.parseInt(startParts[1]);
            int z1 = Integer.parseInt(startParts[2]);

            // Pos 2
            int x2 = Integer.parseInt(endParts[0]);
            int y2 = Integer.parseInt(endParts[1]);
            int z2 = Integer.parseInt(endParts[2]);

            // Pos 1
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);

            // Pos 2
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            for (int x = minX; x <= maxX; x++)
            {
                for (int y = minY; y <= maxY; y++)
                {
                    for (int z = minZ; z <= maxZ; z++)
                    {
                        world.getBlockAt(x, y, z).setType(material);
                    }
                }
            }
        });

        return createTextResult("Area filled with " + blockType + " from " + startPos + " to " + endPos + ".");
    }
}
