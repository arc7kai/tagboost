package sp.arc.TagBoost;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class TagBoostApplication {
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(TagBoostApplication.class, args);

		// Get the service
		TagBoostService service = context.getBean(TagBoostService.class);

		try {
			// Check if resource already exists
			List<WebResource> existing = service.getAllResources();
			boolean resourceExists = existing.stream()
					.anyMatch(r -> r.getUrl().equals("https://ProgrammerArc.com"));
			WebResource resource;

			if (!resourceExists) {
				// Add a sample web resource
				resource = service.addResource("https://ProgrammerArc.com", "Java is a great programming language");
				System.out.println("Added Resource: " + resource.getUrl() + " with content: " + resource.getContent());

				// Add an annotation
				service.addAnnotation(resource.getId(), "Useful site!", "Shash");
				System.out.println("Added annotation to resource ID: " + resource.getId());
			} else {
				resource = existing.stream()
						.filter(r -> r.getUrl().equals("https://ProgrammerArc.com"))
						.findFirst()
						.orElse(null);
				System.out.println("Resource already exists: " + resource.getUrl());
			}

			// Search for resources
			List<WebResource> results = service.search("java");
			System.out.println("Search results for 'java':");
			for (WebResource r : results) {
				System.out.println("URL: " + r.getUrl() + ", Content: " + r.getContent() + ", Annotations: " + r.getAnnotations());
			}
		} catch (IOException | ParseException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}