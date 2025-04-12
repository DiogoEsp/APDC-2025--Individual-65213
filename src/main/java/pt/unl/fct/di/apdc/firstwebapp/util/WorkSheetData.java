package pt.unl.fct.di.apdc.firstwebapp.util;

public class WorkSheetData {

    public String description;
    public String reference;
    public String target;
    public String adj;
    public WorkSheetData(){}

    public WorkSheetData(String description, String reference, String target,String adj) {
        this.description = description;
        this.reference = reference;
        this.target = target;
        this.adj = adj;
    }

    public boolean isValid(){
        return (description != null && reference != null && target != null && adj != null);
    }

}
