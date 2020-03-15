package beresheet2;
/**
 * 
 * @author Rachel
 *
 */
public class Spacecraft {

	// Constants
	public static final int    TIMEOUT            = 1;     // ms

	public static final double SELF_WEIGHT        = 165;   // kg
	public static final double SELF_RADIUS        = 0.925; // M
	public static final double MAIN_ENGINE_THRUST = 430;   // N
	public static final double SIDE_ENGINE_THRUST = 25;    // N
	public static final double NUM_SIDE_ENGINE    = 8;

	public static final double FUEL_MAIN_BURN     = 0.15;  // liter per sec, 12 liter per m'
	public static final double FUEL_SIDE_BURN     = 0.009; // liter per sec 0.6 liter per m'

	public static final String MSG_SUCCESS        = "The Eagle has landed!";
	public static final String MSG_ERROR          = "Houston.. We have a problem.";
	public static final String MSG_ERROR_SPEED    = "We crashed! :(\nThe speed was too high.";
	public static final String MSG_ERROR_ANGLE    = "We crashed! :(\nCouldn't land on our feet.";
	public static final String MSG_ERROR_FUEL     = "Out of fuel!";
	enum ERR_TYPES {
		FUEL,
		CRASH_SPEED,
		CRASH_ANGLE,
	}

	//public static final double ALT0 = 25000; //Altitude
	//public static final double V0 = 1700;    // m/s

	public static final int dt = 1;    // Delta Time is 1 sec

	// Variables
	private int    time      = 0;
	private double altitude  = 30000;
	private double fuel      = 420 / 2;
	private double vSpeed    = 0;
	private double hSpeed    = 1700;
	private double accX      = 0;
	private double accY      = 0;
	private double angle     = 90;
	private double angSpeed  = 0;
	private double angAcc    = 0;

	// Main function to start the landing stage
	public void landManeuver() {

		printHead();
		printState();

		// Continue the process until alt = 0 or out of fuel
		while ( this.fuel > 0 && this.altitude > 0 ) {

			try {
				Thread.sleep( TIMEOUT );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			time += dt; // 1sec per loop

			if( time % 5 == 0 ) {
				printState();
			}

			resetAcceleration();

			activateEngines();
			adjustRotation();

			updateAltitude();
			updateFuel();
			updateSpeed();

		}

		printState();

		if ( this.fuel <= 0 ) {
			printError( ERR_TYPES.FUEL );
			return;
		}

		if ( this.altitude < 0 ) {
			// We've reached the surface

			if ( this.vSpeed < -5 ) {
				// Speed too high
				printError( ERR_TYPES.CRASH_SPEED );
				return;
			}
			if ( angle < -5 || angle > 5 ) {
				// Spacecraft isn't straight
				printError( ERR_TYPES.CRASH_ANGLE );
				return;
			}
		}

		printSuccess();

	}

	private void adjustRotation() {

		if ( angle > 0 && angSpeed > -0.3 )
			// Rotate spacecraft clock-wise
			angAcc = -getAngularThrust();
		else if ( angle < 0 && angSpeed < 0.3 )
			// Rotate spacecraft counter-clock-wise
			angAcc = getAngularThrust();
		else
			angAcc = 0;

		updateAngularSpeed();
		updateAngle();

	}

	// Update angular speed
	private void updateAngularSpeed() {
		angSpeed += Physics.getVelocity( dt, angAcc );
	}

	// Update angle
	private void updateAngle() {
		angle += Physics.getDisplacement( dt, angSpeed, angAcc );
		normalizeAngle();
	}

	// Keep angle between -180 to 180 deg
	private void normalizeAngle() {

		while ( angle < -180 )
			angle += 360;
		while ( angle > 180 )
			angle -= 360;

	}

	public double getAngularThrust() {
		double torque = Physics.getTorque( SELF_RADIUS, getTotalEngineThrust() );
		double moment = Physics.getDiscMoment( SELF_RADIUS, getTotalWeight() );
		return Physics.getAccNewton2( torque, moment );
	}


	// Reset acceleration (without engines)
	private void resetAcceleration() {

		// no gravitational acceleration on X-axis
		this.accX = 0;

		// calculate gravitational acceleration
		double weight  = getTotalWeight();
		double gravity = Moon.getGravityForce( weight, this.altitude );
		this.accY      = Physics.getAccNewton2( gravity, weight );

	}

	// Calculate horizontal acceleration (backwards)
	private void thrustHorizontally() {
		this.accX -= getTotalEngineThrust();
	}

	// Calculate vertical acceleration (upwards)
	private void thrustVertically() {
		this.accY += getTotalEngineThrust();
	}

	// Activate engines if needed
	private void activateEngines() {

		// Thrust Horizontally to get to Moon orbit speed
		if ( this.hSpeed > Moon.ORBIT_SPEED )
			thrustHorizontally();

		// Thrust Vertically to slow down
		if ( this.altitude > 10000 ) {

			if ( this.vSpeed < -100 )
				thrustVertically();

		} else if ( this.altitude > 1000 ) {

			if ( this.vSpeed < -50 )
				thrustVertically();

		} else if ( this.altitude > 100 ) {

			if ( this.vSpeed < -10 )
				thrustVertically();

		} else {

			if ( this.vSpeed < -5 )
				thrustVertically();

		}

	}

	// Update remaining fuel
	private void updateFuel() {
		this.fuel -= dt * ( FUEL_MAIN_BURN + FUEL_SIDE_BURN * NUM_SIDE_ENGINE );
	}

	// Update remaining fuel
	private void updateAltitude() {
		this.altitude += Physics.getDisplacement( dt, this.vSpeed, this.accY );
	}

	// Update speed
	private void updateSpeed() {
		this.hSpeed += Physics.getVelocity( dt, this.accX );
		this.vSpeed += Physics.getVelocity( dt, this.accY );
	}

	public double getTotalWeight() {
		return SELF_WEIGHT + this.fuel;
	}

	public double getTotalEngineThrust() {
		double weight = getTotalWeight();
		double force  = MAIN_ENGINE_THRUST + SIDE_ENGINE_THRUST * NUM_SIDE_ENGINE;
		return Physics.getAccNewton2( force, weight );
	}


	private void printHead() {
		System.out.println( "Time\tAlt\t\tV-Speed\t\tV-Acc\t\tH-Speed\t\tH-Acc\t\tFuel\tWeight\tAngle\tA-Speed\tA-Acc" );
	}

	private void printState() {
		System.out.println( this.toString() );
	}

	private void printSuccess() {
		System.out.println( MSG_SUCCESS );
	}

	private void printError( ERR_TYPES type ) {

		System.out.println( MSG_ERROR );

		switch( type ) {
		case FUEL:
			System.out.println( MSG_ERROR_FUEL );
			break;
		case CRASH_SPEED:
			System.out.println( MSG_ERROR_SPEED );
			break;
		case CRASH_ANGLE:
			System.out.println( MSG_ERROR_ANGLE );
			break;
		default:
			break;
		}
	}


	@Override
	public String toString() {
		return String.format( "%4ds\t%,9.2f\t%5.2fm/s\t%5.2fm/s²\t%7.2fm/s\t%5.2fm/s²\t%.2f\t%.2f\t%5.2f°\t%5.2f\t%6.3f",
				time,
				altitude,
				vSpeed,
				accY,
				hSpeed,
				accX,
				fuel,
				getTotalWeight(),
				angle,
				angSpeed,
				angAcc
			);
	}


}

