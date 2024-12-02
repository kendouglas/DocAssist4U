package uk.co.mycomputerworld.docAssist4U.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for Qdrant vector database client setup.
 * This configuration is only active when the "manual" profile is selected.
 */
@Configuration
@Profile("manual")
public class QdrantConfig {

    /**
     * Host address for the Qdrant server, defaults to localhost
     */
    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String qdrantHost;

    /**
     * Port number for the Qdrant server connection
     */
    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int qdrantPort;

    /**
     * API key for authenticating with the Qdrant server
     */
    @Value("${spring.ai.vectorstore.qdrant.api-key:}")
    private String apiKey;

    /**
     * Name of the collection in Qdrant to be used
     */
    @Value("${spring.ai.vectorstore.qdrant.collection-name}")
    private String collectionName;

    /**
     * Name of the embedding model to be used
     */
    @Value("${spring.ai.embedding.model-name}")
    private String embeddingModelName;

    /**
     * Creates and configures a QdrantClient bean.
     *
     * @return Configured QdrantClient instance
     */
    @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(getQdrantHost(), getQdrantPort(), true);
        builder.withApiKey(getQdrantAPIKey());
        QdrantGrpcClient qdrantGrpcClient = builder.build();
        return new QdrantClient(qdrantGrpcClient);
    }

    /**
     * Retrieves the configured Qdrant server port.
     *
     * @return port number for Qdrant server
     */
    private int getQdrantPort() {
        return 0;
    }

    /**
     * Retrieves the configured Qdrant server host.
     *
     * @return host address for Qdrant server
     */
    private String getQdrantHost() {
        return null;
    }

    /**
     * Retrieves the configured Qdrant API key.
     *
     * @return API key for Qdrant authentication
     */
    private String getQdrantAPIKey() {
        return apiKey;
    }
}