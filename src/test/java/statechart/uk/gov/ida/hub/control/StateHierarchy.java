package statechart.uk.gov.ida.hub.control;

import java.util.Set;

public class StateHierarchy {
  final String name;
  final boolean initial;
  final Set<StateHierarchy> children;
  final Set<StateTransition> transitions;

  public StateHierarchy(String name, boolean initial, Set<StateHierarchy> children, Set<StateTransition> transitions) {
    if (name == null) { throw new IllegalArgumentException("name"); }
    if (children == null) { throw new IllegalArgumentException("children"); }
    if (transitions == null) { throw new IllegalArgumentException("transitions"); }

    this.name = name;
    this.initial = initial;
    this.children = children;
    this.transitions = transitions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StateHierarchy that = (StateHierarchy) o;

    if (initial != that.initial) return false;
    if (!name.equals(that.name)) return false;
    if (!children.equals(that.children)) return false;
    return transitions.equals(that.transitions);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (initial ? 1 : 0);
    result = 31 * result + children.hashCode();
    result = 31 * result + transitions.hashCode();
    return result;
  }

}
