package uk.gov.ida.hub.control.statechart;

import org.apache.commons.lang3.NotImplementedException;

public final class VerifySessionStateFactory {
    private VerifySessionStateFactory() { }

    public static VerifySessionState forName(String name) {
        if (name == null) { throw new NullPointerException("Parameter 'name' cannot be null"); }
        else if (name.equals(VerifySessionState.Started.class.getSimpleName()))                        { return new VerifySessionState.Started();                        }
        else if (name.equals(VerifySessionState.IdpSelected.class.getSimpleName()))                    { return new VerifySessionState.IdpSelected();                    }
        else if (name.equals(VerifySessionState.AuthnFailed.class.getSimpleName()))                    { return new VerifySessionState.AuthnFailed();                    }
        else if (name.equals(VerifySessionState.Match.class.getSimpleName()))                          { return new VerifySessionState.Match();                          }
        else if (name.equals(VerifySessionState.Cycle0And1MatchRequestSent.class.getSimpleName()))     { return new VerifySessionState.Cycle0And1MatchRequestSent();     }
        else if (name.equals(VerifySessionState.AwaitingCycle3Data.class.getSimpleName()))             { return new VerifySessionState.AwaitingCycle3Data();             }
        else if (name.equals(VerifySessionState.Cycle3MatchRequestSent.class.getSimpleName()))         { return new VerifySessionState.Cycle3MatchRequestSent();         }
        else if (name.equals(VerifySessionState.MatchingFailed.class.getSimpleName()))                 { return new VerifySessionState.MatchingFailed();                 }
        else if (name.equals(VerifySessionState.UserAccountCreationRequestSent.class.getSimpleName())) { return new VerifySessionState.UserAccountCreationRequestSent(); }
        else if (name.equals(VerifySessionState.UserAccountCreated.class.getSimpleName()))             { return new VerifySessionState.UserAccountCreated();             }
        else if (name.equals(VerifySessionState.FraudResponse.class.getSimpleName()))                  { return new VerifySessionState.FraudResponse();                  }
        else {
            throw new NotImplementedException("No State for name '" + name + "'");
        }
    }
}
