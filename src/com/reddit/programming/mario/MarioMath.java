package com.reddit.programming.mario;

final class MarioMath {

	public static float stepsToJump(float h) {
		if(h < 26.6f) return 10*h/133;
		if(h < 64.6) return (float) (17-Math.sqrt(281-80*h/19))/2;
		return Float.POSITIVE_INFINITY;
	}

	// I LOVE ALL THIS TYPING!
	// NOT ONLY ON THE KEYBOARD, BUT ALSO THE CREATION OF NEW TYPES AND
	// SPECIFYING THEIR USE
	static interface DistanceFunction { public float value(float dx0, float n); }

	static private class FallDistance implements DistanceFunction {
		public float value(float ya0, float steps) {
			return fallDistance(ya0, steps);
		}
	}

	static private class RunDistance implements DistanceFunction {
		public float value(float v0, float steps) {
			return runDistance(v0, steps);
		}
	}

	static private float secantSolve(DistanceFunction f, float distance, float dx0, float min) {
		float x0=1, x1=2, xdiff;
		float sgn = 1;
		if(distance < 0) { sgn = -1; distance = -distance; }
		do {
			float fx0 = f.value(dx0, x0);
			float fx1 = f.value(dx0, x1);
			xdiff = (fx1-distance) * (x1 - x0)/(fx1 - fx0);
			x0 = x1;
			x1 -= xdiff;
			// if our iteration takes us negative, negate and hope it doesn't loop
			if(x1 < min) x1 = 2*min-x1; // reflect about min
		} while(Math.abs(xdiff) > 1e-4);
		return x1*sgn;
	}

	///////////////////////////////////////////////////////////////
	// public mathods (ha), finally

	static private final RunDistance _runDistance = new RunDistance();

	static private final FallDistance _fallDistance = new FallDistance();

	static public float runDistance(float v0, float steps) {
		// Mario's running iteration looks like this:
		//   xa'[n] = xa[n-1] + s
		//   x[n] = x[n-1] + xa'[n]
		//   xa[n] = xa'[n] * d
		// Working through the recurrence:
		// x[n] = x0 + xa0*Sum[d^i,{i,0,n-1}] + s*Sum[(n-i)*d^i,{i,0,n-1}]
		// where d === damping = 89/100 and s === step size = 12/10
		// if you substitute and solve you get this:

		float d_n = (float) Math.pow(0.89f, steps); // d^n
		return (1320*steps - 20*(d_n-1)*(55*v0-534))/121;
	}

	static public float fallDistance(float ya0, float steps) {
		// Mario's falling iteration looks like this:
		//   y[n] = y[n-1] + ya[n-1]
		//   ya[n] = (ya[n-1] * d) + s
		// Solving the recurrence:
		//   ya[n] = ya0*d^n + s*Sum[d^i, {i,0,n-1}]
		//   y[n] = y0 + ya0*Sum[d^i, {i,0,n-1}] + s*(Sum[(n-1-i)*d^i, {i,0,n-2}])
		//
		float d_n = (float) Math.pow(0.85f, steps); // d^n
		return 20*steps - 20*(d_n-1)*(ya0-20)/3;
	}

	static public float runSpeed(float xa0, float steps) {
		float d_n = (float) Math.pow(0.89f, steps);
		return -9.70909f * (d_n-1) + d_n*xa0;
	}

	// runDistance is terrible to invert, so use the secant method to solve it
	public static float stepsToRun(float distance, float v0) {
		return secantSolve(_runDistance, distance, v0, 0);
	}

	// as, of course, is fallDistance
	public static float stepsToFall(float height, float ya0) {
		// this has too many numerical problems; let's just underestimate it
		if(ya0 < 0) {
			// if we're "falling upwards" then find where we're falling
			// downwards at the same height
			float apogee = (float) (Math.log(1-ya0/20)/Math.log(0.85));
			return secantSolve(_fallDistance, height, ya0, 2*apogee);
		}
		else
			return secantSolve(_fallDistance, height, ya0, 0);
	}

}

