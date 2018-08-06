package statechart.uk.gov.ida.hub.control;

import java.util.Set;
import java.util.function.Predicate;

public class PlantUmlPrinter {
  public static String printPlantUml(Set<StateHierarchy> stateHierarchy) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("@startuml\n")
        .append("skinparam monochrome true\n")
        .append("skinparam defaultFontName Courier\n")
        .append("skinparam Shadowing false\n");
    printPlantUml(stateHierarchy, stringBuilder, "", x -> true);
    stringBuilder.append("@enduml");
    return stringBuilder.toString();
  }

  private static void printPlantUml(Set<StateHierarchy> stateHierarchies, StringBuilder stringBuilder, String indent, Predicate<StateTransition> shouldPrintTransition) {
    stateHierarchies.stream().sorted((x, y) -> Boolean.compare(y.initial, x.initial)).forEach(currentState -> {
      if (currentState.initial && shouldPrintTransition.test(new StateTransition("[*]", currentState.name))) {
        stringBuilder.append(indent).append("[*] --> ").append(currentState.name).append("\n");
      }
      if (currentState.children.size() > 0) {
        stringBuilder.append(indent).append("state ").append(currentState.name).append(" {\n");
        printPlantUml(currentState.children, stringBuilder, indent + "  ", x -> currentState.children.stream().anyMatch(y -> y.name.equals(x.targetState)));
        stringBuilder.append(indent).append("}\n");
        printPlantUml(currentState.children, stringBuilder, indent, x -> currentState.children.stream().noneMatch(y -> y.name.equals(x.targetState)));
      }
      currentState.transitions
          .stream()
          .filter(shouldPrintTransition)
          .forEach(x -> stringBuilder
          .append(indent).append(currentState.name).append(" --> ").append(x.targetState).append(": ").append(x.name).append("\n"));
    });
  }
}
