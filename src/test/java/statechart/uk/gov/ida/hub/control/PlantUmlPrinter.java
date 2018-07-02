package statechart.uk.gov.ida.hub.control;

import java.util.Set;

public class PlantUmlPrinter {
  public static String printPlantUml(Set<StateHierarchy> stateHierarchy) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("@startuml\n")
        .append("skinparam monochrome true\n")
        .append("skinparam defaultFontName Courier\n")
        .append("skinparam Shadowing false\n");
    printPlantUml(stateHierarchy, stringBuilder, "");
    stringBuilder.append("@enduml");
    return stringBuilder.toString();
  }

  private static void printPlantUml(Set<StateHierarchy> stateHierarchies, StringBuilder stringBuilder, String indent) {
    stateHierarchies.forEach(currentState -> {
      if (currentState.initial) {
        stringBuilder.append(indent).append("[*] --> ").append(currentState.name).append("\n");
      }
      if (currentState.children.size() > 0) {
        stringBuilder.append(indent).append("state ").append(currentState.name).append(" {\n");
        printPlantUml(currentState.children, stringBuilder, indent + "  ");
        stringBuilder.append(indent).append("}\n");
      }
      currentState.transitions.forEach(x -> stringBuilder
          .append(indent).append(currentState.name).append(" --> ").append(x.targetState).append(": ").append(x.name).append("\n"));
    });
  }
}
