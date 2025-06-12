package sp.arc.TagBoost;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TagBoostController {

    private final TagBoostService service;

    @Autowired
    public TagBoostController(TagBoostService service) {
        this.service = service;
    }

    @PostMapping("/resource")
    public ResponseEntity<WebResource> addResource(@RequestParam String url, @RequestParam String content) {
        try {
            WebResource saved = service.addResource(url, content);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/annotation")
    public ResponseEntity<WebResource> addAnnotation(@RequestParam Long resourceId,
                                                     @RequestParam String text,
                                                     @RequestParam String author) {
        try {
            WebResource updated = service.addAnnotation(resourceId, text, author);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<WebResource>> search(@RequestParam String keyword) {
        try {
            List<WebResource> results = service.searchResources(keyword);
            return ResponseEntity.ok(results);
        } catch (IOException | ParseException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/resources")
    public ResponseEntity<List<WebResource>> getAllResources() {
        List<WebResource> resources = service.getAllResources();
        return ResponseEntity.ok(resources);
    }
}