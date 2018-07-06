package uk.gov.ida.hub.control.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class SelectIdpDto {
    @NotEmpty
    private final String selectedIdpEntityId;
    @NotEmpty
    private final String principalIpAddress;
    @NotNull
    private final Boolean registration;
    @NotNull
    private final LevelOfAssurance requestedLoa;

    @JsonCreator
    public SelectIdpDto(
        @JsonProperty("selectedIdpEntityId") String selectedIdpEntityId,
        @JsonProperty("principalIpAddress") String principalIpAddress,
        @JsonProperty("registration") Boolean registration,
        @JsonProperty("requestedLoa") LevelOfAssurance requestedLoa
    ) {
        this.selectedIdpEntityId = selectedIdpEntityId;
        this.principalIpAddress = principalIpAddress;
        this.registration = registration;
        this.requestedLoa = requestedLoa;
    }

    public String getSelectedIdpEntityId() {
        return selectedIdpEntityId;
    }

    public String getPrincipalIpAddress() {
        return principalIpAddress;
    }

    public boolean isRegistration() {
        return registration;
    }

    public LevelOfAssurance getRequestedLoa() {
        return requestedLoa;
    }
}
