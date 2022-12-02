package testppup3.views;

import testppup3.controlers.ImpactAnalyzer;

public class TestMyMethods {

	public static void main(String[] args) {

        String text = "AABCCAAADCBBAADBBC";
        String str = "AA";
 
        int count = ImpactAnalyzer.countMatches(text, str);
        System.out.println("hu");

	}

}
