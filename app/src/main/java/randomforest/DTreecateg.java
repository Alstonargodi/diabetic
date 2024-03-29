package randomforest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class DTreecateg {

    /** Instead of checking each index we'll skip every INDEX_SKIP indices unless there's less than MIN_SIZE_TO_CHECK_EACH*/
    private static final int INDEX_SKIP=3;

    /** If there's less than MIN_SIZE_TO_CHECK_EACH points, we'll check each one */
    private static final int MIN_SIZE_TO_CHECK_EACH=10;

    /** If the number of data points is less than MIN_NODE_SIZE, we won't continue splitting, we'll take the majority vote */
    private static final int MIN_NODE_SIZE=5;

    /** the number of data records */
    private int N;

    /** the number of samples left out of the boostrap of all N to test error rate
     * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#ooberr">OOB error estimate</a>
     */
    private int testN;

    /** Of the testN, the number that were correctly identified */
    private int correct;

    /** an estimate of the importance of each attribute in the data record
     * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#varimp">Variable Importance</a>
     */
    private int[] importances;

    /** This keeps track of all the predictions done by this tree */
    public ArrayList<String> predictions;

    /** This is the root of the Decision Tree */
    private TreeNode root;

    /** This is a pointer to the Random Forest this decision tree belongs to */
    private RandomForestCateg forest;


    public DTreecateg(ArrayList<ArrayList<String>> data, RandomForestCateg forest, int treenum) {
        // TODO Auto-generated constructor stub
        this.forest=forest;
        N=data.size();
        importances = new int[RandomForestCateg.M];

        ArrayList<ArrayList<String>> train = new ArrayList<ArrayList<String>>(N);
        ArrayList<ArrayList<String>> test = new ArrayList<ArrayList<String>>();

        BootStrapSample(data,train,test,treenum);
        testN=test.size();
        correct=0;

        root=CreateTree(train,treenum);
        FlushData(root, treenum);
    }

    /**
     * Create a boostrap sample of a data matrix
     *
     * @param data		the data matrix to be sampled
     * @param train		the bootstrap sample
     * @param test		the records that are absent in the bootstrap sample
     */
    @SuppressWarnings("unchecked")
    private void BootStrapSample(ArrayList<ArrayList<String>> data,ArrayList<ArrayList<String>> train,ArrayList<ArrayList<String>> test,int numb){
        ArrayList<Integer> indices=new ArrayList<Integer>();
        for (int n=0;n<N;n++)
            indices.add((int)Math.floor(Math.random()*N));
        ArrayList<Boolean> in=new ArrayList<Boolean>();
        for (int n=0;n<N;n++)
            in.add(false); //have to initialize it first
        for (int num:indices){
            ArrayList<String> k = data.get(num);
            train.add((ArrayList<String>) k.clone());
//			train.add(data.get(num));
//			train.add((data.get(num)).clone());
            in.set(num,true);
        }//System.out.println("created training-data for tree : "+numb);
        for (int i=0;i<N;i++)
            if (!in.get(i))
                test.add(data.get(i));//System.out.println("created testing-data for tree : "+numb);//everywhere its set to false we get those to test data

//		System.out.println("bootstrap N:"+N+" size of bootstrap:"+bootstrap.size());
    }

    /**
     * This creates the decision tree according to the specifications of random forest trees.
     *
     * @param train		the training data matrix (a bootstrap sample of the original data)
     * @return			the TreeNode object that stores information about the parent node of the created tree
     */
    private TreeNode CreateTree(ArrayList<ArrayList<String>> train, int ntree){
        TreeNode root=new TreeNode();
        root.label = "|ROOT|";
        root.data=train;
        //System.out.println("creating ");
        RecursiveSplit(root,ntree);
        return root;
    }
    /**
     *
     * @author TreeNode
     *
     */
    private class TreeNode implements Cloneable{
        public boolean isLeaf;
        public ArrayList<TreeNode> ChildNode ;
        public TreeNode left;
        public TreeNode right;
        public int splitAttributeM;//which attribute its split on...
        public boolean spiltonCateg = false;
        public String Class;
        public ArrayList<ArrayList<String>> data;
        public String splitValue;//check this if it return false on splitonCateg
        public String label;//Label of each node
        public int generation;

        public TreeNode(){
            splitAttributeM=-99;
            splitValue="-99";
            generation=1;
            label = null;
            spiltonCateg = false;
            isLeaf = false;
            Class = null;
        }
        public TreeNode clone(){ //"data" element always null in clone
            TreeNode copy=new TreeNode();
            copy.isLeaf=isLeaf;
            for(TreeNode TN : ChildNode){
                if(TN != null){
                    copy.ChildNode.add(TN.clone());
                }
            }
            if (left != null) //otherwise null
                copy.left=left.clone();
            if (right != null) //otherwise null
                copy.right=right.clone();
            copy.splitAttributeM=splitAttributeM;
            copy.Class=Class;
            copy.splitValue=splitValue;
            return copy;
        }
    }
    private class DoubleWrap{//hold the entropy
        public double d;
        public DoubleWrap(double d){
            this.d=d;
        }
    }
    /**
     * This method will get the classes and will return the updates
     *
     */
    public ArrayList<String> CalculateClasses(ArrayList<ArrayList<String>> traindata,ArrayList<ArrayList<String>> testdata, int treenumber){
        ArrayList<String> predicts = new ArrayList<String>();
        for(ArrayList<String> record:testdata){
            String Clas = Evaluate(record, traindata);
            if(Clas==null)
                System.out.println("Evaluation Nul hua");
            predicts.add(Clas);
        }
        predictions = predicts;
        return predicts;
    }

    public String Evaluate(ArrayList<String> record, ArrayList<ArrayList<String>> tester){
        TreeNode evalNode=root;
        while (true) {
            if(evalNode.isLeaf)
                return evalNode.Class;
            else{
                if(evalNode.spiltonCateg){
                    // if its categorical
//						boolean found = false;
//						for(TreeNode child:evalNode.ChildNode){
//							// Check for child with label same the data point
//							if(nodeLable.equalsIgnoreCase(child.label)){//what if the category is not present at all
//								evalNode = child;System.out.println("going in child :"+evalNode.label);
//								found = true;
//								break;
//							}
//						}
//						//accomodate the missing values
//						if(!found){
//							// find if supervised or non supervised
//							System.out.println("this lable not found :"+nodeLable);
//							nodeLable = changeNodeLabel(evalNode.splitAttributeM,record,evalNode.data);
//							record.set(evalNode.splitAttributeM, nodeLable);
//							//Evaluate(record, tester);
//						}
                }else{
                    //if its real-valued
                    double Compare = Double.parseDouble(evalNode.splitValue);
                    double Actual = Double.parseDouble(record.get(evalNode.splitAttributeM));
                    if(Actual <= Compare){
                        evalNode=evalNode.left;
                    }else{
                        evalNode=evalNode.right;
                    }
                }
            }
        }
    }
    private String changeNodeLabel(int splitAttributeM, ArrayList<String> record,ArrayList<ArrayList<String>> data) {
        // TODO Auto-generated method stub
        // get the list of all the attributes where class is that
        String label = record.get(record.size()-1);
        ArrayList<String> ToFind = new ArrayList<String>();
        for(ArrayList<String> DP : data){
            if(DP.get(DP.size()-1).equalsIgnoreCase(label)){
                ToFind.add(DP.get(splitAttributeM));
            }
        }
        String Fill =null;
        Fill = forest.ModeofList(ToFind);
        return Fill;
    }


    private void RecursiveSplit(TreeNode parent, int Ntreenum){

        if (!parent.isLeaf){


            //-------------------------------Step A
            String Class=CheckIfLeaf(parent.data);
            if (Class != null){
                parent.isLeaf=true;
                parent.Class=Class;
//				PrintOutClasses(parent.data);
                return;
            }


            //-------------------------------Step B
            int Nsub=parent.data.size();
//			PrintOutClasses(parent.data);			

            parent.ChildNode = new ArrayList<TreeNode>();
            for(TreeNode TN: parent.ChildNode){
                TN = new TreeNode();
                TN.generation = parent.generation+1;
            }
            parent.left = new TreeNode();
            parent.left.generation=parent.generation+1;

            parent.right = new TreeNode();
            parent.right.generation = parent.generation+1;

            ArrayList<Integer> vars=GetVarsToInclude();//randomly selects Ms.Nos of attributes from M

            DoubleWrap lowestE=new DoubleWrap(Double.MAX_VALUE);


            //-------------------------------Step C
            for (int m:vars){//m is from 0-M
                SortAtAttribute(parent.data,m);//sorts on a particular column in the row
                ArrayList<Integer> DataPointToCheck=new ArrayList<Integer>();// which data points to be scrutinized
                for (int n=1;n<Nsub;n++){
                    String classA=GetClass(parent.data.get(n-1));
                    String classB=GetClass(parent.data.get(n));
                    if(!classA.equalsIgnoreCase(classB))
                        DataPointToCheck.add(n);
                }
                if (DataPointToCheck.size() == 0){//if all the Y-values are same, then get the class directly
                    parent.isLeaf=true;
                    parent.Class=GetClass(parent.data.get(0));
                    continue;
                }

                if (DataPointToCheck.size() > MIN_SIZE_TO_CHECK_EACH){
                    for (int i=0;i<DataPointToCheck.size();i+=INDEX_SKIP){
                        CheckPosition(m, DataPointToCheck.get(i), Nsub, lowestE, parent, Ntreenum);
                        if (lowestE.d == 0)//lowestE now has the minimum conditional entropy so IG is max there
                            break;
                    }
                }else{
                    for (int k:DataPointToCheck){
                        CheckPosition(m,k, Nsub, lowestE, parent, Ntreenum);
                        if (lowestE.d == 0)//lowestE now has the minimum conditional entropy so IG is max there
                            break;
                    }
                }
                if (lowestE.d == 0)
                    break;
            }
            //System.out.println("Adding "+parent.ChildNode.size()+" children at level: "+parent.generation);
            //-------------------------------Step D
            if(!parent.spiltonCateg){
                //....................Left Child
                if (parent.left.data.size() == 1){
                    parent.left.isLeaf=true;
                    parent.left.Class=GetClass(parent.left.data.get(0));
                }
                else if (parent.left.data.size() < MIN_NODE_SIZE){
                    parent.left.isLeaf=true;
                    parent.left.Class=GetMajorityClass(parent.left.data);
                }
                else {
                    Class=CheckIfLeaf(parent.left.data);
                    if (Class == null){
                        parent.left.isLeaf=false;
                        parent.left.Class=null;
//						System.out.println("make branch left: m:"+m);
                    }
                    else {
                        parent.left.isLeaf=true;
                        parent.left.Class=Class;
                    }
                }
                //....................Right Child
                if (parent.right.data.size() == 1){
                    parent.right.isLeaf=true;
                    parent.right.Class=GetClass(parent.right.data.get(0));
                }
                else if (parent.right.data.size() < MIN_NODE_SIZE){
                    parent.right.isLeaf=true;
                    parent.right.Class=GetMajorityClass(parent.right.data);
                }
                else {
                    Class=CheckIfLeaf(parent.right.data);
                    if (Class == null){
//						System.out.println("make branch right: m:"+m);
                        parent.right.isLeaf=false;
                        parent.right.Class=null;
                    }
                    else {
                        parent.right.isLeaf=true;
                        parent.right.Class=Class;
                    }
                }
                //....................Split if necessary
                if (!parent.left.isLeaf){
                    //System.out.println(" Splitting again from "+parent.label+" at level: "+parent.generation);
                    RecursiveSplit(parent.left,Ntreenum);
                }
                if (!parent.right.isLeaf){
                    //System.out.println(" Splitting again from "+parent.label+" at level: "+parent.generation);
                    RecursiveSplit(parent.right,Ntreenum);
                }
            }else{
                //....................for Categorical Children
                for(TreeNode Child:parent.ChildNode){
                    if(Child.label != null){
                        if(Child.data.size()==1){
                            Child.isLeaf=true;
                            Child.Class = GetClass(Child.data.get(0));
                        }else if(Child.data.size() <= MIN_NODE_SIZE){
                            Child.isLeaf = true;
                            Child.Class=GetMajorityClass(Child.data);
                        }else{
                            if(CheckIfLeaf(Child.data)!=null){
                                Child.isLeaf = true;
                                Child.Class = CheckIfLeaf(Child.data);
                            }
                        }
                    }
                }
                //Split again if necessary
                for(TreeNode Child : parent.ChildNode){
                    if(!Child.isLeaf){
                        System.out.println(" Splitting again from "+parent.label+", with data size "+Child.data.size()+" at gen "+parent.generation);
                        RecursiveSplit(Child, Ntreenum);
                    }
                }
            }
        }
    }
    /**
     * Given a data matrix, return the most popular Y value (the class)
     * @param data	The data matrix
     * @return		The most popular class
     */
    private String GetMajorityClass(ArrayList<ArrayList<String>> data){
        // find the max class for this data.
        ArrayList<String> ToFind = new ArrayList<String>();
        for(ArrayList<String> s:data){
            ToFind.add(s.get(s.size()-1));
        }
        String MaxValue = null; int MaxCount = 0;
        for(String s1:ToFind){
            int count =0;
            for(String s2:ToFind){
                if(s2.equalsIgnoreCase(s1))
                    count++;
            }
            if(count > MaxCount){
                MaxValue = s1;
                MaxCount = count;
            }
        }return MaxValue;
    }

    private double CheckPosition(int m,int n,int Nsub,DoubleWrap lowestE,TreeNode parent, int nTre){

        String real_OR_categ = parent.data.get(n).get(m);
        double entropy =0;

        if (n < 1) //exit conditions
            return 0;
        if (n > Nsub)
            return 0;

        if(isAlphaNumeric(real_OR_categ)){

            //this is a categorical thing

            ArrayList<Double> pr=getClassProbs(parent.data);
            entropy =CalEntropy(pr);
            if (entropy < lowestE.d){
                lowestE.d=entropy;
                parent.splitAttributeM=m;
                parent.spiltonCateg = true;
                parent.splitValue=parent.data.get(n).get(m);
                /**
                 * Adding Data to Child
                 *
                 */
                //Finding number of unique categorical values
                HashMap<String, Integer> coun = new HashMap<String, Integer>();
                for(ArrayList<String> s:parent.data){
                    if(coun.containsKey(s.get(m))){
                        coun.put(s.get(m), coun.get(s.get(m))+1);
                    }else
                        coun.put(s.get(m), 1);
                }//creating each child-node with its own data
                if(coun.size()>parent.ChildNode.size()){
                    for(Entry<String, Integer> entry : coun.entrySet()){
                        //if()
                    }
                }
                parent.ChildNode.clear();//remove everything before you add

                for(Entry<String, Integer> entry : coun.entrySet()){
                    ArrayList<ArrayList<String>> child_data = new ArrayList<ArrayList<String>>();
                    for(ArrayList<String> s : parent.data){
                        if(entry.getKey().equalsIgnoreCase(s.get(m)))
                            child_data.add(s);
                    }//adding node and its contents
                    TreeNode Child = new TreeNode();
                    Child.data = child_data;
                    Child.label = entry.getKey();
                    parent.ChildNode.add(Child);
                }//System.out.println("Added "+parent.ChildNode.size()+" children at level: "+parent.generation);
            }
        }else{

            //this is a real valued thing

            ArrayList<ArrayList<String>> lower=GetLower(parent.data,n);
            ArrayList<ArrayList<String>> upper=GetUpper(parent.data,n);
            ArrayList<Double> pl=getClassProbs(lower);
            ArrayList<Double> pu=getClassProbs(upper);
            double eL=CalEntropy(pl);
            double eU=CalEntropy(pu);

            entropy =(eL*lower.size()+eU*upper.size())/((double)Nsub);

            if (entropy < lowestE.d){
                lowestE.d=entropy;
                parent.splitAttributeM=m;
                parent.spiltonCateg=false;
                parent.splitValue = parent.data.get(n).get(m).trim();
                /**
                 * Adding Data to Left/Right Child
                 *
                 */
                parent.left.data=lower;
                parent.left.label="Left";
                parent.right.data=upper;
                parent.right.label="Right";
            }
        }
        return entropy;
    }

    /**
     * Split a data matrix and return the lower portion
     *
     * @param data		the data matrix to be split
     * @param nSplit	return all data records below this index in a sub-data matrix
     * @return			the lower sub-data matrix
     */
    private ArrayList<ArrayList<String>> GetLower(ArrayList<ArrayList<String>> data,int nSplit){
        ArrayList<ArrayList<String>> LS = new ArrayList<ArrayList<String>>();
        for(int n=0;n<nSplit;n++){
            LS.add(data.get(n));
        }return LS;
    }

    /**
     * Split a data matrix and return the upper portion
     *
     * @param data		the data matrix to be split
     * @param nSplit	return all data records above this index in a sub-data matrix
     * @return			the upper sub-data matrix
     */
    private ArrayList<ArrayList<String>> GetUpper(ArrayList<ArrayList<String>> data,int nSplit){
        int N=data.size();
        ArrayList<ArrayList<String>> LS = new ArrayList<ArrayList<String>>();
        for(int n=nSplit;n<N;n++){
            LS.add(data.get(n));
        }return LS;
    }

    private ArrayList<Double> getClassProbs(ArrayList<ArrayList<String>> record){
        double N=record.size();
        HashMap<String, Integer > counts = new HashMap<String, Integer>();
        for(ArrayList<String> s : record){
            String clas = GetClass(s);
            if(counts.containsKey(clas))
                counts.put(clas, counts.get(clas)+1);
            else
                counts.put(clas, 1);
        }
        ArrayList<Double> probs = new ArrayList<Double>();
        for(Entry<String, Integer> entry : counts.entrySet()){
            double prob = entry.getValue()/N;
            probs.add(prob);
        }return probs;
    }
    /**
     *  ln(2)
     */
    private static final double logoftwo=Math.log(2);
    /**
     * Given a probability mass function indicating the frequencies of class representation, calculate an "entropy" value using the method
     * in Tan|Steinbach|Kumar's "Data Mining" textbook
     *
     * @param ps			the probability mass function
     * @return				the entropy value calculated
     */
    private double CalEntropy(ArrayList<Double> ps){
        double e=0;
        for (double p:ps){
            if (p != 0) //otherwise it will divide by zero - see TSK p159
                e+=p*Math.log(p)/logoftwo;
        }
        return -e; //according to TSK p158
    }
    /**
     * Checks if attribute is categorical or not
     *
     * @param s
     * @return boolean true if it has an alphabet
     */
    private boolean isAlphaNumeric(String s){
        char c[]=s.toCharArray();
        for(int j=0;j<c.length;j++){
            if(Character.isLetter(c[j])){
                //System.out.println(s+" :has alpha");
                return true;
            }
        }return false;
    }
    /**
     * Sorts a data matrix by an attribute from lowest record to highest record
     *
     * @param data			the data matrix to be sorted
     * @param m				the attribute to sort on
     */
    private void SortAtAttribute(ArrayList<ArrayList<String>> data,int m){
        if(isAlphaNumeric(data.get(0).get(m)))
            Collections.sort(data,new AttributeComparatorCateg(m));
        else
            Collections.sort(data,new AttributeComparatorReal(m));
    }
    /**
     * This class compares two data records by numerically/categorically comparing a specified attribute
     *
     *
     */
    private class AttributeComparatorCateg implements Comparator<ArrayList<String>>{
        /** the specified attribute */
        private int m;
        /**
         * Create a new comparator
         * @param m			the attribute in which to compare on
         */
        public AttributeComparatorCateg(int m){
            this.m=m;
        }


        @Override
        public int compare(ArrayList<String> arg1, ArrayList<String> arg2) {//compare strings
            // TODO Auto-generated method stub
            String a = arg1.get(m);
            String b = arg2.get(m);
            return a.compareToIgnoreCase(b);
        }
    }

    /**
     * This class compares two data records by numerically/categorically comparing a specified attribute
     *
     *
     */
    private class AttributeComparatorReal implements Comparator<ArrayList<String>>{
        /** the specified attribute */
        private int m;
        /**
         * Create a new comparator
         * @param m			the attribute in which to compare on
         */
        public AttributeComparatorReal(int m){
            this.m=m;
        }
        /**
         * Compare the two data records. They must be of type int[].
         *
         * @param arg1		data record A
         * @param arg2		data record B
         * @return			-1 if A[m] < B[m], 1 if A[m] > B[m], 0 if equal
         */
        @Override
        public int compare(ArrayList<String> arg1, ArrayList<String> arg2) {//compare value of strings
            // TODO Auto-generated method stub
            double a2 = Double.parseDouble(arg1.get(m));
            double b2 = Double.parseDouble(arg2.get(m));
            if(a2<b2)
                return -1;
            else if(a2>b2)
                return 1;
            else
                return 0;
        }
    }

    private ArrayList<Integer> GetVarsToInclude() {
        boolean[] whichVarsToInclude=new boolean[RandomForestCateg.M];

        for (int i = 0; i< RandomForestCateg.M; i++)
            whichVarsToInclude[i]=false;

        while (true){
            int a=(int)Math.floor(Math.random()* RandomForestCateg.M);
            whichVarsToInclude[a]=true;
            int N=0;
            for (int i = 0; i< RandomForestCateg.M; i++)
                if (whichVarsToInclude[i])
                    N++;
            if (N == RandomForestCateg.Ms)
                break;
        }

        ArrayList<Integer> shortRecord=new ArrayList<Integer>(RandomForestCateg.Ms);

        for (int i = 0; i< RandomForestCateg.M; i++)
            if (whichVarsToInclude[i])
                shortRecord.add(i);
        return shortRecord;//values from 0-M
    }
    /**
     * Given a data record, return the Y value - take the last index
     *
     * @param record		the data record
     * @return				its y value (class)
     */
    public static String GetClass(ArrayList<String> record){
        return record.get(RandomForestCateg.M).trim();
    }
    /**
     * Given a data matrix, check if all the y values are the same. If so,
     * return that y value, null if not
     *
     * @param data		the data matrix
     * @return			the common class (null if not common)
     */
    private String CheckIfLeaf(ArrayList<ArrayList<String>> data){
//		System.out.println("checkIfLeaf");
        boolean isLeaf=true;
        String ClassA=GetClass(data.get(0));
        for(ArrayList<String> record : data){
            if(!ClassA.equalsIgnoreCase(GetClass(record))){
                isLeaf = false;
                return null;
            }
        }
//		if (isLeaf)
//			return GetClass(data.get(0));
//		else
//			return null;
        return GetClass(data.get(0));
    }
    /**
     * Recursively deletes all data records from the tree. This is run after the tree
     * has been computed and can stand alone to classify incoming data.
     *
     * @param node		initially, the root node of the tree
     * @param treenum
     */
    private void FlushData(TreeNode node, int treenum){
        node.data=null;
        if(node.ChildNode!=null){
            for(TreeNode TN : node.ChildNode){
                if(TN != null)
                    FlushData(TN,treenum);
            }
        }
        if (node.left != null)
            FlushData(node.left,treenum);
        if (node.right != null)
            FlushData(node.right,treenum);
    }

}