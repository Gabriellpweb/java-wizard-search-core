package search.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;

import java.io.IOException;
import java.io.File;
import java.nio.file.Paths;


public class SearchCoreBuilder {

    private final String INDEXES_PATH = "src/main/resources/indexes/";
    /**
     * Types of analysers we can work.
     */
    public enum AnalyzerType {
        STANDARD,
        KEYWORD
    }

    /**
     * Types of directories we can work.
     */
    public enum IndexType {
        MEMORY,
        FILESYSTEM
    }

    private AnalyzerType analyzerType;

    private IndexType indexType;

    private Analyzer analyzer;

    private Directory index;

    private String coreName;

    private boolean isNewIndex = true;

    /**
     * Build the search core according to builder settings
     * @return
     */
    public SearchCore build() {

        createDirectory();
        createAnalyzer();

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

        if (isNewIndex) {
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        return new SearchCore(index , indexWriterConfig);
    }

    /**
     * Define the core analyzer type.
     * @param type AnalyzerType
     * @return SearchCoreBuilder
     */
    public SearchCoreBuilder setAnalyserType(AnalyzerType type) {
        this.analyzerType = type;
        return this;
    }

    /**
     * Define the core index type.
     * @param type
     * @return
     */
    public SearchCoreBuilder setIndexType(IndexType type) {
        this.indexType = type;
        return this;
    }

    /**
     * Define the index directory.
     * @param index
     * @return
     */
    public SearchCoreBuilder setIndex(Directory index) {
        this.index = index;
        return this;
    }

    /**
     * Define the analyzer.
     * @param analyzer
     * @return
     */
    public SearchCoreBuilder setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    /**
     * Define the core name if this uses file system index.
     * @param name
     * @return
     */
    public SearchCoreBuilder setCoreName(String name) {
        this.coreName = name;
        return this;
    }

    /**
     * Create the analyzer if this is not defined.
     * @return
     */
    private void createAnalyzer() {

        if (this.analyzer == null) {

            switch (analyzerType) {

                case KEYWORD:
                    this.analyzer = new KeywordAnalyzer();
                    break;
                case STANDARD:
                default:
                    this.analyzer = new StandardAnalyzer();
                    break;
            }
        }
    }

    /**
     * Create the index directory if it`s not defined.
     * @return
     */
    private void createDirectory() {

        if (this.index == null) {

            switch (indexType) {
                case MEMORY:
                    this.index = new RAMDirectory();
                    break;
                case FILESYSTEM:
                    createFSDirectory();
                    break;
            }
        }
    }

    /**
     * Create file system directory for core name.
     */
    private void createFSDirectory() {

        if (coreName == null) {
            throw new NullPointerException("For filesystem indexes the core name must be defined.");
        }

        // Path of core indexes.
        String indexPath = INDEXES_PATH + coreName;

        // Validate if must create new files.
        File index = new File(indexPath);

        if (index.isDirectory() && index.exists()) {
            if (index.list().length > 0) {
                isNewIndex = false;
            }
        }

        try {
            this.index = FSDirectory.open(Paths.get(indexPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
