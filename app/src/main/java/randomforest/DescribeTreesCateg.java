package randomforest;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DescribeTreesCateg {

    BufferedReader BR = null;
    String path;
    public DescribeTreesCateg(String path){
        this.path=path;
    }
    public ArrayList<ArrayList<String>> CreateInputCateg(String path, Context context){
        ArrayList<ArrayList<String>> DataInput = new ArrayList<ArrayList<String>>();

        try {

            String sCurrentLine;

            BR = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
            while ((sCurrentLine = BR.readLine()) != null) {
                ArrayList<Integer> Sp=new ArrayList<Integer>();int i;
                if(sCurrentLine!=null){
                    if(sCurrentLine.indexOf(",")>=0){
                        //has comma

                        sCurrentLine=","+sCurrentLine+",";
                        char[] c =sCurrentLine.toCharArray();
                        for(i=0;i<sCurrentLine.length();i++){
                            if(c[i]==',')
                                Sp.add(i);
                        }ArrayList<String> DataPoint=new ArrayList<String>();
                        for(i=0;i<Sp.size()-1;i++){
                            DataPoint.add(sCurrentLine.substring(Sp.get(i)+1, Sp.get(i+1)).trim());
                        }DataInput.add(DataPoint);
                    }
                    else if(sCurrentLine.indexOf(" ")>=0){
                        //has spaces
                        sCurrentLine=" "+sCurrentLine+" ";
                        for(i=0;i<sCurrentLine.length();i++){
                            if(Character.isWhitespace(sCurrentLine.charAt(i)))
                                Sp.add(i);
                        }ArrayList<String> DataPoint=new ArrayList<String>();
                        for(i=0;i<Sp.size()-1;i++){
                            DataPoint.add(sCurrentLine.substring(Sp.get(i), Sp.get(i+1)).trim());
                        }DataInput.add(DataPoint);
                    }
                }
            }System.out.println("Input generated");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (BR != null)BR.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return DataInput;
    }

    public ArrayList<ArrayList<String>> InputText(String path,Context context){

        ArrayList<ArrayList<String>> DataInput = new ArrayList<ArrayList<String>>();

        try {

            String sCurrentLine;

            InputStream inputStream = context.openFileInput(path);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BR = new BufferedReader(inputStreamReader);
            while ((sCurrentLine = BR.readLine()) != null) {
                ArrayList<Integer> Sp=new ArrayList<Integer>();int i;
                if(sCurrentLine!=null){
                    if(sCurrentLine.indexOf(",")>=0){
                        //has comma

                        sCurrentLine=","+sCurrentLine+",";
                        char[] c =sCurrentLine.toCharArray();
                        for(i=0;i<sCurrentLine.length();i++){
                            if(c[i]==',')
                                Sp.add(i);
                        }ArrayList<String> DataPoint=new ArrayList<String>();
                        for(i=0;i<Sp.size()-1;i++){
                            DataPoint.add(sCurrentLine.substring(Sp.get(i)+1, Sp.get(i+1)).trim());
                        }DataInput.add(DataPoint);//System.out.println(DataPoint);
                    }
                    else if(sCurrentLine.indexOf(" ")>=0){
                        //has spaces
                        sCurrentLine=" "+sCurrentLine+" ";
                        for(i=0;i<sCurrentLine.length();i++){
                            if(Character.isWhitespace(sCurrentLine.charAt(i)))
                                Sp.add(i);
                        }ArrayList<String> DataPoint=new ArrayList<String>();
                        for(i=0;i<Sp.size()-1;i++){
                            DataPoint.add(sCurrentLine.substring(Sp.get(i), Sp.get(i+1)).trim());
                        }DataInput.add(DataPoint);//System.out.println(DataPoint);
                    }
                }
            }System.out.println("Input generated");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (BR != null)BR.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return DataInput;
    }
}