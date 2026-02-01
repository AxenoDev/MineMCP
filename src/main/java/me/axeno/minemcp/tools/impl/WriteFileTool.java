package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WriteFileTool extends McpTool
{
    private final Path serverRoot;

    public WriteFileTool()
    {
        super("write_file", "Write to a file on the server");
        this.serverRoot = new File(".").toPath().toAbsolutePath().normalize();
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("required", mapper.createArrayNode().add("path").add("content"));
        ObjectNode props = mapper.createObjectNode();
        props.set("path", mapper.createObjectNode().put("type", "string"));
        props.set("content", mapper.createObjectNode().put("type", "string"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        String pathStr = args.get("path").asText();
        String content = args.get("content").asText();

        Path path = serverRoot.resolve(pathStr).normalize();
        if (!path.startsWith(serverRoot))
        {
            throw new SecurityException("Access denied: Path is outside server root.");
        }

        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        return createTextResult("File written successfully to " + pathStr);
    }
}
