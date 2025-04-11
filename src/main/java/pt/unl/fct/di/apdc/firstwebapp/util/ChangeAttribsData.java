package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttribsData {

    public String other;
    public String name;
    public String email;
    public String profile;
    public String role;
    public String state;
    public String address;
    public String cc;
    public String NIF;
    public String employer;
    public String function;

    public ChangeAttribsData(){

    }

    // Constructor
    public ChangeAttribsData(String other, String name, String email, String profile, String role, String state,
                             String address, String cc, String NIF, String employer, String function) {
        this.other = other;
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.role = role;
        this.state = state;
        this.address = address;
        this.cc = cc;
        this.NIF = NIF;
        this.employer = employer;
        this.function = function;
    }
}
