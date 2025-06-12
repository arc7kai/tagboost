package sp.arc.TagBoost;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WebResourceRepository extends JpaRepository<WebResource, Long> {
    List<WebResource> findByContentContainingIgnoreCase(String keyword);
}