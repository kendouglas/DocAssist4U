package uk.co.mycomputerworld.docAssist4U.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.co.mycomputerworld.docAssist4U.model.MessageDTO;
import uk.co.mycomputerworld.docAssist4U.services.RAGVectorService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.co.mycomputerworld.docAssist4U.utils.CodeFormatter.replaceTags;

@RestController
@RequestMapping("da")
public class DA4UController {
    private static final Logger logger = LoggerFactory.getLogger(DA4UController.class);
    private final RAGVectorService ragVectorService;
    private final OllamaChatClient chatClient;
    private final OllamaEmbeddingClient embeddingClient;

    @Value("${config.tempDir}")
    private String TEMP_DIR;

    /**
     * Constructor for DA4UController.
     *
     * @param embeddingClient  the client used for embedding operations
     * @param chatClient       the client used for chat operations
     * @param ragVectorService the service used for [RAG](https://en.wikipedia.org/wiki/Retrieval-augmented_generation) vector operations
     */
    @Autowired
    public DA4UController(OllamaEmbeddingClient embeddingClient, OllamaChatClient chatClient, RAGVectorService ragVectorService) {
        this.embeddingClient = embeddingClient;
        this.ragVectorService = ragVectorService;
        this.chatClient = chatClient;
        logger.info("Controller started.");
    }


    /**
     * Endpoint to perform RAG (Retrieval-Augmented Generation) operation.
     *
     * @param message the message containing the query in natural language
     * @return a map containing the generated response
     */
    @PostMapping("/rag")
    public Map<String, Object> rag(@RequestBody MessageDTO message) {
        logger.info("rag2 called {}", message.getMessage());

        // Get the original generation value
        String originalValue = this.ragVectorService.rag(message.getMessage());

        // Replace \n with <BR> in the original value
        String modifiedValue = originalValue.replace("\n", "<BR>");
        modifiedValue = replaceTags(modifiedValue, "<B>", "</B>");

        // Create a mutable HashMap and put the modified value
        HashMap<String, Object> ragMap = new HashMap<>();
        ragMap.put("generation", modifiedValue);

        return ragMap;
    }

    /**
     * Endpoint to perform embedding operation.
     *
     * @param message the message containing the text to be embedded
     * @return a map containing the embedding response
     */
    @PostMapping("/embedding")
    public Map<String, Object> embed(@RequestBody MessageDTO message) {
        logger.info("embedding called {}", message.toString());
        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message.getMessage()));
        return Map.of("embedding", embeddingResponse);
    }

    /**
     * Endpoint to store a PDF file in Oracle Vector DB 23 AI.
     *
     * @param file the PDF file sent as Multipart
     * @return a success or failure message
     */
    @PostMapping("/store")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("store called {}", file.getOriginalFilename());

        // Check if the file is empty and return a failure message if true
        if (file.isEmpty()) {
            return "Failed to upload empty file.";
        }

        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");
            Path parentDir = Path.of(currentDir);

            // Resolve the temporary directory path
            Path tempPath = parentDir.resolve(TEMP_DIR);

            // Create the temporary directory if it does not exist
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }

            // Create a unique temporary directory for uploads
            Path tempDir = Files.createTempDirectory(tempPath, "uploads_");

            // Resolve the file path within the temporary directory
            Path filePath = tempDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));

            // Copy the file input stream to the resolved file path, replacing if it exists
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Store the document using the ragVectorService
            this.ragVectorService.putDocument(new FileSystemResource(filePath.toString()));

            // Return a success message with the file path
            return "File stored successfully " + filePath;
        } catch (IOException e) {
            // Log the exception message and return a failure message
            logger.info(e.getMessage());
            return "Failed to upload file: " + e.getMessage();
        }
    }


}
