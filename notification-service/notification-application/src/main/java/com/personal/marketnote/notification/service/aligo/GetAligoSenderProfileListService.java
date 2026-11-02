package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoSenderProfileListResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoSenderProfileListUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoSenderProfileListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoSenderProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
public class GetAligoSenderProfileListService implements GetAligoSenderProfileListUseCase {

    private final GetAligoSenderProfileListExternalPort getAligoSenderProfileListExternalPort;
    private final FindAligoSenderProfilePort findAligoSenderProfilePort;
    private final SaveAligoSenderProfilePort saveAligoSenderProfilePort;
    private final UpdateAligoSenderProfilePort updateAligoSenderProfilePort;

    @Override
    public GetAligoSenderProfileListResult getSenderProfileList() {
        List<AligoSenderProfile> externalProfiles = getAligoSenderProfileListExternalPort.getProfileList();

        List<AligoSenderProfile> syncedProfiles = syncProfiles(externalProfiles);

        List<GetAligoSenderProfileListResult.AligoSenderProfileResult> results = syncedProfiles.stream()
                .map(this::toResult)
                .toList();

        return new GetAligoSenderProfileListResult(results);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<AligoSenderProfile> syncProfiles(List<AligoSenderProfile> externalProfiles) {
        List<AligoSenderProfile> localProfiles = findAligoSenderProfilePort.findAll();
        Map<String, AligoSenderProfile> localMap = localProfiles.stream()
                .collect(Collectors.toMap(AligoSenderProfile::getSenderKey, p -> p));

        List<AligoSenderProfile> syncedProfiles = new ArrayList<>();

        for (AligoSenderProfile externalProfile : externalProfiles) {
            AligoSenderProfile local = localMap.get(externalProfile.getSenderKey());

            if (FormatValidator.hasNoValue(local)) {
                AligoSenderProfile saved = saveAligoSenderProfilePort.save(externalProfile);
                syncedProfiles.add(saved);
                continue;
            }

            local.updateFromExternal(
                    externalProfile.getProfileInspectionStatus(),
                    externalProfile.getSenderProfileStatus(),
                    externalProfile.isAlimUseYn()
            );
            updateAligoSenderProfilePort.update(local);
            syncedProfiles.add(local);
        }

        return syncedProfiles;
    }

    private GetAligoSenderProfileListResult.AligoSenderProfileResult toResult(AligoSenderProfile profile) {
        return new GetAligoSenderProfileListResult.AligoSenderProfileResult(
                profile.getId(),
                profile.getSenderKey(),
                profile.getPlusId(),
                profile.getName(),
                profile.getCategoryCode(),
                FormatValidator.hasValue(profile.getProfileInspectionStatus())
                        ? profile.getProfileInspectionStatus().getDescription() : null,
                FormatValidator.hasValue(profile.getSenderProfileStatus())
                        ? profile.getSenderProfileStatus().getDescription() : null,
                profile.isAlimUseYn()
        );
    }
}
