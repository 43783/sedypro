package ch.hesge.sedypro.utils;

/**
 * Creates a monotone cubic spline from a given set of control points.
 * Ref: https://gist.github.com/lecho/7627739
 * 
 * @author lecho
 *
 */
public class SplineInterpolator {

	private Vector mX;
	private Vector mY;
	private double[] mM;

	/**
	 * Creates a monotone cubic spline from a given set of control points.
	 * 
	 * The spline is guaranteed to pass through each control point exactly. Moreover, assuming the control points are
	 * monotonic (Y is non-decreasing or non-increasing) then the interpolated values will also be monotonic.
	 * 
	 * This function uses the Fritsch-Carlson method for computing the spline parameters.
	 * http://en.wikipedia.org/wiki/Monotone_cubic_interpolation
	 * 
	 * @param x
	 *            The X component of the control points, strictly increasing.
	 * @param y
	 *            The Y component of the control points
	 * @return
	 * 
	 * @throws IllegalArgumentException
	 *             if the X or Y arrays are null, have different lengths or have fewer than 2 values.
	 */
	public SplineInterpolator(Vector x, Vector y) {
		init(x, y);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	private void init(Vector x, Vector y) {
		
		if (x == null || y == null || x.size() != y.size() || x.size() < 2)
			throw new IllegalArgumentException("There must be at least two control points and the arrays must be of equal length.");

		int size = x.size();
		double[] d = new double[size - 1]; // could optimize this out
		double[] m = new double[size];

		// Compute slopes of secant lines between successive points.
		for (int i = 0; i < size - 1; i++) {
			
			double h = x.getValue(i + 1) - x.getValue(i);

			if (h <= 0f) {
				throw new IllegalArgumentException("The control points must all have strictly increasing X values.");
			}
			
			d[i] = (y.getValue(i+1) - y.getValue(i)) / h;
		}

		// Initialize the tangents as the average of the secants.
		m[0] = d[0];
		for (int i = 1; i < x.size() - 1; i++) {
			m[i] = (d[i - 1] + d[i]) * 0.5f;
		}
		m[size - 1] = d[size - 2];

		// Update the tangents to preserve monotonicity.
		for (int i = 0; i < size - 1; i++) {
			if (d[i] == 0f) { // successive Y values are equal
				m[i] = 0f;
				m[i + 1] = 0f;
			} 
			else {
				double a = m[i] / d[i];
				double b = m[i + 1] / d[i];
				double h = (float) Math.hypot(a, b);
				
				if (h > 9f) {
					double t = 3f / h;
					m[i] = t * a * d[i];
					m[i + 1] = t * b * d[i];
				}
			}
		}
		
		mX = x;
		mY = y;
		mM = m;
	}

	/**
	 * Interpolates the value of Y = f(X) for given X. Clamps X to the domain of the spline.
	 * 
	 * @param x
	 *            The X value.
	 * @return The interpolated Y = f(X) value.
	 */
	public double interpolate(float x) {
		
		// Handle the boundary cases.
		final int n = mX.size();
		if (Float.isNaN(x)) {
			return x;
		}
		
		if (x <= mX.getValue(0)) {
			return mY.getValue(0);
		}
		
		if (x >= mX.getValue(n - 1)) {
			return mY.getValue(n - 1);
		}

		// Find the index 'i' of the last point with smaller X.
		// We know this will be within the spline due to the boundary tests.
		int i = 0;
		while (x >= mX.getValue(i + 1)) {
			i += 1;
			if (x == mX.getValue(i)) {
				return mY.getValue(i);
			}
		}

		// Perform cubic Hermite spline interpolation.
		double h = mX.getValue(i + 1) - mX.getValue(i);
		double t = (x - mX.getValue(i)) / h;
		
		return (mY.getValue(i) * (1 + 2 * t) + h * mM[i] * t) * (1 - t) * (1 - t) + (mY.getValue(i + 1) * (3 - 2 * t) + h * mM[i + 1] * (t - 1)) * t * t;
	}
}
