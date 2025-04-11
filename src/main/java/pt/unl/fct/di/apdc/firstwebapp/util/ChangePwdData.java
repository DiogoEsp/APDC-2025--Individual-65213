package pt.unl.fct.di.apdc.firstwebapp.util;


public class ChangePwdData {
    public String oldPwd;

    public String confirmation;

    public String newPwd;


    public ChangePwdData(){

    }

    public ChangePwdData(String oldPwd, String newPwd, String confirmation){
        this.oldPwd = oldPwd;
        this.newPwd = newPwd;
        this.confirmation = confirmation;
    }

    public boolean checkConfirmation(){
        return newPwd.equals(confirmation);
    }
}
