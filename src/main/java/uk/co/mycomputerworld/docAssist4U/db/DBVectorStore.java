package uk.co.mycomputerworld.docAssist4U.db;

import oracle.jdbc.OracleType;
import oracle.sql.json.OracleJsonFactory;
import oracle.sql.json.OracleJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class DBVectorStore implements VectorStore, InitializingBean {
    protected  static final Logger logger = LoggerFactory.getLogger(DBVectorStore.class);
    protected JdbcTemplate jdbcTemplate;
    protected Map<String, String> DISTANCE_METRICS_FUNC;
    protected EmbeddingClient embeddingClient;

    protected int BATCH_SIZE = 100;

    @Value("${config.distance}")
    protected String distance_metric = "COSINE";

    @Value("${config.dropDatabaseVectorTableAtStart}")
    protected Boolean dropAtStartup = false;

    @Value("${config.vectorDB:vectortab}")
    protected String VECTOR_TABLE = "";

    protected List<String> DISTANCE_METRICS = List.of("COSINE");

    public DBVectorStore() {
    }

    @Override
    public void add(List<Document> documents) {

        int size = documents.size();

        this.jdbcTemplate.batchUpdate("INSERT INTO " + this.VECTOR_TABLE + " (text,embeddings,metadata) VALUES (?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {

                        var document = documents.get(i);
                        var text = document.getContent();

                        OracleJsonFactory factory = new OracleJsonFactory();
                        OracleJsonObject jsonObj = factory.createObject();
                        Map<String, Object> metaData = document.getMetadata();
                        for (Map.Entry<String, Object> entry : metaData.entrySet()) {
                            jsonObj.put(entry.getKey(), String.valueOf(entry.getValue()));
                        }

                        List<Double> vectorList = embeddingClient.embed(document);
                        double[] embeddings = new double[vectorList.size()];
                        for (int j = 0; j < vectorList.size(); j++) {
                            embeddings[j] = vectorList.get(j);
                        }

                        ps.setString(1, text);
                        ps.setObject(2, embeddings, OracleType.VECTOR);
                        ps.setObject(3, jsonObj, OracleType.JSON);

                    }

                    @Override
                    public int getBatchSize() {
                        return size;
                    }
                });

    }

    @Override
    // This should be done in the sub class
    public Optional<Boolean> delete(List<String> idList) {
        return Optional.empty();
    }

    @Override
    // This should be done in the sub class
    public List<Document> similaritySearch(SearchRequest request) {
        return List.of();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
