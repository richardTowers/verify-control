package uk.gov.ida.hub.control.statechart;

import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.errors.StateProcessingException;

public interface VerifySessionState {
    static VerifySessionState forName(String name) {
        if (name == null) { throw new NullPointerException("Parameter 'name' cannot be null"); }
        switch (name) {
            case Started.NAME: return new Started();
            case IdpSelected.NAME: return new IdpSelected();
            case Match.NAME: return new Match();
            default: throw new NotImplementedException("No State for name '" + name + "'");
        }
    }

    // Transitions
    default IdpSelected selectIdp() { throw new StateProcessingException("selectIdp", this); }

    // Methods
    String getName();

    // States
    final class Started implements VerifySessionState {
        public static final String NAME = "started";

        @Override
        public IdpSelected selectIdp() { return new IdpSelected(); }

        @Override
        public String getName() { return NAME; }
    }

    final class IdpSelected implements VerifySessionState {
        public static final String NAME = "idp-selected";

        @Override
        public String getName() { return NAME; }
    }

    final class Match implements VerifySessionState {
        public static final String NAME = "match";

        @Override
        public String getName() { return NAME; }
    }
}
