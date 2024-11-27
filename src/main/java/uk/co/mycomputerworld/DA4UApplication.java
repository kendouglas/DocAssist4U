package uk.co.mycomputerworld;

import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.co.mycomputerworld.docAssist4U.db.OracleDBVectorStore;

@SpringBootApplication
public class DA4UApplication {
    public static void main(String[] args) {

        SpringApplication.run(DA4UApplication.class, args);
    }

    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    /**
     * Creates and configures a VectorStore bean using the provided OllamaEmbeddingClient and JdbcTemplate.
     *
     * @param ec the OllamaEmbeddingClient used for embedding operations
     * @param t  the JdbcTemplate used for database interactions
     * @return a configured instance of OracleDBVectorStore
     */
    @Bean
    VectorStore vectorStore(OllamaEmbeddingClient ec, JdbcTemplate t) {
        return new OracleDBVectorStore(t, ec);
    }

}
