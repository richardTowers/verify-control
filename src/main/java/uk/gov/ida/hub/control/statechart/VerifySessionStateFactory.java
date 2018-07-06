package uk.gov.ida.hub.control.statechart;

import org.apache.commons.lang3.NotImplementedException;

public final class VerifySessionStateFactory {
    private VerifySessionStateFactory() { }

    public static VerifySessionState forName(String name) {
        if (name == null) { throw new NullPointerException("Parameter 'name' cannot be null"); }
        switch (name) {
            case VerifySessionState.Started.NAME                        : return new VerifySessionState.Started();
            case VerifySessionState.IdpSelected.NAME                    : return new VerifySessionState.IdpSelected();
            case VerifySessionState.AuthnFailed.NAME                    : return new VerifySessionState.AuthnFailed();
            case VerifySessionState.Match.NAME                          : return new VerifySessionState.Match();
            case VerifySessionState.Cycle0And1MatchRequestSent.NAME     : return new VerifySessionState.Cycle0And1MatchRequestSent();
            case VerifySessionState.AwaitingCycle3Data.NAME             : return new VerifySessionState.AwaitingCycle3Data();
            case VerifySessionState.Cycle3MatchRequestSent.NAME         : return new VerifySessionState.Cycle3MatchRequestSent();
            case VerifySessionState.MatchingFailed.NAME                 : return new VerifySessionState.MatchingFailed();
            case VerifySessionState.UserAccountCreationRequestSent.NAME : return new VerifySessionState.UserAccountCreationRequestSent();
            case VerifySessionState.UserAccountCreated.NAME             : return new VerifySessionState.UserAccountCreated();
            case VerifySessionState.FraudResponse.NAME                  : return new VerifySessionState.FraudResponse();
            default: throw new NotImplementedException("No State for name '" + name + "'");
        }
    }
}
