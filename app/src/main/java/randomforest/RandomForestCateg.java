package randomforest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class RandomForestCateg {

    private static final int NUM_THREADS=Runtime.getRuntime().availableProcessors();
    //private static final int NUM_THREADS=1;

    public static int C;

    public static int M;
    /** Of the M total attributes, the random forest computation requires a subset of them
     * to be used and picked via random selection. "Ms" is the number of attributes in this
     * subset. The formula used to generate Ms was recommended on Breiman's website.
     */
    public static int Ms;//recommended by Breiman: =(int)Math.round(Math.log(M)/Math.log(2)+1);
    /** the collection of the forest's decision trees */
    private ArrayList<DTreecateg> trees;
    /** the collection of the forest's decision trees */
    private ArrayList<DTreecateg2> trees2;
    /** the starting time when timing random forest creation */
    private long time_o;
    /** the number of trees in this random tree */
    private int numTrees;
    /** For progress bar display for the creation of this random forest, this is the amount to update by when one tree is completed */
    private double update;
    /** For progress bar display for the creation of this random forest, this records the total progress */
    private double progress;
    /** this is an array whose indices represent the forest-wide importance for that given attribute */
    private int[] importances;
    /** This maps from a data record to an array that records the classifications by the trees where it was a "left out" record (the indices are the class and the values are the counts) */
    private HashMap<int[],int[]> estimateOOB;
    /** the total forest-wide error */
    private double error;
    /** the thread pool that controls the generation of the decision trees */
    private ExecutorService treePool;
    /** the original training data matrix that will be used to generate the random forest classifier */
    private ArrayList<ArrayList<String>> data;
    /** the data on which produced random forest will be tested*/
    private ArrayList<ArrayList<String>> testdata;
    /** This holds all of the predictions of trees in a Forest */
    private ArrayList<ArrayList<String>> Prediction;

    public static String timeCompute = "";
    /**
     * This hold the genres of attributes in the forest
     *
     * 1 if categ
     * 0 if real
     */
    public ArrayList<Integer> TrainAttributes;
    public ArrayList<Integer> TestAttributes;


    public RandomForestCateg(
            int numTrees,
            int M,
            int Ms,
            int C,
            ArrayList<ArrayList<String>> train,
            ArrayList<ArrayList<String>> test
    ) {
        // TODO Auto-generated constructor stub
        StartTimer();
        this.numTrees=numTrees;
        this.data=train;
        this.testdata=test;
        this.M=M;
        this.Ms=Ms;
        this.C=C;
        this.TrainAttributes=GetAttributes(train);
        this.TestAttributes=GetAttributes(test);
        trees = new ArrayList<DTreecateg>(numTrees);
        trees2 = new ArrayList<DTreecateg2>(numTrees);
        update=100/((double)numTrees);
        progress=0;
        System.out.println("creating "+numTrees+" trees in a random Forest. . . ");
        System.out.println("total data size is "+train.size());
        System.out.println("number of attributes "+M);
        System.out.println("number of selected attributes "+Ms);

        estimateOOB=new HashMap<int[],int[]>(data.size());
        Prediction = new ArrayList<ArrayList<String>>();
    }


    public String Start() {
        // TODO Auto-generated method stub
        System.out.println("Number of threads started : "+NUM_THREADS);
        System.out.println("Starting trees");
        String result = " ";
        treePool=Executors.newFixedThreadPool(NUM_THREADS);
        for (int t=0;t<numTrees;t++){
            treePool.execute(new CreateTree(data,this,t+1));
        }treePool.shutdown();
        try {
            treePool.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS); //effectively infinity
        } catch (InterruptedException ignored){
            System.out.println("interrupted exception in Random Forests");
        }
        if(data.get(0).size()>testdata.get(0).size()){
//            TestForestNoLabel2(trees2, data, testdata);
            System.out.println("Starting nolabel");
            TestForestNoLabel(trees,data,testdata);
        }
        else if(data.get(0).size()==testdata.get(0).size()){
            result = TestForest2(trees2, data, testdata);
            //TestForest(trees,data,testdata);
        }

        else
            System.out.println("Cannot test this data");

        System.out.print("Done in "+TimeElapsed(time_o));
        
        return result;
    }

    private void TestForestNoLabel(
            ArrayList<DTreecateg> trees2,
            ArrayList<ArrayList<String>> data2,
            ArrayList<ArrayList<String>> testdata2
    ) {
        // TODO Auto-generated method stub
        ArrayList<String> TestResult = new ArrayList<String>();
        System.out.println("Predicting Labels now");
        for(ArrayList<String> DP:testdata2){
            ArrayList<String> Predict = new ArrayList<String>();
            for(DTreecateg DT:trees2){
                Predict.add(DT.Evaluate(DP, testdata2));
            }
            TestResult.add(ModeofList(Predict));
        }
    }

    public void TestForest(ArrayList<DTreecateg> trees,ArrayList<ArrayList<String>> train,ArrayList<ArrayList<String>> test){
        int correctness=0;
        ArrayList<String> ActualValues = new ArrayList<String>();

        for(ArrayList<String> s:test){
            ActualValues.add(s.get(s.size()-1));
        }int treee=1;
        System.out.println("Testing forest now ");

        for(DTreecateg DTC : trees){
            DTC.CalculateClasses(train, test, treee);treee++;
            if(DTC.predictions!=null)
                Prediction.add(DTC.predictions);
        }
        for(int i = 0;i<test.size();i++){
            ArrayList<String> Val = new ArrayList<String>();
            for(int j=0;j<trees.size();j++){
                Val.add(Prediction.get(j).get(i));
            }
            String pred = ModeofList(Val);
            if(pred.equalsIgnoreCase(ActualValues.get(i))){
                correctness = correctness +1;
            }
        }
        System.out.println("The Result of Predictions :-");
        System.out.println("Total Cases : "+test.size());
        System.out.println("Total CorrectPredicitions  : "+correctness);
        System.out.println("Forest Accuracy :"+(correctness*100/test.size())+"%");
    }


    private void TestForestNoLabel2(ArrayList<DTreecateg2> trees22,ArrayList<ArrayList<String>> data2,ArrayList<ArrayList<String>> testdata2) {
        // TODO Auto-generated method stub
        ArrayList<String> TestResult = new ArrayList<String>();
        System.out.println("Predicting Labels now");
        for(ArrayList<String> DP:testdata2){
            ArrayList<String> Predict = new ArrayList<String>();
            for(DTreecateg2 DT:trees22){
                Predict.add(DT.Evaluate(DP, testdata2));
            }
            TestResult.add(ModeofList(Predict));
        }
    }

    public String TestForest2(
            ArrayList<DTreecateg2> trees,
            ArrayList<ArrayList<String>> train,
            ArrayList<ArrayList<String>> test
    ){
        int correctness=0;
        ArrayList<String> ActualValues = new ArrayList<String>();
        ArrayList<String> Val = new ArrayList<String>();

        for(ArrayList<String> s:test){
            ActualValues.add(s.get(s.size()-1));
        }int treee=1;
        System.out.println("Testing forest now ");

        for(DTreecateg2 DTC : trees){
            DTC.CalculateClasses(train, test, treee);treee++;
            if(DTC.predictions!=null)
                Prediction.add(DTC.predictions);
        }
        for(int i = 0;i<test.size();i++){
            for(int j=0;j<trees.size();j++){
                Val.add(Prediction.get(j).get(i));
                System.out.println("The Result of Predictions :" + Prediction.get(j).get(i));
            }
            String pred = ModeofList(Val);
            if(pred.equalsIgnoreCase(ActualValues.get(i))){
                correctness = correctness +1;
            }
        }
//        System.out.println("Result of Predictions :"+ getMostly(Val));
//        System.out.println("Total Cases : "+test.size());
//        System.out.println("Total CorrectPredicitions  : "+correctness);
//        System.out.println("Forest Accuracy :"+(correctness*100/test.size())+"%");

        timeCompute = TimeElapsed(time_o);

        String result = "hasil "+ getMostly(Val) +
                "\njumlah kasus " + test.size() +
                "\ntotal benar " + correctness +
                "\nakurasi "+(correctness*100/test.size())+"%" +
                "\nwaktu komputasi \n" + TimeElapsed(time_o)
                ;
        return result;
    }

    public String ModeofList(ArrayList<String> predictions) {
        String MaxValue = null; int MaxCount = 0;
        for(int i=0;i<predictions.size();i++){
            int count=0;
            for(int j=0;j<predictions.size();j++){
                if(predictions.get(j).trim().equalsIgnoreCase(predictions.get(i).trim()))
                    count++;
                if(count>MaxCount){
                    MaxValue=predictions.get(i);
                    MaxCount=count;
                }
            }
        }return MaxValue;
    }

    private class CreateTree implements Runnable{

        private ArrayList<ArrayList<String>> data;
        private RandomForestCateg forest;

        private int treenum;
        public CreateTree(ArrayList<ArrayList<String>> data,RandomForestCateg forest,int num){
            this.data=data;
            this.forest=forest;
            this.treenum=num;
        }

        public void run() {
            //trees.add(new DTreeCateg(data,forest,treenum));
            trees2.add(new DTreecateg2(data, forest, treenum));
            progress+=update;
        }
    }


    private void StartTimer(){
        time_o=System.currentTimeMillis();
    }
    private static String TimeElapsed(long timeinms){
        double s=(double)(System.currentTimeMillis()-timeinms)/1000;
        int h=(int)Math.floor(s/((double)3600));
        s-=(h*3600);
        int m=(int)Math.floor(s/((double)60));
        s-=(m*60);
        return ""+h+"hr "+m+"m "+s+"sec";
    }


    private boolean isAlphaNumeric(String s){
        char c[]=s.toCharArray();boolean hasalpha=false;
        for(int j=0;j<c.length;j++){
            hasalpha = Character.isLetter(c[j]);
            if(hasalpha)break;
        }return hasalpha;
    }
    /**
     * Of the attributes selected this function will record the genre of attributes
     */
    private ArrayList<Integer> GetAttributes(List<ArrayList<String>> data){
        ArrayList<Integer> Attributes = new ArrayList<Integer>();int iter = 0;
        ArrayList<String> DataPoint = data.get(iter);
        if(DataPoint.contains("n/a") || DataPoint.contains("N/A")){
            iter = iter +1;
            DataPoint = data.get(iter);
        }
        for(int i =0;i<DataPoint.size();i++){
            if(isAlphaNumeric(DataPoint.get(i)))
                Attributes.add(1);
            else
                Attributes.add(0);
        }
        return Attributes;
    }

    private String getMostly(ArrayList<String> data){
        HashMap<String, Integer> frequencyMap = new HashMap<>();

        int maxFrequency = 0;
        String mostFrequentString = null;

        for (String str : data) {
            int frequency = frequencyMap.getOrDefault(str, 0) + 1;
            frequencyMap.put(str, frequency);

            if (frequency > maxFrequency) {
                maxFrequency = frequency;
                mostFrequentString = str;
            }
        }

        return mostFrequentString;
    }
}