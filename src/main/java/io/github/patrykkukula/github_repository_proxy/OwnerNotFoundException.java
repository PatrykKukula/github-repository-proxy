package io.github.patrykkukula.github_repository_proxy;

class OwnerNotFoundException extends RuntimeException {
    OwnerNotFoundException(String ownerLogin) {
        super("Owner with login: %s not found".formatted(ownerLogin));
    }
}
