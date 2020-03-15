package beresheet2;
/**
 * 
 * @author Rachel
 *
 */
public class Moon {

	// from: https://he.wikipedia.org/wiki/%D7%94%D7%99%D7%A8%D7%97
	public static final double MASS        = 7.3477 * Math.pow( 10, 22 ); // meters
	public static final double RADIUS      = 3475 * 1000; // meters
	public static final double GRAVITY     = -1.622;      // m/s^2
	public static final double EQ_SPEED    = 1700;        // m/s
	public static final double ORBIT_SPEED = 1022;        // m/s

	public static double getAcc(double speed) {
		double n = Math.abs(speed)/EQ_SPEED;
		double ans = (1-n)*GRAVITY;
		return ans;
	}

	public static double getGravityForce( double mass, double height ) {
		return -Physics.getGravityForce( mass, MASS, RADIUS + height );
	}
	
}

