package com.personal.marketnote.reward.configuration;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "reward")
@Validated
@Getter
@Setter
public class RewardProperties {

    @NotNull
    @DecimalMin(value = "0.0", message = "share-point-rate는 0.0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "share-point-rate는 1.0 이하여야 합니다.")
    private BigDecimal sharePointRate;
}
