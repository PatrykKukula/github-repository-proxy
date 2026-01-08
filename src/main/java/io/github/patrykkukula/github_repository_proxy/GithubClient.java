package io.github.patrykkukula.github_repository_proxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static io.github.patrykkukula.github_repository_proxy.GithubClientConstants.*;

/**
 * Client to communicate with GitHub REST API.
 * Official docs for API version used in this application available at https://docs.github.com/en/rest?apiVersion=2022-11-28
 */
@Component
@RequiredArgsConstructor
@Slf4j
class GithubClient {
    private final RestTemplate restTemplate;

    /**
     * base API url
     */
    @Value("${github.api.base-url}")
    private String apiBaseUrl;
    /**
     * path to retrieve repositories for given owner
     */
    @Value("${github.api.repos-path}")
    private String reposUrl;
    /**
     * path to retrieve branches for given owner and repository
     */
    @Value("${github.api.branches-path}")
    private String branchesUrl;

    /**
     * @param ownerLogin - login of GitHub repository owner
     * @return public repositories data array for given owner or empty array if no repositories exist
     * @throws OwnerNotFoundException if owner doesn't exist
     * @throws RateLimitExceededException if requests rate limit exceeded
     */
    GithubApiRepositoryResponse[] getOwnerRepositories(String ownerLogin) {
        String url = (apiBaseUrl + reposUrl).formatted(ownerLogin);
        logMethodInvocation(url);

        RequestEntity<Void> requestEntity = buildRequestEntity(url);

        try {
            GithubApiRepositoryResponse[] apiResponse = restTemplate.exchange(requestEntity, GithubApiRepositoryResponse[].class).getBody();
            return apiResponse != null ? apiResponse : new GithubApiRepositoryResponse[0];
        } catch (HttpClientErrorException ex) {
            logHttpClientErrorException(url, ex.getMessage());

            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new OwnerNotFoundException(ownerLogin);
            } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throwRateLimitExceededException(ex);
            }
            logUnexpectedError(ex.getMessage());
            throw ex;
        }
    }

    /**
     * @param ownerLogin     - login of GitHub repository owner
     * @param repositoryName - repository name
     * @return branches data array inside given repository or empty array if no branches exist
     * @throws RateLimitExceededException if requests rate limit exceeded
     */
    GithubApiBranchResponse[] getBranches(String ownerLogin, String repositoryName) {
        String url = (apiBaseUrl + branchesUrl).formatted(ownerLogin, repositoryName);
        logMethodInvocation(url);

        RequestEntity<Void> requestEntity = buildRequestEntity(url);

        try {
            GithubApiBranchResponse[] apiResponse = restTemplate.exchange(requestEntity, GithubApiBranchResponse[].class).getBody();
            return apiResponse != null ? apiResponse
                    : new GithubApiBranchResponse[0];
        } catch (HttpClientErrorException ex) {
            logHttpClientErrorException(url, ex.getMessage());

            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new GithubApiBranchResponse[0];
            }
            else if (ex.getStatusCode() == HttpStatus.FORBIDDEN || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS){
                throwRateLimitExceededException(ex);
            }
            logUnexpectedError(ex.getMessage());
            throw ex;
        }
    }

    private RequestEntity<Void> buildRequestEntity(String url){
        return RequestEntity.get(url)
                .header(GithubClientConstants.ACCEPT_HEADER, ACCEPT_HEADER_VALUE)
                .header(GITHUB_API_VERSION_HEADER, GITHUB_API_VERSION_HEADER_VALUE)
                .build();
    }

    private void throwRateLimitExceededException(HttpClientErrorException ex){
        HttpHeaders headers = ex.getResponseHeaders();
        String epochSeconds = headers != null ?
                headers.getFirst(RATE_LIMIT_RESET_HEADER) :
                null;

        throw new RateLimitExceededException("Requests rate limit exceeded and will be renewed at: "
                + formatRateLimitRenewalDate(epochSeconds));
    }

    private String formatRateLimitRenewalDate(String epochSeconds) {
        return epochSeconds != null ? Instant.ofEpochSecond(Long.parseLong(epochSeconds))
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) :
                "unknown";
    }

    private void logMethodInvocation(String url){
        log.info("Invoking request to GitHub API:{} ", url);
    }

    private void logUnexpectedError(String message){
        log.warn("Unexpected error occurred:{} ", message);
    }

    private void logHttpClientErrorException(String url, String message){
        log.warn("HttpClientErrorException occurred while invoking url: {}. Error message:{}",
                url, message);
    }
}
