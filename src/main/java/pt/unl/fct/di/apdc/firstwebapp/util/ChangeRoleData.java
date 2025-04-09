package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeRoleData {

    private static final String ENDUSER = "ENDUSER";
    private static final String BACKOFFICE = "BACKOFFICE";
    private static final String ADMIN = "ADMIN";
    private static final String PARTNER = "PARTNER";

    public String otherUser;

    public String role;

    public ChangeRoleData(){}

    public ChangeRoleData(String otherUser , String role){
        this.role = role;
        this.otherUser = otherUser;
    }

    public boolean validRole(){
        String s = role.toUpperCase();
        return (s.equals(ADMIN) || s.equals(PARTNER) || s.equals(BACKOFFICE) || s.equals(ENDUSER));
    }
}
