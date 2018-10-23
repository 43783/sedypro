package ch.hesge.sedypro.utils;

import java.util.List;

/**
 * Compilation: javac Matrix.java Execution: java Matrix
 *
 * A bare-bones immutable data type for M-by-N matrices.
 * http://introcs.cs.princeton.edu/java/95linear/Matrix.java
 * 
 */
public class Matrix {

	// Private attributes
	private final int rows;
	private final int columns;
	private final double[][] values; // row-by-column array

	/**
	 * Create m-by-n matrix of 0's
	 * 
	 * @param m row count
	 * @param n column count
	 */
	public Matrix(int m, int n) {
		this.rows = m;
		this.columns = n;
		this.values = new double[m][n];
	}

	/**
	 * Copy constructor.
	 * 
	 * @param A the matrix to copy
	 */
	public Matrix(Matrix A) {
		this(A.values);
	}

	/**
	 * Create a matrix based on 2d array
	 * 
	 * @param data the matrix initial data
	 */
	public Matrix(double[][] data) {
		
		this.rows = data.length;
		this.columns = data[0].length;
		this.values = new double[rows][columns];
		
		// Fill matrix values
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				this.values[i][j] = data[i][j];
			}
		}
	}

	/**
	 * Create a matrix based on its column vectors.
	 * 
	 * @param columnVectors
	 */
	public Matrix(List<Vector> columnVectors) {
		
		this.rows = columnVectors.size();
		this.columns = columnVectors.get(0).size();
		this.values = new double[rows][columns];
		
		// Fill matrix values
		for (int i = 0; i < rows; i++) {
			setColumn(i, columnVectors.get(i));
		}
	}

	/**
	 * Create and return a random m-by-n matrix with values between 0 and 1.
	 * 
	 * @param rowSize
	 * @param columnSize
	 * @return
	 */
	public static Matrix getRandom(int rowSize, int columnSize) {
		
		Matrix A = new Matrix(rowSize, columnSize);
		
		// Fill matrix values
		for (int i = 0; i < rowSize; i++) {
			for (int j = 0; j < columnSize; j++) {
				A.values[i][j] = Math.random();
			}
		}
		
		return A;
	}

	/**
	 * Create and return the n-by-n identity matrix.
	 * 
	 * @param size
	 * @return
	 */
	public static Matrix getIdentity(int size) {
		
		Matrix I = new Matrix(size, size);
		
		for (int i = 0; i < size; i++) {
			I.values[i][i] = 1;
		}
		
		return I;
	}
	
	/**
	 * Reset all matrix cells to a specific value.
	 * 
	 * @param value
	 */
	public void reset(double value) {

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				values[i][j] = value;
			}
		}
		
	}
	
	/**
	 * Return matrix row size.
	 * 
	 * @return
	 */
	public int getRowSize() {
		return rows;
	}
	
	/**
	 * Return matrix column size
	 * 
	 * @return
	 */
	public int getColumnSize() {
		return columns;
	}
	
	/**
	 * Get matrix cell value
	 * 
	 * @param i the cell's row
	 * @param j the cell's column
	 * @return
	 */
	public double getValue(int i, int j) {
		return this.values[i][j];
	}

	/**
	 * Set matrix cell value
	 * 
	 * @param i the cell's row
	 * @param j the cell's column
	 * @param value the new cell's value
	 */
	public void setValue(int i, int j, double value) {
		this.values[i][j] = value;
	}

	/**
	 * Initialize all vector elements with the same value.
	 *
	 * @param value
	 *        values to initialize.
	 */
	public void setValues(double value) {

		for (int i = 0; i < this.getRowSize(); i++) {
			for (int j = 0; j < this.getColumnSize(); j++) {
				this.values[i][j] = value;
			}
		}
	}
	
	/**
	 * Get the row vector for a specific index
	 * 
	 * @param row
	 * @return
	 */
	public Vector getRow(int row) {
		
		Vector rowVector = new Vector(columns);
		
		for(int j = 0; j < columns; j++) {
			rowVector.setValue(j, this.values[row][j]);
		}
		
		return rowVector;
	}

	/**
	 * Set matrix row for a specific index
	 * 
	 * @param row
	 * @param rowVector
	 */
	public void setRow(int row, Vector rowVector) {
		
		if (columns != rowVector.size())
			throw new RuntimeException("Illegal vector dimensions.");
		
		for(int j = 0; j < columns; j++) {
			this.values[row][j] = rowVector.getValue(j);
		}
	}
	
	/**
	 * Set all rows at once
	 * 
	 * @param rowVector
	 */
	public void setRows(Vector rowVector) {
		
		if (columns != rowVector.size())
			throw new RuntimeException("Illegal vector dimensions.");
		
		for (int i = 0; i < rows; i++) {
			setRow(i, rowVector);
		}
	}	
	
	/**
	 * Set all rows at once
	 * 
	 * @param rowVectorList
	 */
	public void setRows(List<Vector> rowVectorList) {
		
		if (rows != rowVectorList.size())
			throw new RuntimeException("Illegal vector dimensions.");
		
		for (int i = 0; i < rows; i++) {
			setRow(i, rowVectorList.get(i));
		}
	}	
	
	/**
	 * Get the column vector for a specific index
	 * 
	 * @param column
	 * @return
	 */
	public Vector getColumn(int column) {
		
		Vector columnVector = new Vector(rows);
		
		for(int i = 0; i < rows; i++) {
			columnVector.setValue(i, this.values[i][column]);
		}
		
		return columnVector;
	}

	/**
	 * Set matrix column for a specific index
	 * 
	 * @param column
	 * @param columnVector
	 */
	public void setColumn(int column, Vector columnVector) {
		
		if (rows != columnVector.size())
			throw new RuntimeException("Illegal vector dimensions.");
		
		for(int i = 0; i < rows; i++) {
			this.values[i][column] = columnVector.getValue(i);
		}
	}
	
	/**
	 * Set all columns at once
	 * 
	 * @param columnVector
	 */
	public void setColumns(Vector columnVector) {
		
		if (rows != columnVector.size())
			throw new RuntimeException("Illegal vector dimensions.");
		
		for (int j = 0; j < columns; j++) {
			setColumn(j, columnVector);
		}
	}	
	
	/**
	 * Set all columns at once
	 * 
	 * @param columnVectorList
	 */
	public void setColumns(List<Vector> columnVectorList) {
		
		if (columns != columnVectorList.size())
			throw new RuntimeException("Illegal vector dimensions.");
		
		for (int j = 0; j < columns; j++) {
			setColumn(j, columnVectorList.get(j));
		}
	}	
	
	/**
	 * Swap rows i and j
	 * 
	 * @param i
	 * @param j
	 */
	private void swapRows(int i, int j) {
		double[] temp = values[i];
		values[i] = values[j];
		values[j] = temp;
	}

	/**
	 * Create and return the transpose of the invoking matrix
	 * 
	 * @return
	 */
	public Matrix transpose() {
		
		Matrix A = new Matrix(columns, rows);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				A.values[j][i] = this.values[i][j];
			}
		}
		
		return A;
	}

	/**
	 * Return a new matrix, where all elements are normalize between 0 and a maxValue.
	 * 
	 * @param maxValue the maximum cell value of the returned matrix
	 * @return a normalized matrix
	 */
	public Matrix normalize(double normalizedValue) {
		
		Matrix A = new Matrix(this);
		
		double maxCellValue = 0d;
		
		// First retrieve max cell's value
		for (int i = 0; i < A.rows; i++) {
			for (int j = 0; j < A.columns; j++) {
				maxCellValue = Math.max(maxCellValue, A.values[i][j]);
			}
		}

		if (maxCellValue == 0) {
			A.setValues(0d);
		}
		else {
			
			double normalizeFactor = normalizedValue / maxCellValue;
			
			// Then normalize all cell's values
			for (int i = 0; i < A.rows; i++) {
				for (int j = 0; j < A.columns; j++) {
					A.values[i][j] = A.values[i][j] * normalizeFactor;
				}
			}
		}

		return A;
	}
	
	/**
	 * Return C = A + B
	 * 
	 * @param B
	 * @return
	 */
	public Matrix add(Matrix B) {
		
		Matrix A = this;
		 
		if (B.rows != A.rows || B.columns != A.columns)
			throw new RuntimeException("Illegal matrix dimensions.");
		
		Matrix C = new Matrix(rows, columns);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				C.values[i][j] = A.values[i][j] + B.values[i][j];
			}
		}
		
		return C;
	}

	/**
	 * Return C = A - B
	 * 
	 * @param B
	 * @return
	 */
	public Matrix minus(Matrix B) {
		
		Matrix A = this;
		
		if (B.rows != A.rows || B.columns != A.columns)
			throw new RuntimeException("Illegal matrix dimensions.");
		
		Matrix C = new Matrix(rows, columns);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				C.values[i][j] = A.values[i][j] - B.values[i][j];
			}
		}
		
		return C;
	}

	/**
	 * Does A = B exactly?
	 * Return true if matrix element are all equals.
	 * 
	 * @param B
	 * @return
	 */
	public boolean equals(Matrix B) {
		
		Matrix A = this;
		
		if (B.rows != A.rows || B.columns != A.columns)
			throw new RuntimeException("Illegal matrix dimensions.");
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (A.values[i][j] != B.values[i][j])
					return false;
			}
		}
		
		return true;
	}

	/**
	 * Return C = A * B
	 * 
	 * @param B
	 * @return
	 */
	public Matrix multiply(Matrix B) {
		 
		Matrix A = this;
		
		if (A.columns != B.rows)
			throw new RuntimeException("Illegal matrix dimensions.");
		
		Matrix C = new Matrix(A.rows, B.columns);
		
		for (int i = 0; i < C.rows; i++) {
			for (int j = 0; j < C.columns; j++) {
				for (int k = 0; k < A.columns; k++) {
					C.values[i][j] += (A.values[i][k] * B.values[k][j]);
				}
			}
		}
		
		return C;
	}

	/**
	 * Return C = A * B (term-by-term)
	 * 
	 * @param B
	 * @return
	 */
	public Matrix ebeMultiply(Matrix B) {
		
		Matrix A = this;
		
		if (A.rows != B.rows || A.columns != B.columns)
			throw new RuntimeException("Illegal matrix dimensions.");
		
		Matrix C = new Matrix(A.rows, A.columns);
		
		for (int i = 0; i < A.rows; i++) {
			for (int j = 0; j < A.columns; j++) {
					C.values[i][j] = (A.values[i][j] * B.values[i][j]);
			}
		}
		
		return C;
	}
	
	/**
	 * Return x = A^-1 b, assuming A is square and has full rank
	 * 
	 * @param rhs
	 * @return
	 */
	public Matrix solve(Matrix rhs) {
		
		if (rows != columns || rhs.rows != columns || rhs.columns != 1)
			throw new RuntimeException("Illegal matrix dimensions.");

		// Create copies of the data
		Matrix A = new Matrix(this);
		Matrix b = new Matrix(rhs);

		// Gaussian elimination with partial pivoting
		for (int i = 0; i < columns; i++) {

			// Find pivot row and swap
			int max = i;
			for (int j = i + 1; j < columns; j++) {
				if (Math.abs(A.values[j][i]) > Math.abs(A.values[max][i])) {
					max = j;
				}
			}
			
			A.swapRows(i, max);
			b.swapRows(i, max);

			// Singular
			if (A.values[i][i] == 0.0) 
				throw new RuntimeException("Matrix is singular.");

			// Pivot within b
			for (int j = i + 1; j < columns; j++) {
				b.values[j][0] -= b.values[i][0] * A.values[j][i] / A.values[i][i];
			}

			// Pivot within A
			for (int j = i + 1; j < columns; j++) {
				
				double m = A.values[j][i] / A.values[i][i];
				
				for (int k = i + 1; k < columns; k++) {
					A.values[j][k] -= A.values[i][k] * m;
				}
				
				A.values[j][i] = 0.0;
			}
		}

		// Back substitution
		Matrix x = new Matrix(columns, 1);
		
		for (int j = columns - 1; j >= 0; j--) {
		
			double t = 0.0;
			
			for (int k = j + 1; k < columns; k++) {
				t += A.values[j][k] * x.values[k][0];
			}
			
			x.values[j][0] = (b.values[j][0] - t) / A.values[j][j];
		}
		
		return x;
	}

	/**
	 * Print matrix to standard output
	 */
	public void show() {
		
		for (int i = 0; i < rows; i++) {
			
			for (int j = 0; j < columns; j++) {
				System.out.printf("%9.4f ", values[i][j]);
			}
			
			System.out.println();
		}
	}

	/**
	 * Test method
	 */
	public static void main(String[] args) {
		
		double[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3 } };
		Matrix D = new Matrix(d);
		D.show();
		System.out.println();

		Matrix A = Matrix.getRandom(5, 5);
		A.show();
		System.out.println();

		A.swapRows(1, 2);
		A.show();
		System.out.println();

		Matrix B = A.transpose();
		B.show();
		System.out.println();

		Matrix C = Matrix.getIdentity(5);
		C.show();
		System.out.println();

		A.add(B).show();
		System.out.println();

		B.multiply(A).show();
		System.out.println();

		// shouldn't be equal since AB != BA in general
		System.out.println(A.multiply(B).equals(B.multiply(A)));
		System.out.println();

		Matrix b = Matrix.getRandom(5, 1);
		b.show();
		System.out.println();

		Matrix x = A.solve(b);
		x.show();
		System.out.println();

		A.multiply(x).show();
	}
}
