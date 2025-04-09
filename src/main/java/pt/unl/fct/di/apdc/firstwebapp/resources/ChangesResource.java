package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAccountStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.RemUserData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/changes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangesResource {


    private static final String PUBLIC = "PUBLIC";
    private static final String PRIVATE = "PRIVATE";
    private static final String ACTIVATED = "ACTIVATED";
    private static final String SUSPENDED = "SUSPENDED";
    private static final String DEACTIVATED = "DEACTIVATED";
    private static final String ENDUSER = "ENDUSER";
    private static final String BACKOFFICE = "BACKOFFICE";
    private static final String ADMIN = "ADMIN";
    private static final String PARTNER = "PARTNER";
    private static final String EMPTY = "";
    private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());

    private final static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    public ChangesResource() {} //nothing to be done here @GET

    // ========== AUTH TOKEN VALIDATION ==========
    private Entity validateTokenAndGetUser(String tokenID) {
        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(tokenID);
        Entity token = datastore.get(tokenKey);

        if (token == null || token.getTimestamp("expiration").toDate().before(new Date())) {
            return null;
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("username"));
        return datastore.get(userKey);
    }


    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(@Context HttpHeaders headers, ChangeRoleData data) {
        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        //checks if the authHeader is valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //cuts the bearer word to get the userId
        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = validateTokenAndGetUser(tokenID);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        if (data.role == null || !data.validRole()) {
            LOG.info("Invalid role.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String userRole = user.getString("user_role");

        if (!(userRole.equals(ADMIN) ||
                (userRole.equals(BACKOFFICE) && (data.role.equalsIgnoreCase(PARTNER) || data.role.equalsIgnoreCase(ENDUSER))))) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.otherUser);
            Entity other = datastore.get(otherKey);

            if (other == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Entity updatedUser = Entity.newBuilder(otherKey)
                    .set("user_userName", other.getString("user_userName"))
                    .set("user_name", other.getString("user_name"))
                    .set("user_pwd", other.getString("user_pwd")) // No need to hash again
                    .set("user_email", other.getString("user_email"))
                    .set("user_profile", other.getString("user_profile"))
                    .set("user_role", data.role.toUpperCase())
                    .set("user_account_state", other.getString("user_account_state"))
                    .set("address", other.getString("address"))
                    .set("cc", other.getString("cc"))
                    .set("NIF", other.getString("NIF"))
                    .set("employer", other.getString("employer"))
                    .set("function", other.getString("function"))
                    .build();

            datastore.put(updatedUser);
            return Response.ok().build();
        } catch (DatastoreException e) {
            LOG.log(Level.SEVERE, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
    }


    @POST
    @Path("/accountState")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountState(@Context HttpHeaders headers, ChangeAccountStateData data){

        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        //checks if the authHeader is valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //cuts the bearer word to get the userId
        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = validateTokenAndGetUser(tokenID);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }


        if(data.state == null || !data.validState()){
            LOG.info("Null or not valid state");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String userRole = user.getString("user_role");

        if (!(userRole.equals(ADMIN) ||
                (userRole.equals(BACKOFFICE) && (data.state.equalsIgnoreCase(ACTIVATED) || data.state.equalsIgnoreCase(DEACTIVATED))))) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {



            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

            Entity other = datastore.get(otherKey);

            if(other == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }


            Entity updatedUser = Entity.newBuilder(otherKey)
                    .set("user_userName", other.getString("user_userName"))
                    .set("user_name", other.getString("user_name"))
                    .set("user_pwd", DigestUtils.sha512Hex(other.getString("user_pwd")))
                    .set("user_email", other.getString("user_email"))
                    .set("user_profile", other.getString("user_profile"))
                    .set("user_role", other.getString("user_role"))
                    .set("user_account_state", data.state.toUpperCase())
                    .set("address", other.getString("address"))
                    .set("cc", EMPTY)
                    .set("NIF", EMPTY)
                    .set("employer", EMPTY)
                    .set("function", EMPTY).build();

            datastore.add(updatedUser);

            return Response.ok().build();
        }
        catch(DatastoreException e) {
            LOG.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        }
    }


    @POST
    @Path("/remUserAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUserAccount(@Context HttpHeaders headers, RemUserData data){

        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        //checks if the authHeader is valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //cuts the bearer word to get the userId
        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = validateTokenAndGetUser(tokenID);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String userRole = user.getString("user_role");

        try {

            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.other);

            Entity other = datastore.get(otherKey);

            if(other == null){
                LOG.info("User does not exist.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (!(userRole.equals(ADMIN) ||
                    userRole.equals(BACKOFFICE) &&
                            (other.getString("user_role").equals(PARTNER) ||
                                    other.getString("user_role").equals(ENDUSER)))) {
                LOG.info("Nonono you dont have permission for that");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            datastore.delete(otherKey);
            return Response.ok().build();

        }
        catch(DatastoreException e){
            LOG.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        }
    }

    @POST
    @Path("/AccountAttrib")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAttribs(){
        return Response.ok().build();
    }
    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(){
        return Response.ok().build();
    }


    @POST
    @Path("/logOut")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logOut(@HeaderParam("Authorization") String authHeader){

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing or invalid Authorization header")
                    .build();
        }

        String tokenId = authHeader.substring("Bearer ".length());

        try {
            Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(tokenId);
            datastore.delete(tokenKey);  // This invalidates the token
            LOG.info("Token invalidated: " + tokenId);
        } catch (Exception e) {
            LOG.warning("Failed to invalidate token: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }
}
