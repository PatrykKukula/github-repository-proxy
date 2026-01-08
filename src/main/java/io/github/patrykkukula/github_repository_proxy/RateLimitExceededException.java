package io.github.patrykkukula.github_repository_proxy;

class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String timeStamp) {
        super("Requests rate limit exceeded and will be renewed at: " + timeStamp);
    }
}
