package framework.base.exception;
/** 
*@description TODO 
*@author James Guo
**/
public class InvalidParameterExceptoin extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public InvalidParameterExceptoin() {
		
	}
	public InvalidParameterExceptoin(String value) {
		super(value);
	}
}
