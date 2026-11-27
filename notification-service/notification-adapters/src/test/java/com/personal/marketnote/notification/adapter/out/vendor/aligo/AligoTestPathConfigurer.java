package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.configuration.AligoProperties;

import java.util.Set;

/**
 * 테스트용 AligoProperties.Api path 설정 헬퍼.
 *
 * <p>운영의 application.yml과 동일한 17개 path 값을 일괄 적용하여
 * 어댑터 단위 테스트에서 path 분기 로직이 정상 동작하도록 한다.</p>
 */
final class AligoTestPathConfigurer {

    private AligoTestPathConfigurer() {
    }

    static void configure(AligoProperties properties) {
        AligoProperties.Api api = properties.getApi();
        api.setAlimtalkSendPath("/akv10/alimtalk/send/");
        api.setBrandTalkTemplateCreatePath("/brandtalk/template/create/");
        api.setBrandTalkTemplateListPath("/brandtalk/template/list/");
        api.setProfileAuthPath("/akv10/profile/auth/");
        api.setProfileAddPath("/akv10/profile/add/");
        api.setProfileListPath("/akv10/profile/list/");
        api.setHeartInfoPath("/akv10/heartinfo/");
        api.setCancelPath("/akv10/cancel/");
        api.setHistoryListPath("/akv10/history/list/");
        api.setTemplateAddPath("/akv10/template/add/");
        api.setTemplateModifyPath("/akv10/template/modify/");
        api.setTemplateListPath("/akv10/template/list/");
        api.setTemplateRequestPath("/akv10/template/request/");
        api.setTemplateDelPath("/akv10/template/del/");
        api.setCategoryPath("/akv10/category/");
        api.setHistoryDetailPath("/akv10/history/detail/");
        api.setBrandTalkSendPath("/brandtalk/template/send/");
        api.setSensitivePaths(Set.of(
            "/akv10/alimtalk/send/",
            "/brandtalk/template/send/",
            "/akv10/history/list/",
            "/akv10/history/detail/",
            "/akv10/profile/auth/",
            "/akv10/profile/add/"
        ));
    }
}
