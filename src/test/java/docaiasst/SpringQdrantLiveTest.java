package docaiasst;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.mycomputerworld.docAssist4U.config.QdrantConfig;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = QdrantConfig.class)
class SpringQdrantAutoConfigLiveTest {

    @Autowired
    private VectorStore qdrantVectorStore;

    @Test
    void givenQdrantDB_whenPropertiesDefined_thenAutoConfigureVectorStore() {
        assertNotNull(qdrantVectorStore);
        assertInstanceOf(QdrantVectorStore.class, qdrantVectorStore);
    }

}
