package amidar.axtLoader;

public class WrongMagicNumberException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WrongMagicNumberException(String fileName, long magicNumber){
		super("the magicnumber in the file " + magicNumber + " is not the same as the required magicnumber " + AXTFile_const.MAGICNUMBER);
	}
	
}
