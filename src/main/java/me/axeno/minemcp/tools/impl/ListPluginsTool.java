package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.MineMCP;
import me.axeno.minemcp.tools.McpTool;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ListPluginsTool extends McpTool
{

    public ListPluginsTool()
    {
        super("list_plugins", "List installed plugins");
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
        CompletableFuture<String> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(MineMCP.getInstance(), () ->
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                for (Plugin p : Bukkit.getPluginManager().getPlugins())
                {
                    sb.append(p.getName()).append(" (").append(p.getPluginMeta().getVersion()).append(")");
                    if (!p.isEnabled())
                        sb.append(" [DISABLED]");
                    sb.append("\n");
                }
                future.complete(sb.toString());
            } catch (Exception e)
            {
                future.completeExceptionally(e);
            }
        });

        return createTextResult(future.get(10, TimeUnit.SECONDS));
    }
}
