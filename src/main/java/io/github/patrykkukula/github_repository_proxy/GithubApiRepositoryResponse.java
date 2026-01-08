package io.github.patrykkukula.github_repository_proxy;

public record GithubApiRepositoryResponse(String name, boolean fork, Owner owner) {
}

record Owner(String login) {
}
