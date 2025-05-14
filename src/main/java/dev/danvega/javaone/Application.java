package dev.danvega.javaone;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final PresentationTools presentationTools = new PresentationTools();

    public static void main(String[] args) {

        // Stdio Server Transport (Support for SSE also available)
        var transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        // Sync tool specification

        // Create a server with custom configuration
        McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("javaone-mcp-server", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                // Register tools, resources, and prompts
                .tools(listPresentations(), searchPresentationsByTitle(), searchPresentationsByYear())
                .build();

        log.info("Starting JavaOne MCP Server...");
    }

    private static McpServerFeatures.SyncToolSpecification listPresentations() {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }
            """;
        // Tool implementation
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("get_presentations", "Get a list of all presentations from JavaOne", schema),
                (exchange, arguments) -> {
                    // Tool implementation
                    List<Presentation> presentations = presentationTools.getPresentations();
                    List<McpSchema.Content> contents = new ArrayList<>();
                    for (Presentation presentation : presentations) {
                        contents.add(new McpSchema.TextContent(presentation.toString()));
                    }
                    return new McpSchema.CallToolResult(contents, false);
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification searchPresentationsByTitle() {
        var schema = """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string"
                }
              },
              "required": ["query"]
            }
            """;
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("search_presentations_by_title", "Search presentations by title", schema),
                (exchange, arguments) -> {
                    String query = arguments.get("query").toString();
                    List<Presentation> presentations = presentationTools.searchPresentations(query);
                    List<McpSchema.Content> contents = new ArrayList<>();
                    for (Presentation presentation : presentations) {
                        contents.add(new McpSchema.TextContent(presentation.toString()));
                    }
                    return new McpSchema.CallToolResult(contents, false);
                }
        );

    }

    private static McpServerFeatures.SyncToolSpecification searchPresentationsByYear() {
        var schema = """
            {
              "type": "object",
              "properties": {
                "year": {
                  "type": "integer"
                }
              },
              "required": ["year"]
            }
            """;
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("search_presentations_by_year", "Search presentations by title", schema),
                (exchange, arguments) -> {
                    Integer year = Integer.valueOf(arguments.get("year").toString());
                    List<Presentation> presentations = presentationTools.getPresentationsByYear(year);
                    List<McpSchema.Content> contents = new ArrayList<>();
                    for (Presentation presentation : presentations) {
                        contents.add(new McpSchema.TextContent(presentation.toString()));
                    }
                    return new McpSchema.CallToolResult(contents, false);
                }
        );

    }

}
