import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Scanner;
/***
 * Generate and validate HashCash stamps.
 * 
 * @author Sheldon
 * 
 */
public class BlockChain {

	private static final byte[] hashBuff = new byte[32];
	private static MessageDigest md = null;
	private static int MAX_BITS = hashBuff.length * Byte.SIZE;

	private static Map<Character, String> charBinStrMap = new HashMap<Character, String>();
	static {
		charBinStrMap.put('0', "0000");
		charBinStrMap.put('1', "0001");
		charBinStrMap.put('2', "0010");
		charBinStrMap.put('3', "0011");
		charBinStrMap.put('4', "0100");
		charBinStrMap.put('5', "0101");
		charBinStrMap.put('6', "0110");
		charBinStrMap.put('7', "0111");
		charBinStrMap.put('8', "1000");
		charBinStrMap.put('9', "1001");
		charBinStrMap.put('A', "1010");
		charBinStrMap.put('B', "1011");
		charBinStrMap.put('C', "1100");
		charBinStrMap.put('D', "1101");
		charBinStrMap.put('E', "1110");
		charBinStrMap.put('F', "1111");
	}

	private static char[] randChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
			'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', '=', '/', '+' };

	private static String genRandStr() {

		StringBuilder builder = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < 10; i++) {
			builder.append(randChars[random.nextInt(randChars.length)]);
		}

		return builder.toString();
	}

	private static String toBinStr(byte[] buff) {
		StringBuilder tmp = new StringBuilder();
		for (char c : toHexStr(buff).toCharArray()) {
			tmp.append(charBinStrMap.get(c));
		}
		return tmp.toString();
	}

	private static String toHexStr(byte[] buff) {
		StringBuilder tmp = new StringBuilder();
		for (byte b : buff) {
			tmp.append(String.format("%02X", b));
		}
		return tmp.toString();
	}

	/***
	 * Slightly slower but more convenient version of valid(...). Computes the
	 * numBits parameter directly from the string with more overhead.
	 * 
	 * @param stamp
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws DigestException
	 */
	public static boolean valid(String stamp) throws NoSuchAlgorithmException,
			DigestException {
		return valid(Integer.parseInt(stamp.split(":")[1]), stamp);
	}

	/***
	 * Validate a HashCash stamp.
	 * 
	 * @param numBits
	 *            Number of leading zeroes that must be zero for the stamp to be
	 *            valid
	 * @param stamp
	 *            The HashCash stamp
	 * @return true if valid else false
	 * @throws NoSuchAlgorithmException
	 * @throws DigestException
	 */
	public static boolean valid(int numBits, String stamp)
			throws NoSuchAlgorithmException, DigestException {

		if (numBits > MAX_BITS) {
			throw new IllegalArgumentException(String.format(
					"Parameter numBits has a maximum size of %d", MAX_BITS));
		}

		boolean result = false;

		if (md == null) {
			md = MessageDigest.getInstance("SHA-256");
		}

		md.reset();
		md.update(stamp.getBytes());
		md.digest(hashBuff, 0, hashBuff.length);
	
		System.out.println(toHexStr(hashBuff));// print out the hashed values

			/*
			 * Inefficient way of computing the stamp by creating the necessary
			 * bitstring, then counting the number of leading zeroes. This
			 * involves a lot of allocation and comparison, so is saved for the
			 * rarer case where we want more than 31 leading zeroes.
			 */
			boolean nonZeroCharFound = false;
			char[] chars = toBinStr(hashBuff).toCharArray();
			for (int i = 0; i < numBits; i++) {
				if (chars[i] != '0') {
					nonZeroCharFound = true;
					break;
				}
			}
			result = !nonZeroCharFound;

		return result;
	}

	/***
	 * Generate a valid HashCash stamp.
	 * 
	 * @param numBits
	 *            Number of leading zeroes for the stamp. Increases the amount
	 *            of time to find the stamp as a function of pow(2, numBits).
	 *            The computation is CPU-bound, so faster clock speeds means
	 *            less real time to compute a stamp (at the cost of more
	 *            energy).
	 * @param resourceStr
	 *            The client-defined resource string. Could be an email address,
	 *            ip address, etc.
	 * @return A valid HashCash stamp.
	 * @throws NoSuchAlgorithmException
	 * @throws DigestException
	 */
	public static String generate(int numBits, String resourceStr)
			throws NoSuchAlgorithmException, DigestException {

		if (numBits > MAX_BITS) {
			throw new IllegalArgumentException(String.format(
					"Parameter numBits has a maximum size of %d", MAX_BITS));
		}

		/*
		 * ver:bits:date:resource:[ext]:rand:counter
		 */
		String result = null;

		Date currDate = new Date(System.currentTimeMillis());

		int version = 1;
		String dateStr = null;
		{
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
			fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateStr = fmt.format(currDate);
		}

		String ext = "";
		String randStr = genRandStr();
		int counter = 1;

		while (result == null) {
			String stamp = "The above line shows the hashed value\nThe number of leading bits is >>> " + numBits +
							"\nThe nonce is >>> "+ randStr +"\nTotal number of times attempted >>> " + counter;
		/* 	String stamp = "The above line shows the hashed value\nThe number of leading bits is >>> " + numBits+"\nThe message is >>> "+resourceStr +
							"\nThe nonce is >>> "+ randStr +"\nTotal number of times attempted >>> " + counter; */
			if (valid(numBits, stamp)) {
				result = stamp;
				break;
			}

			counter++;
		}
		
		return result;
	}

	public static void main(String[] args) {
		String coinbase ="";
		int num =0;
		int blockNo =1;
		Scanner sc = new Scanner(System.in);
		boolean flag = true;
		System.out.println("BlockChain Demo!");
		
		System.out.println("Please enter the number of leading zero bits >>");
		num = sc.nextInt();
		sc.nextLine();
		
		while(flag){
			
			System.out.println("Please enter the coinbase\n(the miner's account address) >>");
			coinbase = sc.nextLine();
			if(coinbase.equals("")){
				break;
			}
			String[] msgForHash = {"Block NO."+ blockNo,"Coinbase: 25 Bitcoin will be credit to " + coinbase};
			System.out.println("Please gather the trasactions happened in the past 10mins \n(enter number of transactions) >>");
			int numTx = sc.nextInt();
			sc.nextLine();
			String[] allTx = {"Transactions for the past 10 mins"};
			// build n number of transaction blocks
			for(int i=0; i < numTx; i++){
				Random random = new Random();
				int numTxIn = random.nextInt(3)+1;
				int numTxOut = random.nextInt(3)+1;
				String[] tx = new String[numTxIn + numTxOut +2];
				tx[0] ="Transaction no. " + (i+1);
				for(int j =0;j<numTxIn ; j++){
					tx[j+1] = "From: " + genRandStr();
				}
				for(int k = 0; k <numTxOut ; k++){
					tx[numTxIn+1+k] = "To: " +genRandStr();
				}
				tx[numTxIn +numTxOut +1] ="Transaction fee Bitcoin $"+ (random.nextInt(10)/100);
				tx = PrintBlock.convertToBox(tx);
				allTx = PrintBlock.mergeStrArr(allTx,tx);
			}
			String[] hashValue = {"The previous blocks' hash value:",toHexStr(hashBuff)};
			msgForHash = PrintBlock.mergeStrArr(msgForHash, allTx);
			msgForHash = PrintBlock.mergeStrArr(msgForHash, hashValue);
			
			String message ="";
			for(int i=0; i<msgForHash.length; i++){
				message = message+ msgForHash[i];
			}
			try {
				String stamp = generate(num, message);
				System.out.println(stamp);
				
				Date currDate = new Date(System.currentTimeMillis());
				String dateStr = null;
				{
					SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
					fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
					dateStr = fmt.format(currDate);
				}
				//String minedTiming =
				String[] minedTime={"Block mined at " + dateStr};
				String[] newBlock = PrintBlock.mergeStrArr(minedTime,msgForHash);
				newBlock = PrintBlock.convertToBox(newBlock);
				PrintBlock.pBox(newBlock);
				
			} catch (NoSuchAlgorithmException | DigestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			blockNo++;
			
		}

	}
	


		


}