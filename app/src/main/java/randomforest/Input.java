package randomforest;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Input {
    public static String timeCompute = "";

    public static String main(
            Context context,
            String type,
            Integer trees
    ){
        System.out.println("Random-Forest with Categorical support");
        System.out.println("Now Running");


        int categ=0;
        String traindata,testdata;
        ArrayList<ArrayList<String>> Test = new ArrayList<>();

        String traincsv = "pima/pima_train_40nr.csv";
        String testcsv = "testpim_3_nrandom_nohead.csv";
        String testinput = "amytextfile.txt";


        if(categ>0){
//            file:///
            traindata=traincsv;
            testdata=testcsv;
        }else if(categ<0){
            traindata=traincsv;
            testdata=testcsv;
        }else{
            traindata=traincsv;
            testdata=testcsv;
        }

        DescribeTreesCateg DT = new DescribeTreesCateg(traindata);
        ArrayList<ArrayList<String>> Train = DT.CreateInputCateg(traindata,context);
        if (type == "test"){
            Test = DT.CreateInputCateg(testdata,context);
        }else if(type == "input"){
            Test = DT.InputText(testinput,context);
        }

//        ArrayList<ArrayList<String>> Test = testInput;

        System.out.println("Input generated"+ Train);
        System.out.println("Input generated"+ Test);
        /*
         * For class-labels
         */
        HashMap<String, Integer> Classes = new HashMap<String, Integer>();
        for(ArrayList<String> dp : Train){
            String clas = dp.get(dp.size()-1);
            if(Classes.containsKey(clas))
                Classes.put(clas, Classes.get(clas)+1);
            else
                Classes.put(clas, 1);
        }

        String result = "";
        int numTrees=trees;
        int M=Train.get(0).size()-1;
        int Ms = (int)Math.round(Math.log(M)/Math.log(2)+1);
        int C = Classes.size();

        RandomForestCateg RFC = new RandomForestCateg(
                numTrees,
                M,
                Ms,
                C,
                Train,
                Test
        );
        result = RFC.Start();
        timeCompute = RFC.timeCompute;

        return result;
    }

    public static String readFile(Context context)
    {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("amytextfile.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}