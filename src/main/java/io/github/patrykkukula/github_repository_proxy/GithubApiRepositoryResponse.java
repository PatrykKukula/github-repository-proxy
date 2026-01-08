package io.github.patrykkukula.github_repository_proxy;

record GithubApiRepositoryResponse(String name, boolean fork, Owner owner) {
}

record Owner(String login) {
}
