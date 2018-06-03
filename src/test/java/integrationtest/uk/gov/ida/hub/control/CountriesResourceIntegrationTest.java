package integrationtest.uk.gov.ida.hub.control;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;

public class CountriesResourceIntegrationTest {
    @Ignore
    @Test
    public void shouldReturnCountriesWhenEidasJourneyIsEnabled() {
        throw new NotImplementedException("Test shouldReturnCountriesWhenEidasJourneyIsEnabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnOnlyEnabledCountriesWhenEidasJourneyIsEnabled() {
        throw new NotImplementedException("Test shouldReturnOnlyEnabledCountriesWhenEidasJourneyIsEnabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnOnlyEnabledCountriesForRpWhenEidasJourneyIsEnabled() {
        throw new NotImplementedException("Test shouldReturnOnlyEnabledCountriesForRpWhenEidasJourneyIsEnabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnAllEnabledCountriesWhenNoCountriesConfiguredForRp() {
        throw new NotImplementedException("Test shouldReturnAllEnabledCountriesWhenNoCountriesConfiguredForRp has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnErrorWhenRequestingAListOfCountriesWithoutExistingSession() {
        throw new NotImplementedException("Test shouldReturnErrorWhenRequestingAListOfCountriesWithoutExistingSession has not been implemented");
    }

    @Ignore
    @Test
    public void shouldSelectCountryWhenEidasJourneyIsEnabled() {
        throw new NotImplementedException("Test shouldSelectCountryWhenEidasJourneyIsEnabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnBadRequestWhenWrongCountryIsSelected() {
        throw new NotImplementedException("Test shouldReturnBadRequestWhenWrongCountryIsSelected has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnBadRequestWhenCountryIsSelectedAndSessionDoesNotSupportEidas() {
        throw new NotImplementedException("Test shouldReturnBadRequestWhenCountryIsSelectedAndSessionDoesNotSupportEidas has not been implemented");
    }

    @Ignore
    @Test
    public void shouldBeAbleToSelectAnotherCountryWhenEidasJourneyIsEnabled() {
        throw new NotImplementedException("Test shouldBeAbleToSelectAnotherCountryWhenEidasJourneyIsEnabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldNotBeAbleToSelectCountryWhichIsNotEnabled() {
        throw new NotImplementedException("Test shouldNotBeAbleToSelectCountryWhichIsNotEnabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldNotBeAbleToSelectCountryWhichIsNotEnabledForRp() {
        throw new NotImplementedException("Test shouldNotBeAbleToSelectCountryWhichIsNotEnabledForRp has not been implemented");
    }
}
