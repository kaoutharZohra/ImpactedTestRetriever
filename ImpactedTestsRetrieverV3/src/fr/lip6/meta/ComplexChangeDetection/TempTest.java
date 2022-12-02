package fr.lip6.meta.ComplexChangeDetection;

public class TempTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s = "addProperty(EOperationImpl@5cabe2f2,name, 'getRankA ( ) ',350).";
		String temp = s.split("'")[1];
		temp.replaceAll("\\(", "-");//here in case the name contains '( )'
		temp.replaceAll("\\)", "*");
		System.out.print(temp.replaceAll("\\(", "").replaceAll("\\)", ""));
		
		long i = 2/3;
		double j = (double)2/(double)3;
		float k = 3/2;
		System.out.println("\n"+ i +", " +j+", " +k+", ");
		
	}

}
