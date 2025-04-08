package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

import javax.print.attribute.standard.Media;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/changes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChagesResource {


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

    public ChagesResource() {} //nothing to be done here @GET

    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data) {

        if(data.role == null || !data.validRole()){
            LOG.info("Null or not valid role");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.user);

            Entity user = datastore.get(userKey);

            if (!(user.getString("user_role").equals(ADMIN) ||
                    (user.getString("user_role").equals(BACKOFFICE) && (data.role.equals(PARTNER) || data.role.equals(ENDUSER))))) {
                LOG.info("Info user does not have permission for that.");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(data.otherUser);

            Entity other = datastore.get(otherKey);


            Entity updatedUser = Entity.newBuilder(userKey)
                    .set("user_userName", other.getString("user_userName"))
                    .set("user_name", other.getString("user_name"))
                    .set("user_pwd", DigestUtils.sha512Hex(other.getString("user_pwd")))
                    .set("user_email", other.getString("user_email"))
                    .set("user_profile", other.getString("user_profile"))
                    .set("user_role", data.role.toUpperCase())
                    .set("user_account_state", DEACTIVATED)
                    .set("address", EMPTY)
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
    @Path("/accountState/{otherUser}/{state}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountState(@PathParam("username") String username, @PathParam("state") String state, @PathParam("otherUser") String otherUser){


        if(state == null || !validState(state)){
            LOG.info("Null or not valid state");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);

            Entity user = datastore.get(userKey);

            if (!(user.getString("user_role").equals(ADMIN) ||
                    (user.getString("user_role").equals(BACKOFFICE) && (state.equals(ACTIVATED) || state.equals(DEACTIVATED))))) {
                LOG.info("Info user does not have permission for that.");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Key otherKey = datastore.newKeyFactory().setKind("User").newKey(otherUser);

            Entity other = datastore.get(otherKey);


            Entity updatedUser = Entity.newBuilder(userKey)
                    .set("user_userName", other.getString("user_userName"))
                    .set("user_name", other.getString("user_name"))
                    .set("user_pwd", DigestUtils.sha512Hex(other.getString("user_pwd")))
                    .set("user_email", other.getString("user_email"))
                    .set("user_profile", other.getString("user_profile"))
                    .set("user_role", other.getString("user_role"))
                    .set("user_account_state", state.toUpperCase())
                    .set("address", EMPTY)
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
    @Path("/remUserAccount/{userName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUserAccount(@PathParam(("userName"))String userName){

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(userName);

        Entity user = datastore.get(userKey);

        return Response.ok().build();
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
    public Response logOut(){
        return Response.ok().build();
    }
    private boolean validState(String state){
        String s = state.toUpperCase();
        return (s.equals(SUSPENDED) || s.equals(ACTIVATED) || s.equals(DEACTIVATED));
    }

    private boolean validRole(String role){
        String s = role.toUpperCase();
        return (s.equals(ADMIN) || s.equals(PARTNER) || s.equals(BACKOFFICE) || s.equals(ENDUSER));
    }
}
