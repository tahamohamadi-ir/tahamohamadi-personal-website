package ir.tahamohamadi.blog.post.api.admin;

public class PublishValidationException extends RuntimeException {
    public PublishValidationException() { super("The post does not meet publishing requirements"); }
}
