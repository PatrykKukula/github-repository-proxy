package io.github.patrykkukula.github_repository_proxy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class GithubController {
    private final GithubService githubService;

    @GetMapping(value = "/repositories")
    ResponseEntity<List<GithubRepository>> getRepositories(@RequestParam(name = "ownerLogin") String ownerLogin) {
        return ResponseEntity.ok(githubService.getRepositories(ownerLogin));
    }
}
