package integrationtest.uk.gov.ida.hub.control;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;

public class EidasMatchingServiceResourceIntegrationTest {
    @Ignore
    @Test
    public void shouldTransitionToEidasSuccessfulMatchStateWhenMatchIsReceivedForEidasCycle0And1() {
        throw new NotImplementedException("Test shouldTransitionToEidasSuccessfulMatchStateWhenMatchIsReceivedForEidasCycle0And1 has not been implemented");
    }

    @Ignore
    @Test
    public void shouldTransitionToEidasAwaitingCycle3DataStateWhenNoMatchIsReceivedForEidasCycle0And1WithCycle3Enabled() {
        throw new NotImplementedException("Test shouldTransitionToEidasAwaitingCycle3DataStateWhenNoMatchIsReceivedForEidasCycle0And1WithCycle3Enabled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldTransitionToNoMatchStateWhenNoMatchIsReceivedForEidasCycle0And1WithCycle3Disabled() {
        throw new NotImplementedException("Test shouldTransitionToNoMatchStateWhenNoMatchIsReceivedForEidasCycle0And1WithCycle3Disabled has not been implemented");
    }
}
