package statechart.uk.gov.ida.hub.control;

import uk.gov.ida.hub.control.statechart.annotations.State;
import uk.gov.ida.hub.control.statechart.annotations.Transition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class StatechartAnalyzer {

  public static Set<StateHierarchy> getStateHierarchy(Class<?>... classes) {

    Map<? extends Class<?>, Set<Class<?>>> classesBySuperClass = Arrays.stream(classes)
        .filter(x -> x.isAnnotationPresent(State.class))
        .collect(groupingBy(Class::getSuperclass, toSet()));

    return getStateHierarchy(classesBySuperClass, Object.class);
  }

  private static Set<StateHierarchy> getStateHierarchy(Map <? extends  Class<?>, Set<Class<?>>> classesBySuperClass, Class<?> currentClass) {
    Set<Class<?>> classes = classesBySuperClass.get(currentClass);
    if (classes == null) { return emptySet(); }
    return classes.stream()
        .map(currentState -> new StateHierarchy(
            getStateName(currentState),
            currentState.getDeclaredAnnotation(State.class).initial(),
            getStateHierarchy(classesBySuperClass, currentState),
            Arrays.stream(currentState.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Transition.class))
                .map(transitionMethod -> new StateTransition(transitionMethod.getName(), getNewStateName(currentState, transitionMethod)))
                .collect(toSet())
        ))
        .collect(toSet());
  }

  private static String getNewStateName(Class<?> currentStateClass, Method transitionMethod) {
    Class<?> returnType = transitionMethod.getReturnType();
    return returnType.isAnnotationPresent(State.class)
      ? getStateName(returnType)
      : getStateName(currentStateClass);
  }

  private static <T> String getStateName(Class<T> stateClass) {
      return stateClass.getDeclaredAnnotation(State.class).name();
  }

}
