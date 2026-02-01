package me.axeno.minemcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class McpTool
{
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected final String name;
    protected final String description;

    public McpTool(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public abstract ObjectNode getSchema();

    public abstract ObjectNode execute(JsonNode args) throws Exception;

    protected ObjectNode createTextResult(String text)
    {
        ObjectNode result = mapper.createObjectNode();
        result.putArray("content").addObject().put("type", "text").put("text", text);
        return result;
    }
}
