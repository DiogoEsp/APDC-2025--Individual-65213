package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.appengine.repackaged.com.google.gson.JsonArray;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.*;
import pt.unl.fct.di.apdc.firstwebapp.validations.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/changes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangesResource {


    private static final String ENDUSER = "ENDUSER";
    private static final String BACKOFFICE = "BACKOFFICE";
    private static final String ADMIN = "ADMIN";
    private static final String PARTNER = "PARTNER";
    private static final String PUBLIC = "PUBLIC";
    private static final String ACTIVATED = "ACTIVATED";

    private static final String EMPTY = "";
    private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());

    private final static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    public ChangesResource() {} //nothing to be done here @GET


    @POST
    @Path("/userRole")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(@Context HttpHeaders headers, ChangeRoleData data){
        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String userRole = user.getString("user_role");

        if(data.role == null || data.username == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        if(!PermissionChecker.canChangeRole(userRole,data) || !PermissionChecker.isActive(user))
            return Response.status(Response.Status.FORBIDDEN).build();

        try{

            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity other = datastore.get(otherKey);

            if(other == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            Entity update = Entity.newBuilder(otherKey)
                    .set("user_userName", other.getString("user_userName"))
                    .set("user_name", other.getString("user_name"))
                    .set("user_pwd", other.getString("user_pwd"))
                    .set("user_email", other.getString("user_email"))
                    .set("user_profile", other.getString("user_profile"))
                    .set("user_role", data.role)
                    .set("user_account_state", other.getString("user_account_state"))
                    .set("address", other.getString("address"))
                    .set("cc", other.getString("cc"))
                    .set("NIF", other.getString("NIF"))
                    .set("employer", other.getString("employer"))
                    .set("function", other.getString("function")).build();

            datastore.put(update);
            return Response.ok().build();

        }catch(DatastoreException e){
            LOG.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
    }




    @POST
    @Path("/accountState")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountState(@Context HttpHeaders headers, ChangeAccountStateData data){

        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }


        if(data.state == null || data.username == null){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String userRole = user.getString("user_role");

        if (!PermissionChecker.canChangeStatus(userRole, data) || !PermissionChecker.isActive(user)) {
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
                    .set("user_pwd", other.getString("user_pwd"))
                    .set("user_email", other.getString("user_email"))
                    .set("user_profile", other.getString("user_profile"))
                    .set("user_role", other.getString("user_role"))
                    .set("user_account_state", data.state.toUpperCase())
                    .set("address", other.getString("address"))
                    .set("cc", other.getString("cc"))
                    .set("NIF", other.getString("NIF"))
                    .set("employer", other.getString("employer"))
                    .set("function", other.getString("function")).build();

            datastore.put(updatedUser);

            return Response.ok().build();
        }
        catch(DatastoreException e) {
            LOG.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
    }


    @POST
    @Path("/remUserAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUserAccount(@Context HttpHeaders headers, RemUserData data){

        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String userRole = user.getString("user_role");

        try {

            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.other);
            Entity other = datastore.get(otherKey);

            if(other == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String otherRole = other.getString("user_role");

            if (!PermissionChecker.canRemUser(userRole, otherRole) || !PermissionChecker.isActive(user)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            datastore.delete(otherKey);
            return Response.ok().build();

        }
        catch(DatastoreException e){
            LOG.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
    }


    @POST
    @Path("/AccountAttrib")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAttribs(@Context HttpHeaders headers, ChangeAttribsData data) {

        // Get the Authorization header
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(tokenID);
        Entity token = datastore.get(tokenKey);
        String requesterUsername = token.getString("username");
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(requesterUsername);

        try {
            Key theKey;
            Entity theUser;

            if (data.other != null && !data.other.isEmpty()) {
                // Editing another user
                Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.other);
                theUser = datastore.get(otherKey);

                if (theUser == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                theKey = otherKey;
            } else {
                // Editing self
                theUser = datastore.get(userKey);
                theKey = userKey;
            }

            // Build the updated entity with preserved and modified values
            Entity updatedUser = Entity.newBuilder(theKey)
                    .set("user_userName", theUser.getString("user_userName"))
                    .set("user_name", getOrDefault(data.name, theUser, "user_name"))
                    .set("user_pwd", theUser.getString("user_pwd"))
                    .set("user_email", getOrDefault(data.email, theUser, "user_email"))
                    .set("user_profile", getOrDefault(data.profile, theUser, "user_profile"))
                    .set("user_role", getOrDefault(data.role, theUser, "user_role"))
                    .set("user_account_state", getOrDefault(data.state, theUser, "user_account_state"))
                    .set("address", getOrDefault(data.address, theUser, "address"))
                    .set("cc", getOrDefault(data.cc, theUser, "cc"))
                    .set("NIF", getOrDefault(data.NIF, theUser, "NIF"))
                    .set("employer", getOrDefault(data.employer, theUser, "employer"))
                    .set("function", getOrDefault(data.function, theUser, "function"))
                    .build();

            datastore.put(updatedUser);
            return Response.ok().build();
        } catch (DatastoreException e) {
            LOG.log(Level.SEVERE, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
    }

    private String getOrDefault(String input, Entity source, String attrName) {
        return (input != null && !input.equals(EMPTY)) ? input : source.getString(attrName);
    }


    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(@Context HttpHeaders headers, ChangePwdData data){

        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        if(!data.checkConfirmation()){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {

            Entity updatedUser = Entity.newBuilder(user.getKey())
                    .set("user_userName", user.getString("user_userName"))
                    .set("user_name", user.getString("user_name"))
                    .set("user_pwd", DigestUtils.sha512Hex(data.newPwd)) // Hash the new password
                    .set("user_email", user.getString("user_email"))
                    .set("user_profile", user.getString("user_profile"))
                    .set("user_role", user.getString("user_role"))
                    .set("user_account_state", user.getString("user_account_state"))
                    .set("address", user.getString("address"))
                    .set("cc", user.getString("cc"))
                    .set("NIF", user.getString("NIF"))
                    .set("employer", user.getString("employer"))
                    .set("function", user.getString("function"))
                    .build();

            datastore.put(updatedUser);
            return Response.ok().build();
        }catch(DatastoreException e){
            LOG.log(Level.SEVERE, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
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

    @GET
    @Path("/getRole")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRole(@Context HttpHeaders headers){

        //gets the authorization header
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        // Return role as JSON
        String role = user.getString("user_role");
        return Response.ok("{\"role\": \"" + role + "\"}").build();
    }

    @POST
    @Path("/listUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllUsers(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity requester = AuthTokenValidator.validateToken(tokenID);

        if (requester == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        if(!PermissionChecker.isActive(requester))
            return Response.status(Response.Status.FORBIDDEN).build();

        String userRole = requester.getString("user_role");
        List<Entity> entities = new ArrayList<>();

        switch (userRole) {
            case "ADMIN" -> {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .build();
                datastore.run(query).forEachRemaining(entities::add);
            }
            case "BACKOFFICE" -> {
                // Fetch ENDUSERs
                Query<Entity> queryEnduser = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "ENDUSER"))
                        .build();
                datastore.run(queryEnduser).forEachRemaining(entities::add);

                // Fetch PARTNERs
                Query<Entity> queryPartner = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "PARTNER"))
                        .build();
                datastore.run(queryPartner).forEachRemaining(entities::add);
            }
            default -> {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(StructuredQuery.CompositeFilter.and(
                                StructuredQuery.PropertyFilter.eq("user_role", "ENDUSER"),
                                StructuredQuery.PropertyFilter.eq("user_account_state", "ACTIVATED"),
                                StructuredQuery.PropertyFilter.eq("user_profile", "PUBLIC")
                        ))
                        .build();
                datastore.run(query).forEachRemaining(entities::add);
            }
        }

        List<Map<String, Object>> userList = new ArrayList<>();

        for (Entity entity : entities) {
            Map<String, Object> userData = new HashMap<>();

            if (userRole.equals("ENDUSER")) {
                userData.put("user_userName", entity.getString("user_userName"));
                userData.put("user_email", entity.getString("user_email"));
                userData.put("user_name", entity.getString("user_name"));
            } else {
                for (String name : entity.getNames()) {
                    if (!name.equals("user_pwd")) {
                        userData.put(name, entity.getValue(name).get().toString());
                    }
                }
            }
            userList.add(userData);
        }

        return Response.ok(userList).build();
    }




    @POST
    @Path("/workSheet")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createWorkSheet(@Context HttpHeaders headers,WorkSheetData data){

        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return Response.status(Response.Status.UNAUTHORIZED).build();

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null)
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();

        if(!PermissionChecker.canCreateWorkSheet(user.getString("user_role")))
            return Response.status(Response.Status.FORBIDDEN).build();

        if(!data.isValid())
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {

            Key sheetKey = datastore.newKeyFactory().setKind("WorkSheet").newKey(data.reference);

            Entity sheet = Entity.newBuilder(sheetKey)
                    .set("sheet_reference", data.reference)
                    .set("sheet_description", data.description)
                    .set("sheet_target", data.target)
                    .set("sheet_adj", data.adj)
                    .set("sheet_adj_date", EMPTY)
                    .set("sheet_start_date", EMPTY)
                    .set("sheet_end_date", EMPTY)
                    .set("sheet_account", EMPTY)
                    .set("sheet_adj_account", EMPTY)
                    .set("sheet_comp_nif", EMPTY)
                    .set("sheet_state", EMPTY)
                    .set("sheet_observations", EMPTY)
                    .build();

            datastore.add(sheet);
            return  Response.ok().build();

        }catch(DatastoreException e){
            LOG.log(Level.SEVERE, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
    }


}
