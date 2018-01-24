package amidar.axtLoader;

public class WrongSizeInHeaderException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WrongSizeInHeaderException(String fileName, long givenSize, long calculatedSize){
		super("the given size in the header (" + givenSize + ")of the file is not the same as the size of the file (" + calculatedSize + ")!");
	}
	
}
