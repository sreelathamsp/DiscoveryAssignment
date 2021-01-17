package za.co.discovery.assignment.exception;

public class SamePlanetsNotAllowedException extends Exception {

	private static final long serialVersionUID = 1L;


	@Override
	public String getMessage() {
		return "Source Planet and Destination Planet cannot be same";
	}
}