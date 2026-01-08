package io.github.patrykkukula.github_repository_proxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class GithubService {
    private final GithubClient client;

    List<GithubRepository> getRepositories(String ownerLogin) {
        GithubApiRepositoryResponse[] apiResponseArray = client.getOwnerRepositories(ownerLogin);
        log.info("Extracted repositories from GitHub API response successfully for owner: {}",
                ownerLogin);

        List<GithubRepository> repositories = new ArrayList<>();

        for (GithubApiRepositoryResponse response : apiResponseArray) {
            GithubRepository repository = new GithubRepository();

            if (response.fork()) continue;

            repository.setName(response.name());
            repository.setOwnerLogin(response.owner().login());

            setGithubRepositoryBranches(repository);

            repositories.add(repository);
        }
        return repositories;
    }

    private void setGithubRepositoryBranches(GithubRepository githubRepository) {
        GithubApiBranchResponse[] apiResponseArray = client.getBranches(githubRepository.getOwnerLogin(), githubRepository.getName());
        log.info("Extracted branches array from GitHub API response successfully for repository: {}",
                githubRepository.getName());

        List<GithubRepository.Branch> branches = new ArrayList<>();

        for (GithubApiBranchResponse response : apiResponseArray) {
            branches.add(new GithubRepository.Branch(response.name(), response.commit().sha()));
        }

        githubRepository.setBranches(branches);
    }
}
