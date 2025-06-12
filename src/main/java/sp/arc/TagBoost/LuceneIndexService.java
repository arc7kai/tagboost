package sp.arc.TagBoost;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class LuceneIndexService {
    private static final Logger LOGGER = Logger.getLogger(LuceneIndexService.class.getName());
    private Directory indexDirectory;
    private StandardAnalyzer analyzer;
    private IndexWriter indexWriter;
    private final WebResourceRepository repository;

    @Autowired
    public LuceneIndexService(WebResourceRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        try {
            Path indexPath = Paths.get("lucene_index");
            // Clean up stale lock file
            Path lockPath = indexPath.resolve("write.lock");
            if (Files.exists(lockPath)) {
                Files.delete(lockPath);
                LOGGER.info("Deleted stale lock file: " + lockPath);
            }
            this.indexDirectory = FSDirectory.open(indexPath);
            this.analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            this.indexWriter = new IndexWriter(indexDirectory, config);
            LOGGER.info("Lucene index initialized at: lucene_index");

            List<WebResource> resources = repository.findAll();
            for (WebResource resource : resources) {
                indexResource(resource);
            }
            LOGGER.info("Indexed " + resources.size() + " resources on startup");
        } catch (IOException e) {
            LOGGER.severe("Failed to initialize Lucene index: " + e.getMessage());
            throw new RuntimeException("Lucene initialization failed", e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (indexWriter != null) {
                indexWriter.close();
                LOGGER.info("Lucene index writer closed");
            }
            if (analyzer != null) {
                analyzer.close();
                LOGGER.info("Lucene analyzer closed");
            }
            if (indexDirectory != null) {
                indexDirectory.close();
                LOGGER.info("Lucene index directory closed");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to close Lucene resources: " + e.getMessage());
        }
    }

    public void indexResource(WebResource resource) throws IOException {
        if (indexWriter == null) {
            throw new IOException("IndexWriter not initialized");
        }
        Document doc = new Document();
        doc.add(new TextField("id", String.valueOf(resource.getId()), Field.Store.YES));
        doc.add(new TextField("url", resource.getUrl(), Field.Store.YES));
        doc.add(new TextField("content", resource.getContent(), Field.Store.YES));
        StringBuilder annotationsText = new StringBuilder();
        for (Annotation annotation : resource.getAnnotations()) {
            annotationsText.append(annotation.getText()).append(" ");
        }
        doc.add(new TextField("annotations", annotationsText.toString(), Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.commit();
        LOGGER.info("Indexed resource ID: " + resource.getId());
    }

    public List<WebResource> search(String keyword) throws IOException, ParseException {
        if (indexDirectory == null) {
            throw new IOException("IndexDirectory not initialized");
        }
        List<WebResource> resources = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        try (IndexReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);
            Query contentQuery = parser.parse(keyword);
            QueryParser annotationParser = new QueryParser("annotations", analyzer);
            Query annotationQuery = annotationParser.parse(keyword);
            Query query = new org.apache.lucene.search.BooleanQuery.Builder()
                    .add(contentQuery, org.apache.lucene.search.BooleanClause.Occur.SHOULD)
                    .add(annotationQuery, org.apache.lucene.search.BooleanClause.Occur.SHOULD)
                    .build();
            TopDocs results = searcher.search(query, 10);
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Long id = Long.parseLong(doc.get("id"));
                if (seenIds.add(id)) {
                    repository.findById(id).ifPresent(resources::add);
                }
            }
            LOGGER.info("Search for '" + keyword + "' returned " + resources.size() + " unique results");
        }
        return resources;
    }
}