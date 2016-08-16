public class PrintBlock{
	private static int getMaxLength(String... strings) {
    int len = Integer.MIN_VALUE;
    for (String str : strings) {
        len = Math.max(str.length(), len);
    }
    return len;
	}
	private static String padString(String str, int len) {
    StringBuilder sb = new StringBuilder(str);
    return sb.append(fill(' ', len - str.length())).toString();
	}
	private static String fill(char ch, int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
        sb.append(ch);
    }
    return sb.toString();
	}
	public static void printBox(String... strings) {
    int maxBoxWidth = getMaxLength(strings);
    String line = "+" + fill('-', maxBoxWidth + 2) + "+";
    System.out.println(line);
    for (String str : strings) {
        String x =String.format("| %s |%n", padString(str, maxBoxWidth));
		System.out.print(x);
		//System.out.printf("| %s |%n", padString(str, maxBoxWidth));
    }
    System.out.println(line);
	}
	public static String[] convertToBox(String[] str){
		int length = str.length;
		int maxBoxWidth = getMaxLength(str);
		String line = "+" + fill('-', maxBoxWidth + 2) + "+";
		String[] result = new String[length +2 ];
		result[0] = line;
		result[length + 1] =line;
		for(int i=1;i<length+1;i++){
			result[i] = String.format("| %s |", padString(str[i-1], maxBoxWidth)); 
		}
		return result;
	}
	public static String[] mergeStrArr(String[] str1, String[] str2){
		int l1=str1.length;
		int l2=str2.length;
		String[] result = new String[l1+l2];
		for(int i =0; i<l1; i++ ){
			result[i]=str1[i];
		}
		for(int j =0; j<l2; j++){
			result[l1+j]=str2[j];
		}
		return result;
	}
	
	public static void pBox(String... strings) {
		for (String str : strings) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) {
	String[] sss = new String[3];
	sss[0] ="coinbase";
	sss[1] ="dbjksdhkjdshkldshkl";
	sss[2] ="djlskjld";
    pBox(convertToBox(sss));
	}
}