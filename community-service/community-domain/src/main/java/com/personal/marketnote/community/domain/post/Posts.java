package com.personal.marketnote.community.domain.post;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Posts {
    private List<Post> posts;

    public static Posts from(List<Post> posts) {
        return new Posts(posts);
    }

    public int size() {
        return posts.size();
    }

    public List<Post> subList(int start, int end) {
        return posts.subList(start, end);
    }
}
