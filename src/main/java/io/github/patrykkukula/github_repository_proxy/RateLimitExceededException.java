package io.github.patrykkukula.github_repository_proxy;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String timeStamp) {
        super("Requests rate limit exceeded and will be renewed at: " + timeStamp);
    }
}
