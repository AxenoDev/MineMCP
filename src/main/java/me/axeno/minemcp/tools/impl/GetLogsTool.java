package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class GetLogsTool extends McpTool
{
    private final Path serverRoot;

    public GetLogsTool()
    {
        super("get_logs", "Get recent log lines");
        this.serverRoot = new File(".").toPath().toAbsolutePath().normalize();
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
        Path logPath = serverRoot.resolve("logs/latest.log");
        if (!Files.exists(logPath))
        {
            return createTextResult("No latest.log found.");
        }

        List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
        int start = Math.max(0, lines.size() - 100);
        String recentLogs = lines.subList(start, lines.size()).stream().collect(Collectors.joining("\n"));
        return createTextResult(recentLogs);
    }
}
