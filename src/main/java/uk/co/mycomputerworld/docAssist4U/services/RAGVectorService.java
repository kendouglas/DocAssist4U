package uk.co.mycomputerworld.docAssist4U.services;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

/**
 * Service class for handling RAG (Retrieval-Augmented Generation) vector operations.
 * This class extends the base VectorService to provide additional functionalities
 * specific to RAG operations.
 */
@Service
public class RAGVectorService extends VectorService {

    /**
     * Constructs a new RAGVectorService with the specified AI client.
     *
     * @param aiClient the AI client used for RAG operations
     */
    RAGVectorService(OllamaChatClient aiClient) {
        super(aiClient);
    }
}
