package pt.unl.fct.di.apdc.firstwebapp.validations;


import com.google.cloud.datastore.*;
import java.util.Date;

public class AuthTokenValidator {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Entity validateToken(String tokenID) {
        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(tokenID);
        Entity token = datastore.get(tokenKey);

        if (token == null || token.getTimestamp("expiration").toDate().before(new Date())) {
            return null;
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("username"));
        return datastore.get(userKey);
    }

    public static String getUsernameFromToken(String tokenID) {
        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(tokenID);
        Entity token = datastore.get(tokenKey);
        return (token != null) ? token.getString("username") : null;
    }

}
