package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountStateData {
    private static final String ACTIVATED = "ACTIVATED";
    private static final String SUSPENDED = "SUSPENDED";
    private static final String DEACTIVATED = "DEACTIVATED";

    public String username;
    public String state;

    public ChangeAccountStateData(){

    }

    public ChangeAccountStateData(String username, String state){
        this.state = state;
        this.username = username;
    }

    public boolean validState(){
        return state.equalsIgnoreCase(ACTIVATED) || state.equalsIgnoreCase(DEACTIVATED) || state.equalsIgnoreCase(SUSPENDED);
    }
}
