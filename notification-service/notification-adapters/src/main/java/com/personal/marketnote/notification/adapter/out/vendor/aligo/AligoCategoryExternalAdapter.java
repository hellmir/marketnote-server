package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoCategoryTreeResult;
import com.personal.marketnote.notification.port.out.aligo.GetAligoCategoryExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AligoCategoryExternalAdapter implements GetAligoCategoryExternalPort {

    private final AligoHttpClient aligoHttpClient;

    @Override
    public GetAligoCategoryTreeResult getCategoryTree() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        AligoCategoryApiResponse response = aligoHttpClient.post(
                "/akv10/category/", params, AligoCategoryApiResponse.class
        );

        List<GetAligoCategoryTreeResult.CategoryResult> categories = buildCategoryTree(response.data());
        return new GetAligoCategoryTreeResult(categories);
    }

    private List<GetAligoCategoryTreeResult.CategoryResult> buildCategoryTree(AligoCategoryApiResponse.CategoryData data) {
        if (data == null) {
            return List.of();
        }

        Map<String, List<GetAligoCategoryTreeResult.CategoryResult>> thirdByParent = groupByParentCode(
                data.thirdBusinessType()
        );

        Map<String, List<GetAligoCategoryTreeResult.CategoryResult>> secondByParent = new LinkedHashMap<>();
        if (data.secondBusinessType() != null) {
            for (AligoCategoryApiResponse.CategoryItem item : data.secondBusinessType()) {
                List<GetAligoCategoryTreeResult.CategoryResult> children = thirdByParent.getOrDefault(
                        item.code(), List.of()
                );
                secondByParent.computeIfAbsent(item.parentCode(), k -> new ArrayList<>())
                        .add(new GetAligoCategoryTreeResult.CategoryResult(item.code(), item.name(), children));
            }
        }

        List<GetAligoCategoryTreeResult.CategoryResult> result = new ArrayList<>();
        if (data.firstBusinessType() != null) {
            for (AligoCategoryApiResponse.CategoryItem item : data.firstBusinessType()) {
                List<GetAligoCategoryTreeResult.CategoryResult> children = secondByParent.getOrDefault(
                        item.code(), List.of()
                );
                result.add(new GetAligoCategoryTreeResult.CategoryResult(item.code(), item.name(), children));
            }
        }

        return result;
    }

    private Map<String, List<GetAligoCategoryTreeResult.CategoryResult>> groupByParentCode(
            List<AligoCategoryApiResponse.CategoryItem> items
    ) {
        Map<String, List<GetAligoCategoryTreeResult.CategoryResult>> grouped = new LinkedHashMap<>();
        if (items == null) {
            return grouped;
        }
        for (AligoCategoryApiResponse.CategoryItem item : items) {
            grouped.computeIfAbsent(item.parentCode(), k -> new ArrayList<>())
                    .add(new GetAligoCategoryTreeResult.CategoryResult(item.code(), item.name(), List.of()));
        }
        return grouped;
    }
}
