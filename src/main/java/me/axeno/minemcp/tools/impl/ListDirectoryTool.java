package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ListDirectoryTool extends McpTool
{
    private final Path serverRoot;

    public ListDirectoryTool()
    {
        super("list_directory", "List files and directories in a path");
        this.serverRoot = new File(".").toPath().toAbsolutePath().normalize();
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("required", mapper.createArrayNode().add("path"));
        ObjectNode props = mapper.createObjectNode();
        props.set("path", mapper.createObjectNode().put("type", "string"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        String pathStr = args.has("path") ? args.get("path").asText() : ".";
        Path path = serverRoot.resolve(pathStr).normalize();
        if (!path.startsWith(serverRoot))
        {
            throw new SecurityException("Access denied: Path is outside server root.");
        }
        if (!Files.exists(path))
        {
            throw new IllegalArgumentException("Directory not found: " + pathStr);
        }
        if (!Files.isDirectory(path))
        {
            throw new IllegalArgumentException("Not a directory: " + pathStr);
        }

        StringBuilder sb = new StringBuilder();
        try (var stream = Files.list(path))
        {
            stream.sorted().forEach(p ->
            {
                String name = p.getFileName().toString();
                if (Files.isDirectory(p))
                {
                    sb.append("[DIR]  ").append(name).append("/\n");
                } else
                {
                    try
                    {
                        long size = Files.size(p);
                        sb.append("[FILE] ").append(name).append(" (").append(formatSize(size)).append(")\n");
                    } catch (Exception e)
                    {
                        sb.append("[FILE] ").append(name).append("\n");
                    }
                }
            });
        }
        return createTextResult(sb.toString());
    }

    private String formatSize(long bytes)
    {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
