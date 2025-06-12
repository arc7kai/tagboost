package sp.arc.TagBoost;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Annotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    private String author;
    private Date date;

    public Annotation() {}
    public Annotation(String text, String author) {
        this.text = text;
        this.author = author;
        this.date = new Date();
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}