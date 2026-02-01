package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.McpTool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public class WriteFileBase64Tool extends McpTool
{
    private final Path serverRoot;

    public WriteFileBase64Tool()
    {
        super("write_file_base64", "Write a binary file from base64 encoded content");
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
        props.set("content", mapper.createObjectNode().put("type", "string").put("description", "Base64 encoded file content"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        String pathStr = args.get("path").asText();
        String base64Content = args.get("content").asText();

        Path path = serverRoot.resolve(pathStr).normalize();
        if (!path.startsWith(serverRoot))
        {
            throw new SecurityException("Access denied: Path is outside server root.");
        }

        byte[] bytes = Base64.getDecoder().decode(base64Content);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return createTextResult("Binary file written successfully to " + pathStr + " (" + bytes.length + " bytes)");
    }
}
