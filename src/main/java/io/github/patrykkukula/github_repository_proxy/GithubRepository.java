package io.github.patrykkukula.github_repository_proxy;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class GithubRepository {
    @JsonProperty(index = 0)
    private String name;
    @JsonProperty(index = 1)
    private String ownerLogin;
    @JsonProperty(index = 2)
    private List<Branch> branches;

    static record Branch(String name, String lastCommitSha){}
}
