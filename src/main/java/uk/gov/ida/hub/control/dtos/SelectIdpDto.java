package uk.gov.ida.hub.control.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SelectIdpDto {
    @NotEmpty
    private final String selectedIdpEntityId;
    @NotEmpty
    private final String principalIpAddress;
    private final boolean registration;
    private final LevelOfAssurance requestedLoa;

    @JsonCreator
    public SelectIdpDto(
        @JsonProperty("selectedIdpEntityId") String selectedIdpEntityId,
        @JsonProperty("principalIpAddress") String principalIpAddress,
        @JsonProperty("registration") boolean registration,
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
