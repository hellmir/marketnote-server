package com.personal.marketnote.community.domain.review;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Reviews {
    private List<Review> reviews;

    public static Reviews from(List<Review> reviews) {
        return new Reviews(reviews);
    }

    public int size() {
        return reviews.size();
    }

    public List<Review> subList(int i, int pageSize) {
        return reviews.subList(0, pageSize);
    }
}
