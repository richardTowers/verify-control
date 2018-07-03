package uk.gov.ida.hub.control.statechart;

import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.errors.StateProcessingException;
import uk.gov.ida.hub.control.statechart.annotations.State;
import uk.gov.ida.hub.control.statechart.annotations.Transition;

public interface VerifySessionState {
    static VerifySessionState forName(String name) {
        if (name == null) { throw new NullPointerException("Parameter 'name' cannot be null"); }
        switch (name) {
            case Started.NAME: return new Started();
            case IdpSelected.NAME: return new IdpSelected();
            case AuthnFailed.NAME: return new AuthnFailed();
            case Match.NAME: return new Match();
            default: throw new NotImplementedException("No State for name '" + name + "'");
        }
    }

    // Transitions
    default IdpSelected selectIdp() { throw new StateProcessingException("selectIdp", this); }
    default AuthnFailed authenticationFailed() { throw new StateProcessingException("authenticationFailed", this); }
    default Cycle0And1MatchRequestSent authenticationSucceeded() { throw new StateProcessingException("authenticationSucceeded", this); }

    // Methods
    String getName();


    // States
    @State(name = Started.NAME, initial = true)
    final class Started implements VerifySessionState {
        public static final String NAME = "started";

        @Override
        @Transition
        public IdpSelected selectIdp() { return new IdpSelected(); }

        @Override
        public String getName() { return NAME; }
    }

    @State(name = IdpSelected.NAME)
    final class IdpSelected implements VerifySessionState {
        public static final String NAME = "idpSelected";

        @Transition
        @Override
        public AuthnFailed authenticationFailed() { return new AuthnFailed(); }

        @Transition
        @Override
        public Cycle0And1MatchRequestSent authenticationSucceeded() { return new Cycle0And1MatchRequestSent(); }

        @Override
        public String getName() { return NAME; }
    }

    @State(name = AuthnFailed.NAME)
    final class AuthnFailed implements VerifySessionState {
        public static final String NAME = "authnFailed";

        @Transition
        @Override
        public IdpSelected selectIdp() { return new IdpSelected(); }

        @Override
        public String getName() { return NAME; }
    }

    @State(name = Matching.NAME)
    abstract class Matching implements VerifySessionState {
        public static final String NAME = "matching";

        @Override
        public String getName() { return NAME; }
    }

    @State(name = Cycle0And1MatchRequestSent.NAME, initial = true)
    final class Cycle0And1MatchRequestSent extends Matching implements VerifySessionState {
        public static final String NAME = "cycle0And1MatchRequestSent";

        @Override
        public String getName() { return NAME; }
    }

    @State(name = Match.NAME)
    final class Match implements VerifySessionState {
        public static final String NAME = "match";

        @Override
        public String getName() { return NAME; }
    }
}
