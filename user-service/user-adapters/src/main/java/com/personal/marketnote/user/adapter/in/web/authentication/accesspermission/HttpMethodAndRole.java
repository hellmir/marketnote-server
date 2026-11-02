package com.personal.marketnote.user.adapter.in.web.authentication.accesspermission;

import com.personal.marketnote.user.constant.AuthorizedHttpMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

@NoArgsConstructor
@Getter
@ToString
public class HttpMethodAndRole {
    private final Map<AuthorizedHttpMethod, Collection<String>> rolesPerHttpMethod = new HashMap<>();

    public void addRole(AuthorizedHttpMethod key, String role) throws NullPointerException {
        getRoleCollectionAndInitIfNotExist(key).add(role);
    }

    public void addAllRoles(AuthorizedHttpMethod key, Collection<String> roles) {
        getRoleCollectionAndInitIfNotExist(key).addAll(roles);
    }

    private Collection<String> getRoleCollectionAndInitIfNotExist(AuthorizedHttpMethod key) {
        if (!rolesPerHttpMethod.containsKey(key)) {
            this.rolesPerHttpMethod.put(key, new HashSet<>());
        }

        return rolesPerHttpMethod.get(key);
    }

    public boolean contains(AuthorizedHttpMethod key, String role) {
        return this.rolesPerHttpMethod.containsKey(key) && this.rolesPerHttpMethod.get(key).contains(role);
    }

    public void runTraversal(BiConsumer<AuthorizedHttpMethod, String> consumer) {
        this.rolesPerHttpMethod.entrySet()
                .stream()
                .flatMap((initEntry) ->
                        initEntry.getValue().stream().map((val) -> Map.entry(initEntry.getKey(), val)))
                .forEach((flatEntry) -> consumer.accept(flatEntry.getKey(), flatEntry.getValue()));
    }
}
