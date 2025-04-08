package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/register")
public class RegisterResource {


	private static final String DEACTIVATED = "DEACTIVATED";
	private static final String ENDUSER = "ENDUSER";

	private static final String EMPTY = "";

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();


	public RegisterResource() {}	// Default constructor, nothing to do
	

	@POST
	@Path("/registerUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUserV4(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);


		if(!data.validRegistration())
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		try{
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

			Entity user = Entity.newBuilder(userKey)
					.set("user_userName", data.username)
					.set("user_name", data.name)
					.set("user_pwd", DigestUtils.sha512Hex((data.password)))
					.set("user_email", data.email)
					.set("user_profile", data.profile.toUpperCase())
					.set("user_role", ENDUSER)
					.set("user_account_state", DEACTIVATED)
					.set("address", EMPTY)
					.set("cc", EMPTY)
					.set("NIF", EMPTY)
					.set("employer", EMPTY)
					.set("function", EMPTY).build();

			datastore.add(user);
			LOG.info("User registered " + data.username);

			return Response.ok().build();
		}
		catch(DatastoreException e){
			LOG.log(Level.ALL, e.toString());
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
		}
	}
}
