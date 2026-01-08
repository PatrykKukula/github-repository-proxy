package io.github.patrykkukula.github_repository_proxy;

public class GithubClientConstants {
    private GithubClientConstants (){}

    static final String ACCEPT_HEADER = "Accept";
    /**
       value recommended by official GitHub Docs
     */
    static final String ACCEPT_HEADER_VALUE = "application/vnd.github+json";
    static final String GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version";
    /**
     GitHub API version
   */
    static final String GITHUB_API_VERSION_HEADER_VALUE = "2022-11-28";
    static final String RATE_LIMIT_RESET_HEADER = "x-ratelimit-reset";
}
