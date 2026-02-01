package me.axeno.minemcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MineServer
{
    private final String token;
    private final McpProtocol protocol;
    private final Map<String, SseClient> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger;
    private Javalin app;

    public MineServer(String token)
    {
        this.token = token;
        this.protocol = new McpProtocol();
        this.logger = MineMCP.getInstance().getSLF4JLogger();
    }

    public void start(int port)
    {
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MineServer.class.getClassLoader());

        try
        {
            app = Javalin.create(config ->
            {
                config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
                config.showJavalinBanner = false;
            });

            setupRoutes();

            app.start(port);
            logger.info("MineMCP Server started on port {}", port);
        } catch (Exception e)
        {
            logger.error("Failed to start MineMCP Server", e);
        } finally
        {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    private void setupRoutes()
    {
        // Authentication Middleware
        app.before(this::handleAuth);

        // SSE Endpoint for Client Connection
        app.sse("/sse", this::handleSseConnection);

        // POST Endpoint for Client Messages
        app.post("/messages", this::handleMessage);

        // Standard HTTP Endpoints
        app.post("/api", this::handleDirectRequest);
        app.post("/mcp", this::handleDirectRequest);
        app.get("/mcp", ctx ->
        {
            ctx.contentType("application/json");
            ctx.result("{\"name\":\"MineMCP\",\"version\":\"1.0.0\",\"transport\":\"streamable-http\"}");
        });
    }

    private void handleAuth(Context ctx)
    {
        // Public endpoints
        if (ctx.path().equals("/mcp") && ctx.method().equals(io.javalin.http.HandlerType.GET))
        {
            return;
        }

        String authHeader = ctx.header("Authorization");
        String queryToken = ctx.queryParam("token");
        String sessionId = ctx.queryParam("sessionId");
        String validBearer = "Bearer " + token;

        boolean hasValidToken = (authHeader != null && authHeader.equals(validBearer)) ||
                (queryToken != null && queryToken.equals(token));

        boolean isSessionRequest = ctx.path().startsWith("/messages") &&
                sessionId != null &&
                sessions.containsKey(sessionId);

        if (!hasValidToken && !isSessionRequest)
        {
            ctx.status(401).result("Unauthorized");
            ctx.skipRemainingHandlers();
        }
    }

    private void handleSseConnection(SseClient client)
    {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, client);

        try
        {
            client.keepAlive();
            logger.info("New SSE client connected: {}", sessionId);

            String endpoint = "/messages?sessionId=" + sessionId;
            client.sendEvent("endpoint", endpoint);

            client.onClose(() ->
            {
                sessions.remove(sessionId);
                logger.info("SSE client disconnected: {}", sessionId);
            });
        } catch (Exception e)
        {
            logger.error("Error in SSE connection for session {}", sessionId, e);
            sessions.remove(sessionId);
        }
    }

    private void handleMessage(Context ctx)
    {
        String sessionId = ctx.queryParam("sessionId");

        if (sessionId == null || !sessions.containsKey(sessionId))
        {
            ctx.status(400).result("Invalid session");
            return;
        }

        String body = ctx.body();
        String response = protocol.handleRequest(body);
        SseClient client = sessions.get(sessionId);

        if (client != null && response != null)
        {
            client.sendEvent("message", response);
        }

        checkForInitialize(body, client);

        ctx.status(202).result("Accepted");
    }

    private void handleDirectRequest(Context ctx)
    {
        String response = protocol.handleRequest(ctx.body());

        if (response != null)
        {
            ctx.contentType("application/json");
            ctx.result(response);
        } else
        {
            ctx.status(202).result("Accepted");
        }
    }

    private void checkForInitialize(String body, SseClient client)
    {
        if (client == null) return;
        try
        {
            JsonNode request = mapper.readTree(body);
            if (request.has("method") && "initialize".equals(request.get("method").asText()))
            {
                client.sendEvent("message", "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/tools/list_changed\"}");
            }
        } catch (Exception ignored)
        {
        }
    }

    public void stop()
    {
        if (app != null) app.stop();
    }
}
