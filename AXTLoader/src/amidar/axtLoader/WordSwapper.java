package amidar.axtLoader;

public class WordSwapper {
	
	
	
	public static byte[] swapWord(byte[] array, int start){
		
		byte first = array[start];
		byte second = array[start+1];
		
		array[start] = array[start+3];
		array[start+1] = array[start+2];
		array[start+2] = second;
		array[start+3] = first;
		
		return array;
	}
	
	public static byte[] swapWords(byte[] array, int start, int end){
		for(int i = start; i < end; i += 4){
			array = swapWord(array, i);
		}
		return array;
	}
	
	public static byte[] swapWordsByNumWords(byte[] array, int start, int numWords){
		
		return swapWords(array, start, start + (numWords << 2));
	}

}