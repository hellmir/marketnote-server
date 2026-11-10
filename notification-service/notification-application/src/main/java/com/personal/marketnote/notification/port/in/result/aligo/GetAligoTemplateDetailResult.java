package com.personal.marketnote.notification.port.in.result.aligo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 알림톡 템플릿 상세 조회 결과 객체.
 * <p>
 * Redis 캐시(`aligo:template:active`) value 로 저장되므로 record 가 아닌 일반 class 로 정의한다.
 * record 는 final 클래스라 Jackson NON_FINAL defaultTyping 환경에서 최상위 레벨 타입 래퍼(@class)가 누락되어
 * 역직렬화 시 InvalidTypeIdException 이 발생한다 (#4844, #4851 선례 참조).
 * <p>
 * accessor 는 record 호환을 위해 `id()`, `buttons()` 등 필드명 그대로 노출한다.
 */
public class GetAligoTemplateDetailResult {

    private final Long id;
    private final String senderKey;
    private final String templateCode;
    private final String templateName;
    private final String templateContent;
    private final String templateTitle;
    private final String templateSubtitle;
    private final String templateType;
    private final String templateEmType;
    private final boolean templateSecure;
    private final boolean templateAdvert;
    private final String imageUrl;
    private final String imageName;
    private final String inspStatus;
    private final String templateStatus;
    private final String comments;
    private final List<ButtonResult> buttons;

    @JsonCreator
    public GetAligoTemplateDetailResult(
            @JsonProperty("id") Long id,
            @JsonProperty("senderKey") String senderKey,
            @JsonProperty("templateCode") String templateCode,
            @JsonProperty("templateName") String templateName,
            @JsonProperty("templateContent") String templateContent,
            @JsonProperty("templateTitle") String templateTitle,
            @JsonProperty("templateSubtitle") String templateSubtitle,
            @JsonProperty("templateType") String templateType,
            @JsonProperty("templateEmType") String templateEmType,
            @JsonProperty("templateSecure") boolean templateSecure,
            @JsonProperty("templateAdvert") boolean templateAdvert,
            @JsonProperty("imageUrl") String imageUrl,
            @JsonProperty("imageName") String imageName,
            @JsonProperty("inspStatus") String inspStatus,
            @JsonProperty("templateStatus") String templateStatus,
            @JsonProperty("comments") String comments,
            @JsonProperty("buttons") List<ButtonResult> buttons
    ) {
        this.id = id;
        this.senderKey = senderKey;
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.templateTitle = templateTitle;
        this.templateSubtitle = templateSubtitle;
        this.templateType = templateType;
        this.templateEmType = templateEmType;
        this.templateSecure = templateSecure;
        this.templateAdvert = templateAdvert;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.inspStatus = inspStatus;
        this.templateStatus = templateStatus;
        this.comments = comments;
        this.buttons = buttons;
    }

    public Long id() {
        return id;
    }

    public String senderKey() {
        return senderKey;
    }

    public String templateCode() {
        return templateCode;
    }

    public String templateName() {
        return templateName;
    }

    public String templateContent() {
        return templateContent;
    }

    public String templateTitle() {
        return templateTitle;
    }

    public String templateSubtitle() {
        return templateSubtitle;
    }

    public String templateType() {
        return templateType;
    }

    public String templateEmType() {
        return templateEmType;
    }

    public boolean templateSecure() {
        return templateSecure;
    }

    public boolean templateAdvert() {
        return templateAdvert;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public String imageName() {
        return imageName;
    }

    public String inspStatus() {
        return inspStatus;
    }

    public String templateStatus() {
        return templateStatus;
    }

    public String comments() {
        return comments;
    }

    public List<ButtonResult> buttons() {
        return buttons;
    }

    public static class ButtonResult {

        private final int ordering;
        private final String name;
        private final String linkType;
        private final String linkMo;
        private final String linkPc;
        private final String linkIos;
        private final String linkAnd;

        @JsonCreator
        public ButtonResult(
                @JsonProperty("ordering") int ordering,
                @JsonProperty("name") String name,
                @JsonProperty("linkType") String linkType,
                @JsonProperty("linkMo") String linkMo,
                @JsonProperty("linkPc") String linkPc,
                @JsonProperty("linkIos") String linkIos,
                @JsonProperty("linkAnd") String linkAnd
        ) {
            this.ordering = ordering;
            this.name = name;
            this.linkType = linkType;
            this.linkMo = linkMo;
            this.linkPc = linkPc;
            this.linkIos = linkIos;
            this.linkAnd = linkAnd;
        }

        public int ordering() {
            return ordering;
        }

        public String name() {
            return name;
        }

        public String linkType() {
            return linkType;
        }

        public String linkMo() {
            return linkMo;
        }

        public String linkPc() {
            return linkPc;
        }

        public String linkIos() {
            return linkIos;
        }

        public String linkAnd() {
            return linkAnd;
        }
    }
}
