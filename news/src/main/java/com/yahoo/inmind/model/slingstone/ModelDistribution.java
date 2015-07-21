package com.yahoo.inmind.model.slingstone;

import java.util.ArrayList;

/**
 * Created by oscarr on 7/15/15.
 */
public class ModelDistribution {

    private Double model1 = 0.0;
    private Double model2 = 0.0;
    private Double model3 = 0.0;
    private Double model4 = 0.0;
    private Double model5 = 0.0;
    private transient ArrayList<String[]> models;

    public ModelDistribution() {
        this.models = new ArrayList<>();
    }

    public Double getModel1() {
        return model1;
    }

    public void setModel1(Double model1) {
        this.model1 = model1;
    }

    public Double getModel2() {
        return model2;
    }

    public void setModel2(Double model2) {
        this.model2 = model2;
    }

    public Double getModel3() {
        return model3;
    }

    public void setModel3(Double model3) {
        this.model3 = model3;
    }

    public Double getModel4() {
        return model4;
    }

    public void setModel4(Double model4) {
        this.model4 = model4;
    }

    public Double getModel5() {
        return model5;
    }

    public void setModel5(Double model5) {
        this.model5 = model5;
    }

    public ArrayList<String[]> getModels(){
        models = new ArrayList<>();
        models.add( new String[]{"Model1", model1.toString(), "Apparel,Beauty,Hobbies,Food,Home,Health" } );
        models.add( new String[]{"Model2", model2.toString(), "Finance,Nature,RealEstate,Travel" } );
        models.add( new String[]{"Model3", model3.toString(), "Society,Sports" } );
        models.add( new String[]{"Model4", model4.toString(), "Business,Education,Science,Technology,Transportation" } );
        models.add( new String[]{"Model5", model5.toString(), "Entertainment,Politics" } );
        return models;
    }

    public static String formatModels(String stringModelDistributions) {
        return stringModelDistributions
                .replace( "Apparel,Beauty,Hobbies,Food,Home,Health", "model1" )
                .replace( "Finance,Nature,RealEstate,Travel", "model2" )
                .replace( "Society,Sports", "model3" )
                .replace( "Business,Education,Science,Technology,Transportation", "model4" )
                .replace( "Entertainment,Politics", "model5" );
    }
}
