package me.axeno.minemcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.impl.*;

import java.util.HashMap;
import java.util.Map;

public class ToolHandler
{
    private final Map<String, McpTool> tools = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ToolHandler()
    {
        registerTools(
                new PoseBlockTool(),
                new BreakBlockTool(),
                new FillBlockTool(),
                new ExecuteCommandTool(),
                new ReadFileTool(),
                new WriteFileTool(),
                new ReadFileBase64Tool(),
                new WriteFileBase64Tool(),
                new ListDirectoryTool(),
                new ListPluginsTool(),
                new GetLogsTool(),
                new GetOnlinePlayers(),
                new GetPlayer()
        );
    }

    private void registerTool(McpTool tool)
    {
        tools.put(tool.getName(), tool);
    }

    private void registerTools(McpTool... toolArray)
    {
        for (McpTool tool : toolArray)
        {
            registerTool(tool);
        }
    }

    public ObjectNode listTools()
    {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode toolsArray = result.putArray("tools");

        for (McpTool tool : tools.values())
        {
            ObjectNode toolNode = toolsArray.addObject();
            toolNode.put("name", tool.getName());
            toolNode.put("description", tool.getDescription());
            toolNode.set("inputSchema", tool.getSchema());
        }

        return result;
    }

    public Object callTool(String name, JsonNode args) throws Exception
    {
        McpTool tool = tools.get(name);
        if (tool == null)
        {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        return tool.execute(args);
    }
}
