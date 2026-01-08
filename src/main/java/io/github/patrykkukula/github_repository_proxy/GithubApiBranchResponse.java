package io.github.patrykkukula.github_repository_proxy;

record GithubApiBranchResponse(String name, Commit commit) {
}

record Commit(String sha) {
}
