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

    private static final String EMPTY = "";
    private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());

    private final static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    public ChangesResource() {} //nothing to be done here @GET


    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(@Context HttpHeaders headers, ChangeRoleData data) {
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

        if (data.role == null || !data.validRole()) {
            LOG.info("Invalid role.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String userRole = user.getString("user_role");

        if (!PermissionChecker.canChangeRole(userRole, data)) {
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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String tokenID = authHeader.substring("Bearer ".length());
        Entity user = AuthTokenValidator.validateToken(tokenID);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }


        if(data.state == null || !data.validState()){
            LOG.info("Null or not valid state");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String userRole = user.getString("user_role");

        if (!PermissionChecker.canChangeStatus(userRole, data)) {
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

            datastore.put(updatedUser);

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
                LOG.info("User does not exist.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String otherRole = other.getString("user_role");

            if (!PermissionChecker.canRemUser(userRole, otherRole)) {
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
    public Response changeAttribs(@Context HttpHeaders headers, ChangeAttribsData data){

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

        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(tokenID);
        Entity token = datastore.get(tokenKey);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("username"));

        Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.other);

        if(data.other != null && datastore.get(otherKey) == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try{
            Key theKey;


            if(data.other == null)
                theKey = userKey;
            else{
                theKey = otherKey;
            }
            Entity theUser = datastore.get(theKey);

            Entity updatedUser = Entity.newBuilder(theKey)
                    .set("user_userName", theUser.getString("user_userName"))
                    .set("user_name", (data.name != null ? data.name : theUser.getString("user_name")))
                    .set("user_email", (data.email != null ? data.email : theUser.getString("user_email")))
                    .set("user_profile", (data.profile != null ? data.profile : theUser.getString("user_profile")))
                    .set("user_role", (data.role != null ? data.role.toUpperCase() : theUser.getString("user_role")))
                    .set("user_account_state", (data.state != null ? data.state.toUpperCase() : theUser.getString("user_account_state")))
                    .set("address", (data.address != null ? data.address : theUser.getString("address")))
                    .set("cc", (data.cc != null ? data.cc : theUser.getString("cc")))
                    .set("NIF", (data.NIF != null ? data.NIF : theUser.getString("NIF")))
                    .set("employer", (data.employer != null ? data.employer : theUser.getString("employer")))
                    .set("function", (data.function != null ? data.function : theUser.getString("function")))
                    .build();


            datastore.put(updatedUser);
            return Response.ok().build();
        }
        catch(DatastoreException e){
            LOG.log(Level.SEVERE, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getReason()).build();
        }
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
    public Response listAllUsers(@Context HttpHeaders headers){

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
        Query<Entity> query = switch (userRole) {
            case "BACKOFFICE" -> Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(StructuredQuery.PropertyFilter.eq("role", "ENDUSER"))
                    .build();
            case "ADMIN" -> Query.newEntityQueryBuilder()
                    .setKind("User")
                    .build();
            default -> Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(StructuredQuery.CompositeFilter.and(
                            StructuredQuery.PropertyFilter.eq("role", "ENDUSER"),
                            StructuredQuery.PropertyFilter.eq("accountState", "ACTIVE"),
                            StructuredQuery.PropertyFilter.eq("profileVisibility", "public")
                    ))
                    .build();
        };

        QueryResults<Entity> results = datastore.run(query);

        List<Map<String, Object>> userList = new ArrayList<>();
        while (results.hasNext()) {
            Entity entity = results.next();
            Map<String, Object> userData = new HashMap<>();

            // Return attributes based on the role
            userData.put("username", entity.contains("username") ? entity.getString("username") : "NOT DEFINED");
            userData.put("email", entity.contains("email") ? entity.getString("email") : "NOT DEFINED");
            userData.put("name", entity.contains("name") ? entity.getString("name") : "NOT DEFINED");

            if (!userRole.equals("ENDUSER")) {
                userData.put("role", entity.contains("role") ? entity.getString("role") : "NOT DEFINED");
                userData.put("accountState", entity.contains("accountState") ? entity.getString("accountState") : "NOT DEFINED");
                userData.put("profileVisibility", entity.contains("profileVisibility") ? entity.getString("profileVisibility") : "NOT DEFINED");
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


    private String getOrDefault(Entity user, String property) {
        if(user.getString(property) == null){
            return "NOT DEFINED";
        }
        else{
            return user.getString(property);
        }
    }

}
