package statechart.uk.gov.ida.hub.control;

public class StateTransition {
  final String name;
  final String targetState;

  public StateTransition(String name, String targetState) {
    if (name == null) { throw new IllegalArgumentException("name"); }
    if (targetState == null) { throw new IllegalArgumentException("targetState"); }
    this.name = name;
    this.targetState = targetState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StateTransition that = (StateTransition) o;

    if (!name.equals(that.name)) return false;
    return targetState.equals(that.targetState);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + targetState.hashCode();
    return result;
  }
}
