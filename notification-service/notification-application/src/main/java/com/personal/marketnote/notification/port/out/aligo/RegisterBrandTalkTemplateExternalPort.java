package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;

import java.util.List;

public interface RegisterBrandTalkTemplateExternalPort {

    void registerTemplate(
            AligoBrandTalkTemplate template, byte[] imageFile, String imageFileName,
            List<byte[]> carouselItemImageFiles, List<String> carouselItemImageFileNames
    );
}
