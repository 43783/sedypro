package ch.hesge.sedypro.utils;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a simple vector storing doubles.
 * 
 * @author Eric Harth
 */
public class Vector {

	// Private attribute
	private int size;
	private double values[];

	/**
	 * Construct a vector of zeroes.
	 *
	 * @param size
	 *        size of the vector.
	 */
	public Vector(int vectorSize) {
		this.size = vectorSize;
		this.values = new double[vectorSize];
	}

	/**
	 * Construct a vector with preset values.
	 *
	 * @param size
	 *        size of the vector
	 * @param initialValue
	 *        all entries will be set with this value.
	 */
	public Vector(int vectorSize, double initialValue) {
		this.size = vectorSize;
		this.values = new double[vectorSize];
		Arrays.fill(this.values, initialValue);
	}

	/**
	 * Create a vector based on a array
	 * 
	 * @param data the matrix initial data
	 */
	public Vector(double[] values) {
		this.size = values.length;
		this.values = values;
	}

	/**
	 * Construct a vector with preset values.
	 *
	 * @param size
	 *        size of the vector
	 * @param initialValue
	 *        all entries will be set with this value.
	 */
	public Vector(List<Double> initialValues) {
		
		this.size = initialValues.size();
		this.values = new double[this.size];
		
		for (int i = 0; i < this.size; i++) {
			this.values[i] = initialValues.get(i);
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param A the vector to copy
	 */
	public Vector(Vector v) {
		this(v.values);
	}
	
	/**
	 * Get vector dimension.
	 * 
	 * @return
	 *         the vector dimension.
	 */
	public int size() {
		return size;
	}

	/**
	 * Get a single vector's element value.
	 * 
	 * @param i
	 *        element index.
	 * @return
	 *         a scalar value
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public double getValue(int i) {
		return values[i];
	}

	/**
	 * Set a single element value.
	 * 
	 * @param i
	 *        element index
	 * @param value
	 *        a scalar value
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public void setValue(int i, double value) {
		values[i] = value;
	}

	/**
	 * Initialize all vector elements with the same value.
	 *
	 * @param value
	 *        values to initialize.
	 */
	public void setValues(double value) {

		for (int i = 0; i < size; i++) {
			values[i] = value;
		}
	}
	
	/**
	 * Compute the sum of this vector and {@code v}.
	 * Returns a new vector instance.
	 *
	 * @param v
	 *        Vector to be added.
	 * @return a vector
	 * @throws IllegalArgumentException
	 */
	public Vector add(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		Vector result = new Vector(size);

		for (int i = 0; i < size; i++) {
			result.values[i] = values[i] + v.values[i];
		}

		return result;
	}

	/**
	 * Add a scalar to a single vector element.
	 * 
	 * @param i
	 *        element
	 * @param scalar
	 *        the scalar value to add
	 * @return a vector
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public Vector addValue(int i, double scalar) {

		Vector result = new Vector(size);
		
		values[i] += scalar;
		
		return result;
	}
	
	/**
	 * Add a scalar to a all vector elements.
	 *
	 * @param scalar
	 *        the scalar to add.
	 * @return a vector
	 */
	public Vector add(double scalar) {

		Vector result = new Vector(size);

		for (int i = 0; i < size; i++) {
			result.values[i] = values[i] + scalar;
		}

		return result;
	}

	/**
	 * Subtract {@code v} from this vector.
	 * Returns a new vector. Does not change instance data.
	 *
	 * @param v
	 *        Vector to be subtracted.
	 * @return {@code this} - {@code v}.
	 * @throws IllegalArgumentException
	 */
	public Vector substract(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		Vector result = new Vector(size);

		for (int i = 0; i < size; i++) {
			result.values[i] = values[i] - v.values[i];
		}

		return result;
	}

	/**
	 * Substract a scalar to a single vector element.
	 * 
	 * @param i element
	 * @param value scalar value
	 * @return a vector
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public Vector substract(int i, double scalar) {

		Vector result = new Vector(size);
		
		values[i] -= scalar;
		
		return result;
	}

	/**
	 * Substract a scalar to a all vector elements.
	 * 
	 * @param scalar
	 * @return
	 * @throws IllegalArgumentException
	 */
	public Vector substract(double scalar) throws IllegalArgumentException {

		Vector result = new Vector(size);

		for (int i = 0; i < size; i++) {
			result.values[i] = values[i] - scalar;
		}

		return result;
	}

	/**
	 * Multiple all vector elements by a scalar
	 * 
	 * @param scalar
	 * @return
	 * @throws IllegalArgumentException
	 */
	public Vector multiply(double scalar) throws IllegalArgumentException {

		Vector result = new Vector(size);

		for (int i = 0; i < size; i++) {
			result.values[i] = values[i] * scalar;
		}

		return result;
	}

	/**
	 * Element-by-element multiplication.
	 *
	 * @param v
	 *        Vector by which instance elements must be multiplied
	 * @return a vector containing this[i] * v[i] for all i.
	 * @throws IllegalArgumentException
	 */
	public Vector ebeMultiply(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		Vector result = new Vector(size);

		for (int i = 0; i < size; i++) {
			result.values[i] = values[i] * v.values[i];
		}

		return result;
	}

	/**
	 * Return a new vector, where all element value are normalized and inverted.
	 * That is each original element equals to 0 are 0 and other original element are 1.
	 *
	 * @return a inverted vector
	 */
	public Vector invert() throws IllegalArgumentException {

		Vector result = new Vector(this.size).normalizeWithLowThreshold(0);
		
		for (int i = 0; i < this.size; i++) {
			result.values[i] = this.values[i] == 0 ? 1 : 0;
		}

		return result;
	}

	/**
	 * Normalize all vector element to 1 or 0,
	 * according the threshold specified. If cell value is greater 
	 * than threshold, the cell content is changed to 1, otherwise 0.
	 * 
	 * @param lowThreshold the trigger value used to decide if 0 or 1 is present in vector
	 * @return a normalized vector
	 * @throws IllegalArgumentException
	 */
	public Vector normalizeWithLowThreshold(double lowThreshold) throws IllegalArgumentException {

		Vector result = new Vector(this.size);

		for (int i = 0; i < this.size; i++) {
			result.values[i] = this.values[i] > lowThreshold ? 1 : 0;
		}

		return result;
	}

	/**
	 * Return a new vector, where all elements are normalize between 0 and a maxValue.
	 * 
	 * @param maxValue the maximum cell value of the returned vector
	 * @return a normalized vector
	 */
	public Vector normalize(double maxValue) {
		
		double maxCellValue = 0d;
		Vector v = new Vector(this);
		
		// First retrieve max cell's value
		for (int i = 0; i < v.size; i++) {
			maxCellValue = Math.max(maxCellValue, v.values[i]);
		}

		if (maxCellValue == 0) {
			v.setValues(0d);
		}
		else {
			
			double normalizeFactor = maxValue / maxCellValue;
			
			// Then normalize all cell's values
			for (int i = 0; i < v.size; i++) {
				v.values[i] = v.values[i] * normalizeFactor;
			}
		}

		return v;
	}
	
	/**
	 * Compute the dot product of this vector with {@code v}.
	 *
	 * @param v
	 *        Vector with which dot product should be computed
	 * @return the scalar dot product between this instance and {@code v}.
	 * @throws IllegalArgumentException
	 */
	public double dotProduct(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		double dot = 0d;

		for (int i = 0; i < size; i++) {
			dot += values[i] * v.values[i];
		}

		return dot;
	}

	/**
	 * Returns the L<sub>0</sub> norm (aka zero norm) of the vector.
	 * <p>
	 * The L<sub>0</sub> norm is the number of non-zero value of the vector elements.
	 * </p>
	 *
	 * @return the norm.
	 * @see #getNorm()
	 */
	public int getL0Norm() {

		int count = 0;

		for (int i = 0; i < size; i++) {
			if (values[i] > 0)
				count++;
		}

		return count;
	}

	/**
	 * Returns the L<sub>1</sub> norm of the vector.
	 * <p>
	 * The L<sub>1</sub> norm is the sum of the absolute values of each vector elements.
	 * </p>
	 *
	 * @return the norm.
	 * @see #getNorm()
	 * @see #getL1Distance(RealVector)
	 */
	public double getL1Norm() {

		double sum = 0d;

		for (int i = 0; i < size; i++) {
			sum += Math.abs(values[i]);
		}

		return sum;
	}

	/**
	 * Returns the L<sub>2</sub> norm of the vector.
	 * <p>
	 * The L<sub>2</sub> norm is the squared root of the sum of the squared vector elements.
	 * This is the standard euclidian norm.
	 * </p>
	 *
	 * @return the norm.
	 */
	public double getL2Norm() {

		double sum = 0d;

		for (int i = 0; i < size; i++) {
			sum += values[i] * values[i];
		}

		return Math.sqrt(sum);
	}

	/**
	 * Distance between two vectors.
	 * <p>
	 * This method computes the distance consistent with L<sub>0</sub> norm,
	 * i.e. the sum of the different values of each vectors.
	 * </p>
	 *
	 * @param v
	 *        Vector to which distance is requested.
	 * @return the distance between two vectors.
	 * @throws IllegalArgumentException
	 */
	public double getL0Distance(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		double sum = 0d;

		for (int i = 0; i < size; i++) {
			sum += this.values[i] == v.values[i] ? 1 : 0;
		}

		return sum;
	}

	/**
	 * Distance between two vectors.
	 * <p>
	 * This method computes the distance consistent with L<sub>1</sub> norm,
	 * i.e. the sum of the absolute values of the elements differences.
	 * </p>
	 *
	 * @param v
	 *        Vector to which distance is requested.
	 * @return the distance between two vectors.
	 * @throws IllegalArgumentException
	 */
	public double getL1Distance(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		double sum = 0d;

		for (int i = 0; i < size; i++) {
			sum += Math.abs(values[i] - v.values[i]);
		}

		return sum;
	}

	/**
	 * Distance between two vectors.
	 * <p>
	 * This method computes the distance consistent with the L<sub>2</sub> norm,
	 * i.e. the square root of the sum of element differences, or Euclidean
	 * distance.
	 * </p>
	 *
	 * @param v
	 *        Vector to which distance is requested.
	 * @return the distance between two vectors.
	 * @throws IllegalArgumentException
	 */
	public double getL2Distance(Vector v) throws IllegalArgumentException {

		if (this.size != v.size)
			throw new IllegalArgumentException("Vector dimension mismatch.");

		double sum = 0d;

		for (int i = 0; i < size; i++) {
			double delta = values[i] - v.values[i];
			sum += delta * delta;
		}

		return Math.sqrt(sum);
	}

	/**
	 * Computes the cosine of the angle between this vector and the
	 * argument.
	 *
	 * @param v
	 *        Vector.
	 * @return the cosine of the angle between this vector and {@code v}.
	 * @throws IllegalArgumentException
	 */
	public double cosine(Vector v) throws IllegalArgumentException {

		final double v1Norm = this.getL2Norm();
		final double v2Norm = v.getL2Norm();

		if (v1Norm == 0 || v2Norm == 0) {
			throw new IllegalArgumentException("Vector norm zero not allowed.");
		}

		return dotProduct(v) / (v1Norm * v2Norm);
	}

	/**
	 * Return the maximul value contained in the vector.
	 * 
	 * @return a double
	 */
	public double max() {
 		
		double maxValue = 0.0;
		
		for(int i = 0; i < this.size(); i++) {
			if (this.values[i] > maxValue) maxValue = this.values[i];
		}
		
		return maxValue;
 	}
	
	/**
	 * Check if any element value of this vector is {@code NaN}.
	 *
	 * @return {@code true} if any element of this vector is {@code NaN},
	 *         {@code false} otherwise.
	 */
	public boolean isNaN() {

		for (int i = 0; i < size; i++) {
			if (Double.isNaN(values[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check whether any element value of this vector is infinite and none
	 * are {@code NaN}.
	 *
	 * @return {@code true} if any element of this vector is infinite and
	 *         none are {@code NaN}, {@code false} otherwise.
	 */
	public boolean isInfinite() {

		if (isNaN()) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			if (Double.isInfinite(values[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if this vector has all its elements to zero.
	 *
	 * @return {@code true} if any element values of this vector is zero and
	 *         {@code false} otherwise.
	 */
	public boolean isNullVector() {
		return this.getL2Norm() == 0;
	}
}
