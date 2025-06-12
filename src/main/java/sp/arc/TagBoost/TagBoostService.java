package sp.arc.TagBoost;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TagBoostService {

    private final WebResourceRepository repository;
    private final LuceneIndexService luceneService;

    @Autowired
    public TagBoostService(WebResourceRepository repository, LuceneIndexService luceneService) {
        this.repository = repository;
        this.luceneService = luceneService;
    }

    public WebResource addResource(String url, String content) throws IOException {
        WebResource resource = new WebResource(url, content);
        WebResource saved = repository.save(resource);
        luceneService.indexResource(saved);
        return saved;
    }

    public WebResource addAnnotation(Long resourceId, String text, String author) {
        WebResource resource = repository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.addAnnotation(new Annotation(text, author));
        return repository.save(resource);
    }

    public List<WebResource> search(String keyword) throws IOException, ParseException {
        return luceneService.search(keyword);
    }

    public List<WebResource> searchResources(String keyword) throws IOException, ParseException {
        return luceneService.search(keyword); // Reuse search for consistency
    }

    public List<WebResource> getAllResources() {
        return repository.findAll();
    }
}