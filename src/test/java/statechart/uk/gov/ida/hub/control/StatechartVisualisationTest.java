package statechart.uk.gov.ida.hub.control;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.assertj.core.api.Assertions.fail;

public class StatechartVisualisationTest {
    @Test
    public void createStateChart() {
        Set<StateHierarchy> stateHierarchy = StatechartAnalyzer.getStateHierarchy(
            VerifySessionState.Started.class,
            VerifySessionState.IdpSelected.class,
            VerifySessionState.AuthnFailed.class,
            VerifySessionState.FraudResponse.class,
            VerifySessionState.Matching.class,
            VerifySessionState.AwaitingCycle3Data.class,
            VerifySessionState.Cycle0And1MatchRequestSent.class,
            VerifySessionState.Cycle3MatchRequestSent.class,
            VerifySessionState.MatchingFailed.class,
            VerifySessionState.UserAccountCreationRequestSent.class,
            VerifySessionState.UserAccountCreated.class,
            VerifySessionState.Match.class);

        String result = PlantUmlPrinter.printPlantUml(stateHierarchy);
        String imagePath = "./images/statechart.svg";
        try (var stream = new BufferedOutputStream(Files.newOutputStream(Paths.get(imagePath), CREATE, TRUNCATE_EXISTING))) {
            new SourceStringReader(result).generateImage(stream, new FileFormatOption(FileFormat.SVG));
        } catch (IOException e) {
            fail("Could not write statechart image to '" + imagePath + "'");
        } catch (Exception e) {
            fail("Could not generate statechart", e);
        }
    }
}
