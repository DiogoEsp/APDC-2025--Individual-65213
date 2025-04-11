package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttribsData {

    public String username;
    public String name;
    public String email;
    public String address;
    public String cc;
    public String NIF;
    public String employer;
    public String function;

    public ChangeAttribsData(){

    }

    public ChangeAttribsData(String username, String name, String email, String address,
                             String cc,String NIF, String employer, String function){
        this.username = username;
        this.address = address;
        this.cc = cc;
        this.NIF = NIF;
        this.function = function;
        this.employer = employer;
        this.email = email;
        this.name = name;
    }
}
