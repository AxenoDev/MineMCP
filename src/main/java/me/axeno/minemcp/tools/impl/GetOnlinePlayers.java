package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;
import org.bukkit.Bukkit;

public class GetOnlinePlayers extends McpTool
{

    public GetOnlinePlayers()
    {
        super("get_online_players", "Get a list of online players");
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        var players = Bukkit.getOnlinePlayers();
        if (players.isEmpty())
        {
            sb.append("No players are currently online.");
        } else
        {
            sb.append("Online Players:\n");
            for (var player : players)
            {
                sb.append("- ").append(player.getName()).append("\n");
            }
        }

        return createTextResult(sb.toString());
    }

}
