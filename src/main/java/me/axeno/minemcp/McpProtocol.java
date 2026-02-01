package me.axeno.minemcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.tools.ToolHandler;

public class McpProtocol
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final ToolHandler toolHandler;

    public McpProtocol()
    {
        this.toolHandler = new ToolHandler();
    }

    public String handleRequest(String jsonBody)
    {
        MineMCP.getInstance().getSLF4JLogger().info("Incoming MCP Request: {}", jsonBody);
        Object id = null;
        try
        {
            JsonNode request = mapper.readTree(jsonBody);
            if (!request.has("method"))
            {
                return createError(null, -32600, "Invalid Request: missing method");
            }

            String method = request.get("method").asText();
            JsonNode idNode = request.get("id");
            if (idNode != null && !idNode.isNull())
            {
                id = idNode.isNumber() ? idNode.asInt() : idNode.asText();
            }

            if (method.equals("initialize"))
            {
                // Check for protocol version if needed, but we'll be lenient
                ObjectNode result = mapper.createObjectNode()
                        .put("protocolVersion", "2024-11-05");

                ObjectNode capabilities = result.putObject("capabilities");
                ObjectNode toolsCap = capabilities.putObject("tools");
                toolsCap.put("listChanged", true);

                capabilities.putObject("resources");
                capabilities.putObject("prompts"); // Add prompts capability

                ObjectNode serverInfo = result.putObject("serverInfo");
                serverInfo.put("name", "MCPMinecraft");
                serverInfo.put("version", "1.2.3");

                return createResponse(id, result);
            }

            if (method.equals("notifications/initialized"))
            {
                // Handshake complete
                return null;
            }

            if (method.equals("tools/list"))
            {
                return createResponse(id, toolHandler.listTools());
            }

            if (method.equals("resources/list"))
            {
                ObjectNode result = mapper.createObjectNode();
                result.putArray("resources");
                return createResponse(id, result);
            }

            if (method.equals("prompts/list"))
            {
                ObjectNode result = mapper.createObjectNode();
                result.putArray("prompts");
                return createResponse(id, result);
            }

            if (method.equals("tools/call"))
            {
                JsonNode params = request.get("params");
                if (params == null) throw new IllegalArgumentException("Missing params");

                String toolName = params.get("name").asText();
                JsonNode args = params.get("arguments");
                if (args == null) args = mapper.createObjectNode();

                Object result = toolHandler.callTool(toolName, args);
                return createResponse(id, result);
            }

            if (method.equals("ping"))
            {
                return createResponse(id, mapper.createObjectNode());
            }

            // Notifications that don't expect response
            if (method.startsWith("notifications/"))
            {
                return null;
            }

            MineMCP.getInstance().getSLF4JLogger().error("Unknown method: {}", method);
            return createError(id, -32601, "Method not found: " + method);

        } catch (Exception e)
        {
            MineMCP.getInstance().getSLF4JLogger().error("Error handling request: {}", e.getMessage());
            e.printStackTrace();
            return createError(id, -32700, "Error: " + e.getMessage());
        }
    }

    private String createResponse(Object id, Object result)
    {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");

        if (id == null)
        {
            response.putNull("id");
        } else if (id instanceof Integer)
        {
            response.put("id", (Integer) id);
        } else
        {
            response.put("id", (String) id);
        }

        response.set("result", mapper.valueToTree(result));
        return response.toString();
    }

    private String createError(Object id, int code, String message)
    {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");

        if (id == null)
        {
            response.putNull("id");
        } else if (id instanceof Integer)
        {
            response.put("id", (Integer) id);
        } else
        {
            response.put("id", (String) id);
        }

        ObjectNode error = response.putObject("error");
        error.put("code", code);
        error.put("message", message);

        return response.toString();
    }
}
