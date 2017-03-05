package com.chandruscm.minirto.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class RegEx
{
    static List<Pattern> Rex;
    final String invalid = "INVALID";

    public RegEx(){

        String p1 = "[ABCDGHJKLMNOPRTUW][ABDGHJKLNPRSY]";
        String p21 = "0[1-9]";
        String p22 = "[1-9]";
        String p23 = "[1-9]\\d";
        String p3 = "[A-Z]*";
        String p4 = "[1-9]";
        //String p41 = "0{1,3}+[1-9]";
        String p41 = "0[1-9]";
        String p42 = "00[1-9]";
        String p43 = "000[1-9]";
        String p44 = "[1-9]\\d"; //old p42
        //String p43 = "0{1,2}+"+p42;
        String p45 = "0[1-9]\\d";
        String p46 = "00[1-9]\\d";
        String p47 = "[1-9]\\d\\d";
        String p48 = "0[1-9]\\d\\d";
        String p49 = "[1-9]\\d\\d\\d";

        String pat1213 = p1+p21+p3;
        String pat1223 = p1+p22+p3;
        String pat1233 = p1+p23+p3;

        String a = pat1213 + p4;  //Format KL041, add 3 zeros, ret val 0
        String a1 = pat1213 + p41; //add 2 zeros, ret va 1
        String a2 = pat1213 + p42; //add 1 zero, ret val 2
        String a3 = pat1213 + p43; //No change, ret val 3
        String a4 = pat1213 + p44; //add 2 zero, ret val 4
        String a5 = pat1213 + p45; // add 1 zero, ret val 5
        String a6 = pat1213 + p46; // No change, ret val 6
        String a7 = pat1213 + p47; // add 1 zero, ret val 7
        String a8 = pat1213 + p48; // No change, ret val 8
        String a9 = pat1213 + p49; //No change, ret val 9

        String b = pat1223 + p4; //Format KL4DDDD d=[0-9,$], add zero dist for all b, add 3 zeros, ret val 10
        String b1 = pat1223 + p41; //add 2 zeros, ret val 11
        String b2 = pat1223 + p42; // add 1 zero, ret val 12
        String b3 = pat1223 + p43; // No change, ret val 13
        String b4 = pat1223 + p44; // add  2 zeros, ret val 14
        String b5 = pat1223 + p45; // add 1 zero, ret val 15
        String b6 = pat1223 + p46; // No change, ret val 16
        String b7 = pat1223 + p47; // add 1 zero, ret val 17
        String b8 = pat1223 + p48; // No change, ret val 18
        String b9 = pat1223 + p49; //No change, ret val 19
        //For all 'b' patterns add zero before RTO number

        String c = pat1233 + p4; // add 3 zeros, ret val 20
        String c1 = pat1233 + p41; // add 2 zeors, ret val 21
        String c2 = pat1233 + p42; // add 1 zero, ret val 22
        String c3 = pat1233 + p43; // No change, ret val 23
        String c4 = pat1233 + p44; // add 2 zero, ret val 24
        String c5 = pat1233 + p45; // add 1 zero, ret val 25
        String c6 = pat1233 + p46; // no change, ret val 26
        String c7 = pat1233 + p47; // add 1 zero, ret val 27
        String c8 = pat1233 + p48; // No change, ret val 28
        String c9 = pat1233 + p49; //No change, ret val 29

        Rex = new ArrayList<Pattern>();

        Rex.add(Pattern.compile(a));
        Rex.add(Pattern.compile(a1));
        Rex.add(Pattern.compile(a2));
        Rex.add(Pattern.compile(a3));
        Rex.add(Pattern.compile(a4));
        Rex.add(Pattern.compile(a5));
        Rex.add(Pattern.compile(a6));
        Rex.add(Pattern.compile(a7));
        Rex.add(Pattern.compile(a8));
        Rex.add(Pattern.compile(a9));

        Rex.add(Pattern.compile(b));
        Rex.add(Pattern.compile(b1));
        Rex.add(Pattern.compile(b2));
        Rex.add(Pattern.compile(b3));
        Rex.add(Pattern.compile(b4));
        Rex.add(Pattern.compile(b5));
        Rex.add(Pattern.compile(b6));
        Rex.add(Pattern.compile(b7));
        Rex.add(Pattern.compile(b8));
        Rex.add(Pattern.compile(b9));

        Rex.add(Pattern.compile(c));
        Rex.add(Pattern.compile(c1));
        Rex.add(Pattern.compile(c2));
        Rex.add(Pattern.compile(c3));
        Rex.add(Pattern.compile(c4));
        Rex.add(Pattern.compile(c5));
        Rex.add(Pattern.compile(c6));
        Rex.add(Pattern.compile(c7));
        Rex.add(Pattern.compile(c8));
        Rex.add(Pattern.compile(c9));
    }

    private int check(String lno){

        int count = 0;
        for(Pattern p : Rex){
            if(p.matcher(lno).matches()){
                return count;
                //count ++;
            }
            else
                count++;
        }
        return -1;
    }

    private String StdFormat(int form, String input){

        String ret = new String();
        String temp = new String();
        //int len = input.length();

        HashSet<Integer> retSet1 = new HashSet<Integer>(); //Set of all return values, where 1 zero is to be added
        retSet1.add(2);
        retSet1.add(5);
        retSet1.add(7);
        retSet1.add(12);
        retSet1.add(15);
        retSet1.add(17);
        retSet1.add(22);
        retSet1.add(25);
        retSet1.add(27);

        HashSet<Integer> retSet2 = new HashSet<Integer>();// Two zeros
        retSet2.add(1);
        retSet2.add(4);
        retSet2.add(11);
        retSet2.add(14);
        retSet2.add(21);
        retSet2.add(24);

        HashSet<Integer> retSet3 = new HashSet<Integer>(); // Three zeros
        retSet3.add(0);
        retSet3.add(10);
        retSet3.add(20);

        if( form>9 && form<=19 ) // District or RTO code is a single digit
            temp = new StringBuilder(input).insert(2,"0").toString();
        else
            temp = input;

        int len = temp.length();
        if(retSet1.contains(form))
            ret = new StringBuilder(temp).insert(len-3,"0").toString();
        else if(retSet2.contains(form))
            ret = new StringBuilder(temp).insert(len-2,"00").toString();
        else if(retSet3.contains(form))
            ret = new StringBuilder(temp).insert(len-1,"000").toString();
        else
            ret = temp;

        return ret;
    }

    public String validate(CharSequence input)
    {
        String numberPlate = input.toString().replaceAll("\\s","").toUpperCase();
        int match = check(numberPlate);

        if (match > -1)
        {
            String finalExp = StdFormat(match, numberPlate);
            return(finalExp.toUpperCase());
        }
        else
            return invalid;

    }
}