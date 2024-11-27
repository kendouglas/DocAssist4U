package uk.co.mycomputerworld.docAssist4U.db;

import oracle.jdbc.OracleType;
import oracle.sql.json.OracleJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.co.mycomputerworld.docAssist4U.model.OracleVectorData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class OracleDBVectorStore extends DBVectorStore {
    private static final Logger logger = LoggerFactory.getLogger(OracleDBVectorStore.class);

    public OracleDBVectorStore(JdbcTemplate _jdbcTemplate, EmbeddingClient embClient) {
        jdbcTemplate = _jdbcTemplate;

        DISTANCE_METRICS_FUNC = new HashMap<>();

        /* Cosine Distance (Calculates Similarity) measures the cosine of the angle between two vectors.
           It focuses on the orientation of the vectors rather than their magnitude.
           By measuring the cosine of the angle between two vectors, it determines how similar they are in terms of
           direction. If the vectors point in the same direction, the cosine distance is small (close to 0), indicating
           high similarity.

           If they point in opposite directions, the cosine distance is large (close to 1), indicating low similarity.

           Cosine Distance is widely used in text analysis, information retrieval, and machine learning to compare the
           similarity of documents, images, or other high-dimensional data points.

        */
        this.DISTANCE_METRICS_FUNC.put("COSINE", "COSINE_DISTANCE"); // Use COSINE similarity searching.
        this.DISTANCE_METRICS_FUNC.put("MANHATTAN", "L1_DISTANCE");
        this.DISTANCE_METRICS_FUNC.put("EUCLIDEAN", "L2_DISTANCE");
        this.DISTANCE_METRICS_FUNC.put("DOT", "INNER_PRODUCT");

        this.embeddingClient = embClient;

    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {

        List<OracleVectorData> nearest = new ArrayList<>();

        logger.info("MSG: REQUESTED QUERY {}", request.getQuery());
        List<Double> queryEmbeddings = embeddingClient.embed(request.getQuery());
        logger.info("MSG: EMBEDDINGS SIZE: {}", queryEmbeddings.size());

        logger.info("MSG: DISTANCE METRICS: {}", distance_metric);
        if (DISTANCE_METRICS_FUNC.get(distance_metric) == null) {
            logger.error("ERROR: wrong distance metrics set. Allowed values are: {}", String.join(",", DISTANCE_METRICS));
            System.exit(1);
        }
        logger.info("MSG: DISTANCE METRICS FUNCTION: {}", DISTANCE_METRICS_FUNC.get(distance_metric));
        int topK = request.getTopK();

        try {
            nearest = similaritySearchByMetrics(VECTOR_TABLE, queryEmbeddings, topK,
                    this.DISTANCE_METRICS_FUNC.get(distance_metric));
        } catch (Exception e) {
            logger.error(e.toString());
        }

        List<Document> documents = new ArrayList<>();

        for (OracleVectorData d : nearest) {
            OracleJsonObject metadata = d.getMetadata();
            Map<String, Object> map = new HashMap<>();
            for (String key : metadata.keySet()) {
                map.put(key, metadata.get(key).toString());
            }
            Document doc = new Document(d.getText(), map);
            documents.add(doc);

        }
        return documents;

    }

    List<OracleVectorData> similaritySearchByMetrics(String vectortab, List<Double> vector, int topK,
                                               String distance_metrics_func) {
        List<OracleVectorData> results = new ArrayList<>();
        double[] doubleVector = new double[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            doubleVector[i] = vector.get(i);
        }

        try {

            String similaritySql = "SELECT id,embeddings,metadata,text FROM " + vectortab
                                   + " ORDER BY " + distance_metrics_func + "(embeddings, ?)"
                                   + " FETCH FIRST ? ROWS ONLY";

            results = jdbcTemplate.query(similaritySql,
                    ps -> {
                        ps.setObject(1, doubleVector, OracleType.VECTOR);
                        ps.setObject(2, topK, OracleType.NUMBER);
                    },
                    (rs, rowNum) -> new OracleVectorData(rs.getString("id"),
                            rs.getObject("embeddings", double[].class),
                            rs.getObject("text", String.class),
                            rs.getObject("metadata", OracleJsonObject.class)));

        } catch (Exception e) {
            logger.error("ERROR: " + e.getMessage());
        }

        return results;
    }

}
