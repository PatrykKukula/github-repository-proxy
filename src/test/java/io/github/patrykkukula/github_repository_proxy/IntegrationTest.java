package io.github.patrykkukula.github_repository_proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.github.patrykkukula.github_repository_proxy.GithubClientConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(
        @ConfigureWireMock(
                name = "github-api",
                baseUrlProperties = "github.api.base-url"
        )
)
@AutoConfigureTestRestTemplate
public class IntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @InjectWireMock("github-api")
    private WireMockServer wireMockServer;

    @Test
    @DisplayName("Should return repositories correctly when invoking getRepositories")
    void shouldReturnRepositoriesCorrectlyWhenInvokingGetRepositories() {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/users/testuser/repos"))
                        .withQueryParam("type", equalTo("owner"))
                        .withHeader(GithubClientConstants.ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(okJson("[{\"name\": \"repositoryName\",\"fork\": false,\"owner\": {\"login\": \"testuser\"}}," +
                                "{\"name\": \"repositoryName2\",\"fork\": false,\"owner\": {\"login\": \"testuser\"}}]")
                        )
        );
        wireMockServer.stubFor(
                get("/repos/testuser/repositoryName/branches")
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(okJson(" [{\"name\": \"branchName\",\"commit\": {\"sha\": \"shavalue\"}}]")
                        )
        );
        wireMockServer.stubFor(
                get("/repos/testuser/repositoryName2/branches")
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(okJson(" [{\"name\": \"branchName2\",\"commit\": {\"sha\": \"shavalue2\"}}]")
                        )
        );

        ResponseEntity<List<GithubRepository>> response =
                testRestTemplate.exchange(
                        "/api/repositories?ownerLogin=testuser",
                        GET,
                        null,
                        new ParameterizedTypeReference<List<GithubRepository>>() {}
                );

        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/users/testuser/repos"))
        );
        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/repos/testuser/repositoryName/branches"))
        );
        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/repos/testuser/repositoryName2/branches"))
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("repositoryName", response.getBody().getFirst().getName());
    }
    @Test
    @DisplayName("Should return empty list when invoking getRepositories and all repositories are forks")
    void shouldReturnEmptyListWhenInvokingGetRepositoriesAndAllRepositoriesAreForks() {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/users/testuser/repos"))
                        .withQueryParam("type", equalTo("owner"))
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(okJson("[{\"name\": \"repositoryName\",\"fork\": true,\"owner\": {\"login\": \"testuser\"}}," +
                                "{\"name\": \"repositoryName2\",\"fork\": true,\"owner\": {\"login\": \"testuser\"}}]")
                        )
        );

        ResponseEntity<List<GithubRepository>> response =
                testRestTemplate.exchange(
                        "/api/repositories?ownerLogin=testuser",
                        GET,
                        null,
                        new ParameterizedTypeReference<List<GithubRepository>>() {}
                );

        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/users/testuser/repos"))
        );
        wireMockServer.verify(0,
                getRequestedFor(urlPathEqualTo("/repos/testuser/repositoryName/branches"))
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }
    @Test
    @DisplayName("Should return repositories correctly when invoking getRepositories and some repositories are forks")
    void shouldReturnRepositoriesCorrectlyWhenInvokingGetRepositoriesAndSomeRepositoriesAreForks() {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/users/testuser/repos"))
                        .withQueryParam("type", equalTo("owner"))
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(okJson("[{\"name\": \"repositoryName\",\"fork\": true,\"owner\": {\"login\": \"testuser\"}}," +
                                "{\"name\": \"repositoryName2\",\"fork\": false,\"owner\": {\"login\": \"testuser\"}}]")
                        )
        );
        wireMockServer.stubFor(
                get("/repos/testuser/repositoryName2/branches")
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(okJson(" [{\"name\": \"branchName\",\"commit\": {\"sha\": \"shavalue\"}}]")
                        )
        );

        ResponseEntity<List<GithubRepository>> response =
                testRestTemplate.exchange(
                        "/api/repositories?ownerLogin=testuser",
                        GET,
                        null,
                        new ParameterizedTypeReference<List<GithubRepository>>() {}
                );

        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/users/testuser/repos"))
        );
        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/repos/testuser/repositoryName2/branches"))
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
    @Test
    @DisplayName("Should return 429 when invoking getRepositories and request rate limit exceeded")
    void shouldReturn429WhenInvokingGetRepositoriesAndRequestRateLimitExceeded() {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/users/user/repos"))
                        .withQueryParam("type", equalTo("owner"))
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(forbidden()
                                .withHeader(RATE_LIMIT_RESET_HEADER, "90000000")
                        )
        );

        ResponseEntity<ErrorResponse> response =
                testRestTemplate.exchange(
                        "/api/repositories?ownerLogin=user",
                        GET,
                        null,
                        ErrorResponse.class
                );

        wireMockServer.verify(
                getRequestedFor(urlPathEqualTo("/users/user/repos"))
        );
        wireMockServer.verify(0,
                getRequestedFor(urlPathEqualTo("/repos/user/repositoryName/branches"))
        );
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().message().contains("rate limit exceeded"));
    }
    @Test
    @DisplayName("Should return 404 when invoking getRepositories and owner not found")
    void shouldReturn404WhenInvokingGetRepositoriesAndOwnerNotFound() {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/users/notexistinguser/repos"))
                        .withQueryParam("type", equalTo("owner"))
                        .withHeader(ACCEPT_HEADER, equalTo(ACCEPT_HEADER_VALUE))
                        .withHeader(GITHUB_API_VERSION_HEADER, equalTo(GITHUB_API_VERSION_HEADER_VALUE))
                        .willReturn(notFound()
                        )
        );

        ResponseEntity<ErrorResponse> response =
                testRestTemplate.exchange(
                        "/api/repositories?ownerLogin=notexistinguser",
                        GET,
                        null,
                        ErrorResponse.class
                );

        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/users/notexistinguser/repos"))
        );
        wireMockServer.verify(0,
                getRequestedFor(urlPathEqualTo("/repos/notexistinguser/repositoryName/branches"))
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().message().contains("not found"));
    }
}
