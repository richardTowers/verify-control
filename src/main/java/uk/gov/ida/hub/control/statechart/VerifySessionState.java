package uk.gov.ida.hub.control.statechart;

import uk.gov.ida.hub.control.data.MatchingStage;
import uk.gov.ida.hub.control.data.ResponseProcessingStage;
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
    default MatchingStage getMatchingStage() { throw new StateProcessingException("getMatchingStage", this); }
    default ResponseProcessingStage getResponseProcessingStage() { throw new StateProcessingException("getResponseProcessingStage", this); }

    // States
    @State(name = "started", initial = true)
    final class Started implements VerifySessionState {
        @Override @Transition public IdpSelected selectIdp() { return new IdpSelected(); }
    }

    @State(name = "idpSelected")
    final class IdpSelected implements VerifySessionState {
        @Transition @Override public AuthnFailed   authenticationFailed()    { return new AuthnFailed();                }
        @Transition @Override public FraudResponse fraudResponse()           { return new FraudResponse();              }
        @Transition @Override public Matching      authenticationSucceeded() { return new Cycle0And1MatchRequestSent(); }
    }

    @State(name = "authnFailed")
    final class AuthnFailed implements VerifySessionState {
        @Transition @Override public IdpSelected selectIdp() { return new IdpSelected(); }
    }

    @State(name = "fraudResponse")
    final class FraudResponse implements VerifySessionState { }

    @State(name = "matching")
    abstract class Matching implements VerifySessionState { }

    @State(name = "cycle0And1MatchRequestSent", initial = true)
    final class Cycle0And1MatchRequestSent extends Matching {
        @Transition @Override public Match                          match()                          { return new Match();                          }
        @Transition @Override public MatchingFailed                 noMatch()                        { return new MatchingFailed();                 }
        @Transition @Override public AwaitingCycle3Data             awaitCycle3Data()                { return new AwaitingCycle3Data();             }
        @Transition @Override public UserAccountCreationRequestSent sendUserAccountCreationRequest() { return new UserAccountCreationRequestSent(); }

        @Override public MatchingStage getMatchingStage() { return MatchingStage.CYCLE_0_AND_1; }
    }

    @State(name = "awaitingCycle3Data")
    final class AwaitingCycle3Data extends Matching {
        @Transition @Override public Cycle3MatchRequestSent submitCycle3Request() { return new Cycle3MatchRequestSent(); }
        @Transition @Override public MatchingFailed         cancelCycle3Request() { return new MatchingFailed();         }
    }

    @State(name = "match")
    final class Match implements VerifySessionState { }

    @State(name = "cycle3MatchRequestSent")
    final class Cycle3MatchRequestSent extends Matching {
        @Transition @Override public Match                          match()                          { return new Match();                          }
        @Transition @Override public MatchingFailed                 noMatch()                        { return new MatchingFailed();                 }
        @Transition @Override public UserAccountCreationRequestSent sendUserAccountCreationRequest() { return new UserAccountCreationRequestSent(); }

        @Override public MatchingStage getMatchingStage() { return MatchingStage.CYCLE_3; }
    }

    @State(name = "userAccountCreationRequestSent")
    final class UserAccountCreationRequestSent extends Matching {
        @Transition @Override public MatchingFailed     userAccountCreationFailed()    { return new MatchingFailed();     }
        @Transition @Override public UserAccountCreated userAccountCreationSucceeded() { return new UserAccountCreated(); }
        @Override public ResponseProcessingStage getResponseProcessingStage() { return ResponseProcessingStage.USER_ACCOUNT_CREATION_REQUEST_SENT; }
    }

    @State(name = "userAccountCreated")
    final class UserAccountCreated extends Matching {
        @Override public ResponseProcessingStage getResponseProcessingStage() { return ResponseProcessingStage.USER_ACCOUNT_CREATED; }
    }

    @State(name = "matchingFailed")
    final class MatchingFailed extends Matching {
        @Override public ResponseProcessingStage getResponseProcessingStage() { return ResponseProcessingStage.MATCHING_FAILED; }
    }
}
