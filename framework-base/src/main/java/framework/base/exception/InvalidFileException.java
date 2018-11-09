package framework.base.exception;
/** 
*@description TODO 
*@author James Guo
**/
public class InvalidFileException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidFileException() {
		
	}
	
	public InvalidFileException(String value) {
		super(value);
	}

}
