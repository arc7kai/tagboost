package sp.arc.TagBoost;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class WebController {
    private final TagBoostService service;

    @Autowired
    public WebController(TagBoostService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<WebResource> resources = service.getAllResources();
        model.addAttribute("resources", resources);
        model.addAttribute("keyword", "");
        return "index";
    }

    @PostMapping("/resource")
    public String addResource(@RequestParam String url, @RequestParam String content, Model model) {
        try {
            service.addResource(url, content);
        } catch (IOException e) {
            model.addAttribute("error", "Failed to add resource: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/annotation")
    public String addAnnotation(@RequestParam Long resourceId, @RequestParam String text, @RequestParam String author, Model model) {
        try {
            service.addAnnotation(resourceId, text, author);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add annotation: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        try {
            List<WebResource> results = service.search(keyword);
            model.addAttribute("resources", results);
            model.addAttribute("keyword", keyword);
        } catch (IOException | ParseException e) {
            model.addAttribute("error", "Search failed: " + e.getMessage());
        }
        return "index";
    }
}