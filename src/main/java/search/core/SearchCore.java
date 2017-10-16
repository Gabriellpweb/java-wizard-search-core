package search.core;

import common.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SearchCore {

    private Directory index;

    private Analyzer analyzer;

    private IndexWriterConfig indexWriterConfig;

    private IndexWriter indexWriter;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;


    public SearchCore(Directory directory, IndexWriterConfig indexWriterConfig) {

        this.index = directory;
        this.analyzer = indexWriterConfig.getAnalyzer();
        this.indexWriterConfig = indexWriterConfig;

        try {

            this.indexWriter = new IndexWriter(index, indexWriterConfig);
            this.indexReader = DirectoryReader.open(index);
            this.indexSearcher = new IndexSearcher(indexReader);

        } catch (IOException e) {

            Logger.getInstance().notify(e);
        }
    }

    /**
     * Transforms a bean into lucene Document and write into the index.
     * @param object Object A bean with getter methods to work on reflection.
     */
    public void indexSimpleBean(Object object) {

        Document doc = new Document();

        Class c = object.getClass();

        for (Field f : c.getDeclaredFields()) {

            String fieldType = f.getType().getName();
            String fieldName = f.getName();

            String getMethod = "get"+fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);

            try {

                Method getter = c.getMethod(getMethod);

                switch (fieldType) {
                    case "java.lang.String":
                        doc.add(new StoredField(fieldName, (String)getter.invoke(object)));
                        break;
                    case "java.lang.Integer":
                        doc.add(new IntPoint(fieldName, (int)getter.invoke(object)));
                        break;
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        indexDocument(doc);
    }

    public void indexDocument(Document document) {

        try {

            indexWriter.addDocument(document);

        } catch (IOException e) {
            Logger.getInstance().notify(e);
        }
    }

    public void finalize() {
        try {
            indexWriter.forceMerge(1);
            indexWriter.close();
            index.close();
            indexReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
