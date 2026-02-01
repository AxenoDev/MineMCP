package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ReadFileBase64Tool extends McpTool
{
    private final Path serverRoot;

    public ReadFileBase64Tool()
    {
        super("read_file_base64", "Read a binary file and return as base64");
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
        String pathStr = args.get("path").asText();
        Path path = serverRoot.resolve(pathStr).normalize();
        if (!path.startsWith(serverRoot))
        {
            throw new SecurityException("Access denied: Path is outside server root.");
        }
        if (!Files.exists(path))
        {
            throw new IllegalArgumentException("File not found: " + pathStr);
        }

        byte[] bytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return createTextResult(base64);
    }
}
