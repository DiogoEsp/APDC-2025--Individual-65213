package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

@Path("/change")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChagesResource {
    private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());
    private final Gson g = new Gson();

    private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    public ChagesResource() {} //nothing to be done here @GET

    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(){
        return Response.ok().build();
    }
    @POST
    @Path("/accountState")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountState(){
        return Response.ok().build();
    }
    @POST
    @Path("/remUserAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUserAccount(){
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
}
