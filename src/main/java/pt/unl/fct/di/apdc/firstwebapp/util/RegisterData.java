package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	private static final String PUBLIC = "PUBLIC";
	private static final String PRIVATE = "PRIVATE";
	private static final String ACTIVE = "ACTIVE";
	private static final String SUSPENDED = "SUSPENDED";
	private static final String DEACTIVATED = "DEACTIVATED";
	private static final String ENDUSER = "ENDUSER";
	private static final String BACKOFFICE = "BACKOFFICE";
	private static final String ADMIN = "ADMIN";
	private static final String PARTNER = "PARTNER";

	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	public String profile;
	public String cc;
	public String nif;
	public String employer;
	public String role;
	public String address;
	public String function;
	public String accountState;

	
	
	public RegisterData() {
		
	}
	
	public RegisterData(String username, String password, String confirmation, String email, String name, String profile) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		this.name = name;
		this.profile = profile;
		this.cc = null;
		this.function = null;
		this.employer = null;
		this.nif = null;
		this.role = ENDUSER;
		this.address = null;
		this.accountState = DEACTIVATED;
	}
	
	private boolean nonEmptyOrBlankField(String field) {
		return field != null && !field.isBlank();
	}
	
	public boolean validRegistration() {
		return nonEmptyOrBlankField(username) &&
			   nonEmptyOrBlankField(password) &&
			   nonEmptyOrBlankField(email) &&
			   nonEmptyOrBlankField(name) &&
			   email.contains("@") &&
			   password.equals(confirmation) &&
				(profile.equals(PUBLIC) || profile.equals(PRIVATE));

	}
}
