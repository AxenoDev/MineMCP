# MineMCP Copilot Instructions

MineMCP is a Minecraft Paper plugin that implements the Model Context Protocol (MCP) server to enable AI agents to control and monitor a Minecraft 1.21 server via HTTP.

## Architecture Overview

**Three-layer design:**
1. **Plugin Layer** ([MineMCP.java](src/main/java/me/axeno/minemcp/MineMCP.java)): Paper plugin entry point. Initializes server and loads config.
2. **HTTP/MCP Layer** ([MineServer.java](src/main/java/me/axeno/minemcp/MineServer.java)): Javalin HTTP server (port 3000) with SSE support for bidirectional MCP communication.
3. **Protocol Layer** ([McpProtocol.java](src/main/java/me/axeno/minemcp/McpProtocol.java), [ToolHandler.java](src/main/java/me/axeno/minemcp/tools/ToolHandler.java)): Implements MCP 2024-11-05 spec. Routes requests to handlers.

**Data Flow**: AI Client → Javalin HTTP Server → MCP Protocol Handler → Tool Execution → Bukkit Scheduler → Minecraft Server

## Key Components

- **MineServer**: HTTP server with SSE `/sse` endpoint for client connection + `/messages` for bidirectional MCP. Token-based auth via header/query param.
- **McpProtocol**: Implements MCP initialize/tools/prompts lifecycle. Routes `tools/call` to ToolHandler.
- **ToolHandler**: Exposes 8 tools: `execute_command`, `read_file`, `write_file`, `list_directory`, `list_plugins`, `get_logs`, `read_file_base64`, `write_file_base64`.
- **McpCommandSender**: Custom CommandSender wrapper that captures command output. Falls back to log capture for commands that reject custom senders (e.g., LuckPerms).

## Build & Run

```bash
./gradlew build          # Builds jar with shadowJar (includes deps)
./gradlew runServer      # Builds + runs embedded Paper 1.21 server on port 25565
```
Jar outputs to `build/libs/MineMCP-1.0.0.jar`. Config at `build/resources/main/config.yml`. MCP server starts on port 3000 during plugin onEnable.

## Project Structure

```
src/main/java/me/axeno/minemcp/
  MineMCP.java           # Plugin entry point
  MineServer.java        # HTTP server (Javalin)
  McpProtocol.java       # MCP request router
  tools/
    ToolHandler.java     # Tool implementations + schemas
    McpCommandSender.java # Command output capture
  disabled/              # Legacy Gemini AI chat features (not active)
```

## Critical Conventions

1. **Thread Safety**: Command execution always runs on Bukkit main thread via `Bukkit.getScheduler().runTask()`. Async operations use `CompletableFuture` with 10s timeout.
2. **Token Auth**: Check [MineServer.java](src/main/java/me/axeno/minemcp/MineServer.java#L35) for auth logic—supports header `Authorization: Bearer {token}` or query param `token`. SSE sessions auto-authenticated.
3. **JSON Responses**: All MCP responses use Jackson ObjectMapper. Tool results must be wrapped in `{"content": [{"type": "text", "text": "..."}]}` format.
4. **Classpath Isolation**: MineServer swaps classloader before Javalin startup to avoid Paper conflicts ([MineServer.java](src/main/java/me/axeno/minemcp/MineServer.java#L28)).
5. **Config**: Loaded from `server.token` in config.yml. Use `MineMCP.getInstance().getConfig()` to access.

## Common Tasks

- **Add new tool**: Update [ToolHandler.listTools()](src/main/java/me/axeno/minemcp/tools/ToolHandler.java#L36) with schema, add case in callTool(), implement method.
- **Debug MCP**: Server logs requests to SLF4J. Check `/run/logs/latest.log` in test server.
- **Run tests**: Use `./gradlew test` (create tests in `src/test/`).
- **Modify HTTP routes**: Edit app routes in [MineServer.start()](src/main/java/me/axeno/minemcp/MineServer.java#L54).

## Integration Points

- **Bukkit API**: Commands via `Bukkit.dispatchCommand()`, tasks via scheduler, logging via `MineMCP.getInstance().getSLF4JLogger()`.
- **Google Genai**: Disabled chat feature (see `disabled/api/GeminiService.java` for integration pattern if re-enabling).
- **MCP Client**: Connects via `http://localhost:3000/sse?token=...`. Test with `.vscode/mcp.json` settings.
