package me.axeno.minemcp.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.axeno.minemcp.MineMCP;
import me.axeno.minemcp.tools.McpTool;
import me.axeno.minemcp.tools.McpCommandSender;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ExecuteCommandTool extends McpTool
{

    public ExecuteCommandTool()
    {
        super("execute_command", "Execute a Minecraft command");
    }

    @Override
    public ObjectNode getSchema()
    {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("required", mapper.createArrayNode().add("command"));
        ObjectNode props = mapper.createObjectNode();
        props.set("command", mapper.createObjectNode().put("type", "string"));
        schema.set("properties", props);

        return schema;
    }

    @Override
    public ObjectNode execute(JsonNode args) throws Exception
    {
        String command = args.get("command").asText();
        CompletableFuture<String> future = new CompletableFuture<>();

        // Run the command on the Bukkit main thread and capture output via McpCommandSender.
        Bukkit.getScheduler().runTask(MineMCP.getInstance(), () ->
        {
            try
            {
                McpCommandSender sender = new McpCommandSender(Bukkit.getConsoleSender());
                Bukkit.dispatchCommand(sender, command);
                String captured = sender.getOutput();

                // If no output captured, attempt a log-capture fallback (some commands write to logger or require console identity).
                if (captured == null || captured.isEmpty())
                {
                    StringBuilder logCapture = new StringBuilder();
                    Logger bukkitLogger = Bukkit.getLogger();
                    Handler handler = new Handler()
                    {
                        @Override
                        public void publish(LogRecord record)
                        {
                            if (record == null) return;
                            String msg = record.getMessage();
                            if (msg != null && !msg.isEmpty())
                            {
                                logCapture.append(msg).append('\n');
                            }
                        }

                        @Override
                        public void flush()
                        {
                        }

                        @Override
                        public void close() throws SecurityException
                        {
                        }
                    };

                    try
                    {
                        bukkitLogger.addHandler(handler);
                        // Dispatch as actual console to increase chance the server prints into logger
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                    catch (Throwable t)
                    {
                        // ignore; we'll return original empty message below
                    }
                    finally
                    {
                        bukkitLogger.removeHandler(handler);
                    }

                    String logs = logCapture.toString();
                    if (logs != null && !logs.isEmpty())
                    {
                        captured = "(captured from server logs)\n" + logs;
                    }
                    else
                    {
                        captured = "Command executed but produced no output.";
                    }
                }

                future.complete(captured);
            }
            catch (Throwable t)
            {
                future.completeExceptionally(t);
            }
        });

        try
        {
            String result = future.get(10, TimeUnit.SECONDS);
            return createTextResult(result);
        }
        catch (TimeoutException e)
        {
            return createTextResult("Command sent but response timed out. The command may still have executed.");
        }
        catch (Exception e)
        {
            return createTextResult("Error executing command: " + e.getMessage());
        }
    }
}
