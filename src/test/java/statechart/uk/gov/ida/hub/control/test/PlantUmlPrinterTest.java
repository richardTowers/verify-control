package statechart.uk.gov.ida.hub.control.test;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import statechart.uk.gov.ida.hub.control.PlantUmlPrinter;
import statechart.uk.gov.ida.hub.control.StateHierarchy;
import statechart.uk.gov.ida.hub.control.StateTransition;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;

public class PlantUmlPrinterTest {

  private Set<StateHierarchy> stateHierarchies = ImmutableSet.of(
      new StateHierarchy(
          "open",
          true,
          ImmutableSet.of(
              new StateHierarchy(
                  "held",
                  false,
                  emptySet(),
                  ImmutableSet.of(
                      new StateTransition("removeHold", "notHeld"),
                      new StateTransition("availableToWithdraw", "held")
                  )
              ),
              new StateHierarchy(
                  "notHeld",
                  true,
                  emptySet(),
                  ImmutableSet.of(
                      new StateTransition("placeHold", "held"),
                      new StateTransition("withdraw", "notHeld"),
                      new StateTransition("availableToWithdraw", "notHeld")
                  )
              )
          ),
          ImmutableSet.of(
              new StateTransition("deposit", "open"),
              new StateTransition("close", "closed")
          )
      ),
      new StateHierarchy(
          "closed",
          false,
          emptySet(),
          ImmutableSet.of(
              new StateTransition("reopen", "open")
          )
      )
  );

  @Test
  public void shouldPrintPlantUml() {
    String result = PlantUmlPrinter.printPlantUml(stateHierarchies);

    assertEquals("@startuml\n" +
        "skinparam monochrome true\n" +
        "skinparam defaultFontName Courier\n" +
        "skinparam Shadowing false\n" +
        "[*] --> open\n" +
        "state open {\n" +
        "  held --> notHeld: removeHold\n" +
        "  held --> held: availableToWithdraw\n" +
        "  [*] --> notHeld\n" +
        "  notHeld --> held: placeHold\n" +
        "  notHeld --> notHeld: withdraw\n" +
        "  notHeld --> notHeld: availableToWithdraw\n" +
        "}\n" +
        "open --> open: deposit\n" +
        "open --> closed: close\n" +
        "closed --> open: reopen\n" +
        "@enduml", result);
  }
}
