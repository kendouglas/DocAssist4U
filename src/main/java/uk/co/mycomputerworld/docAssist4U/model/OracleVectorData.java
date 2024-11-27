package uk.co.mycomputerworld.docAssist4U.model;

import oracle.sql.json.OracleJsonObject;

/**
 *
 */
public class OracleVectorData {
    private String id;
    private String text;

    private double[] embeddings;

    private OracleJsonObject metadata;

    /**
     * @param id
     * @param embeddings
     * @param text
     * @param metadata
     */
    public OracleVectorData(String id, double[] embeddings, String text, OracleJsonObject metadata) {
        this.id = id;
        this.embeddings = embeddings;
        this.metadata = metadata;
        this.text = text;
    }


    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OracleJsonObject getMetadata() {
        return metadata;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double[] getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(double[] embeddings) {
        this.embeddings = embeddings;
    }

    public void setMetadata(OracleJsonObject metadata) {
        this.metadata = metadata;
    }

}
