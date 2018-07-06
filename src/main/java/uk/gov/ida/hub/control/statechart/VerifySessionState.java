package uk.gov.ida.hub.control.statechart;

import uk.gov.ida.hub.control.errors.StateProcessingException;
import uk.gov.ida.hub.control.statechart.annotations.State;
import uk.gov.ida.hub.control.statechart.annotations.Transition;

public interface VerifySessionState {
    // Transitions
    default IdpSelected                    selectIdp()                      { throw new StateProcessingException("selectIdp"                     , this); }
    default AuthnFailed                    authenticationFailed()           { throw new StateProcessingException("authenticationFailed"          , this); }
    default FraudResponse                  fraudResponse()                  { throw new StateProcessingException("fraudResponse"                 , this); }
    default Matching                       authenticationSucceeded()        { throw new StateProcessingException("authenticationSucceeded"       , this); }
    default AwaitingCycle3Data             awaitCycle3Data()                { throw new StateProcessingException("awaitCycle3Data"               , this); }
    default Cycle3MatchRequestSent         submitCycle3Request()            { throw new StateProcessingException("submitCycle3Request"           , this); }
    default MatchingFailed                 cancelCycle3Request()            { throw new StateProcessingException("cancelCycle3Request"           , this); }
    default Match                          match()                          { throw new StateProcessingException("match"                         , this); }
    default MatchingFailed                 noMatch()                        { throw new StateProcessingException("noMatch"                       , this); }
    default UserAccountCreationRequestSent sendUserAccountCreationRequest() { throw new StateProcessingException("sendUserAccountCreationRequest", this); }
    default MatchingFailed                 userAccountCreationFailed()      { throw new StateProcessingException("userAccountCreationFailed"     , this); }
    default UserAccountCreated             userAccountCreationSucceeded()   { throw new StateProcessingException("userAccountCreationSucceeded"  , this); }

    // Methods
    String getName();

    // States
    @State(name = Started.NAME, initial = true)
    final class Started implements VerifySessionState {
        public static final String NAME = "started";

        @Override @Transition public IdpSelected selectIdp() { return new IdpSelected(); }

        @Override public String getName() { return NAME; }
    }

    @State(name = IdpSelected.NAME)
    final class IdpSelected implements VerifySessionState {
        public static final String NAME = "idpSelected";

        @Transition @Override public AuthnFailed authenticationFailed() { return new AuthnFailed(); }
        @Transition @Override public FraudResponse fraudResponse() { return new FraudResponse(); }
        @Transition @Override public Matching authenticationSucceeded() { return new Cycle0And1MatchRequestSent(); }

        @Override public String getName() { return NAME; }
    }

    @State(name = AuthnFailed.NAME)
    final class AuthnFailed implements VerifySessionState {
        public static final String NAME = "authnFailed";

        @Transition @Override public IdpSelected selectIdp() { return new IdpSelected(); }

        @Override
        public String getName() { return NAME; }
    }

    @State(name = FraudResponse.NAME)
    final class FraudResponse implements VerifySessionState {
        public static final String NAME = "fraudResponse";

        @Override public String getName() { return NAME; }
    }

    @State(name = Matching.NAME)
    abstract class Matching implements VerifySessionState {
        static final String NAME = "matching";

        @Override
        public String getName() { return NAME; }
    }

    @State(name = Cycle0And1MatchRequestSent.NAME, initial = true)
    final class Cycle0And1MatchRequestSent extends Matching {
        public static final String NAME = "cycle0And1MatchRequestSent";

        @Transition @Override public Match match() { return new Match(); }
        @Transition @Override public MatchingFailed noMatch() { return new MatchingFailed(); }
        @Transition @Override public AwaitingCycle3Data awaitCycle3Data() { return new AwaitingCycle3Data(); }
        @Transition @Override public UserAccountCreationRequestSent sendUserAccountCreationRequest() { return new UserAccountCreationRequestSent(); }

        @Override
        public String getName() { return NAME; }
    }

    @State(name = AwaitingCycle3Data.NAME)
    final class AwaitingCycle3Data extends Matching {
        public static final String NAME = "awaitingCycle3Data";

        @Transition @Override public Cycle3MatchRequestSent submitCycle3Request() { return new Cycle3MatchRequestSent(); }
        @Transition @Override public MatchingFailed cancelCycle3Request() { return new MatchingFailed(); }

        @Override public String getName() { return NAME; }
    }

    @State(name = Match.NAME)
    final class Match implements VerifySessionState {
        public static final String NAME = "match";

        @Override public String getName() { return NAME; }
    }

    @State(name = Cycle3MatchRequestSent.NAME)
    final class Cycle3MatchRequestSent extends Matching {
        public static final String NAME = "cycle3MatchRequestSent";

        @Transition @Override public Match match() { return new Match(); }
        @Transition @Override public MatchingFailed noMatch() { return new MatchingFailed(); }
        @Transition @Override public UserAccountCreationRequestSent sendUserAccountCreationRequest() { return new UserAccountCreationRequestSent(); }

        @Override public String getName() { return NAME; }
    }

    @State(name = UserAccountCreationRequestSent.NAME)
    final class UserAccountCreationRequestSent extends Matching {
        public static final String NAME = "userAccountCreationRequestSent";

        @Transition @Override public MatchingFailed userAccountCreationFailed() { return new MatchingFailed(); }
        @Transition @Override public UserAccountCreated userAccountCreationSucceeded() { return new UserAccountCreated(); }

        @Override public String getName() { return NAME; }
    }

    @State(name = UserAccountCreated.NAME)
    final class UserAccountCreated extends Matching {
        public static final String NAME = "userAccountCreated";

        @Override public String getName() { return NAME; }
    }

    @State(name = MatchingFailed.NAME)
    final class MatchingFailed extends Matching {
        public static final String NAME = "matchingFailed";

        @Override public String getName() { return NAME; }
    }
}
