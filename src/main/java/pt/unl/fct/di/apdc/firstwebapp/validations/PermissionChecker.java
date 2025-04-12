package pt.unl.fct.di.apdc.firstwebapp.validations;

import com.google.cloud.datastore.Entity;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAccountStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.RemUserData;

public class PermissionChecker {


    private static final String PUBLIC = "PUBLIC";
    private static final String ACTIVATED = "ACTIVATED";
    private static final String DEACTIVATED = "DEACTIVATED";
    private static final String ENDUSER = "ENDUSER";
    private static final String BACKOFFICE = "BACKOFFICE";
    private static final String ADMIN = "ADMIN";
    private static final String PARTNER = "PARTNER";

    public static boolean canChangeRole(String userRole, ChangeRoleData data){
        return !(userRole.equals(ADMIN) ||
                (userRole.equals(BACKOFFICE) && (data.role.equalsIgnoreCase(PARTNER) || data.role.equalsIgnoreCase(ENDUSER))));
    }

    public static boolean canChangeStatus(String userRole, ChangeAccountStateData data){
        return (userRole.equals(ADMIN) ||
                (userRole.equals(BACKOFFICE) && (data.state.equalsIgnoreCase(ACTIVATED) || data.state.equalsIgnoreCase(DEACTIVATED))));
    }

    public static boolean canRemUser(String userRole, String otherRole){
        return (userRole.equals(ADMIN) ||
                userRole.equals(BACKOFFICE) &&
                        (otherRole.equals(PARTNER) ||
                                otherRole.equals(ENDUSER)));
    }

    public static boolean isActive(Entity user){
        String profile = user.getString("user_profile");
        return profile.equals(PUBLIC);
    }

    public static boolean canCreateWorkSheet(String userRole){
        return userRole.equals(PARTNER);
    }



}
