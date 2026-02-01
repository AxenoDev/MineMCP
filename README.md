# MineMCP

[![License: MIT](https://img.shields.io/github/license/AxenoDev/MineMCP)](https://opensource.org/licenses/MIT)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-brightgreen.svg)](https://www.minecraft.net/)
[![Paper API](https://img.shields.io/badge/Paper-API-blue.svg)](https://papermc.io/)

A Minecraft Paper plugin that implements the [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server, enabling AI agents to control and monitor a Minecraft 1.21 server via HTTP.

## 🛠️ Available Tools

### World Manipulation
- `pose_block` - Place a block at specific coordinates
- `break_block` - Break a block at specific coordinates
- `fill_block` - Fill a region with blocks

### Command Execution
- `execute_command` - Execute any Minecraft command with output capture

### File System
- `read_file` - Read text files from the server
- `write_file` - Write text files to the server
- `read_file_base64` - Read binary files as Base64
- `write_file_base64` - Write binary files from Base64
- `list_directory` - List directory contents

### Server Information
- `list_plugins` - Get all installed plugins
- `get_logs` - Retrieve recent server logs
- `get_online_players` - List all online players
- `get_player` - Get detailed information about a specific player (health, inventory, location, effects, etc.)

## 📋 Requirements

- **Java 21** or higher
- **Minecraft 1.21** server
- **Paper** or **Purpur** server software (Spigot may work but not officially supported)

## 🚀 Installation

### Download Pre-built JAR

1. Download the latest release from the [Releases](../../releases) page
2. Place `MineMCP-1.0.0-all.jar` in your server's `plugins/` folder
3. Start/restart your server
4. Configure the token in `plugins/MineMCP/config.yml`

## ⚙️ Configuration

After the first run, edit `plugins/MineMCP/config.yml`:

```yaml
server:
  port: 3000
  token: "your-secret-token-here"  # Change this to a secure random token!
```

**Important**: Always change the default token for security!

### Generating a Secure Token

```bash
# Linux/macOS
openssl rand -hex 32

# Or use any password generator to create a strong token
```

## 🔌 Usage

### Starting the Server

1. Start your Minecraft server with MineMCP installed
2. The MCP server will automatically start on port 3000 (configurable)
3. Check the console for: `[MineMCP] MCP server started on port 3000`

### Connecting with an MCP Client

#### Using VS Code with MCP Extension

Create or edit `.vscode/mcp.json` in your project:

```json
{
  "mcpServers": {
    "minecraft": {
      "url": "http://localhost:3000/sse?token=your-secret-token-here"
    }
  }
}
```

## 📚 API Examples

### Execute a Command

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "execute_command",
    "arguments": {
      "command": "time set day"
    }
  }
}
```

### Place a Block

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "pose_block",
    "arguments": {
      "world": "world",
      "x": 100,
      "y": 64,
      "z": 200,
      "material": "DIAMOND_BLOCK"
    }
  }
}
```

### Get Player Information

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "get_player",
    "arguments": {
      "player": "PlayerName",
      ...
    }
  }
}
```

## 🔒 Security Considerations

1. **Token Protection**: Never commit your token to version control
2. **Network Security**: By default, the server binds to `0.0.0.0` (all interfaces)
3. **Firewall Rules**: Restrict port 3000 access to trusted IPs only
4. **File System Access**: Tools have access to the entire server directory - use with caution

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Adding a New Tool

1. Create a new class extending `McpTool` in `src/main/java/me/axeno/minemcp/tools/impl/`
2. Implement the required methods (`getSchema()` and `execute()`)
3. Register the tool in `ToolHandler.java` constructor

Example:

```java
public class MyTool extends McpTool {
    public MyTool() {
        super("my_tool", "Description of my tool");
    }

    @Override
    public ObjectNode getSchema() {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        // Define your schema...
        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        Bukkit.getScheduler().runTask(MineMCP.getInstance(), () -> {
            try {
                // Your Bukkit operations here
                future.complete("Success!");
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        String result = future.get(10, TimeUnit.SECONDS);
        return createTextResult(result);
    }
}
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Issues**: [GitHub Issues](../../issues)

---

Made with ❤️ for the Minecraft and AI communities
