package uk.co.mycomputerworld.docAssist4U.services;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class for handling RAG (Retrieval-Augmented Generation) vector operations.
 * This class extends the base VectorService to provide additional functionalities
 * specific to RAG operations.
 */
@Service
public class RAGVectorService extends VectorService {

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private String port;

    @Value("${spring.ai.vectorstore.qdrant.collection-name}")
    private String collectionName;

    public RAGVectorService(VectorStore vectorStore, OllamaChatClient aiClient) {
        super(vectorStore, aiClient);

    }

//    public List<String> listPointIds() {
//        String url = String.format("%s/collections/%s/points", "https://" + host + ":" + port, collectionName);
//
//        // Send GET request to Qdrant
//        ResponseEntity<Map> responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, null, Map.class);
//        Map<String, Object> responseBody = responseEntity.getBody();
//
//        // Extract point IDs from the response
//        if (responseBody != null && responseBody.containsKey("points")) {
//            List<Map<String, Object>> points = (List<Map<String, Object>>) responseBody.get("points");
//            return points.stream()
//                    .map(point -> (String) point.get("id"))  // Assuming the point ID is in the "id" field
//                    .collect(Collectors.toList());
//        }
//        return Collections.emptyList();
//    }
}
