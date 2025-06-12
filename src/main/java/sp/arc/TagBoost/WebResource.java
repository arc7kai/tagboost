package sp.arc.TagBoost;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WebResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String content;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Annotation> annotations = new ArrayList<>();

    public WebResource() {}
    public WebResource(String url, String content) {
        this.url = url;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<Annotation> getAnnotations() { return annotations; }
    public void addAnnotation(Annotation annotation) { annotations.add(annotation); }
}