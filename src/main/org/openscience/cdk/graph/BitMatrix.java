package org.openscience.cdk.graph;

import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;

import java.util.BitSet;

/**
 * Mutable bit matrix which can eliminate linearly dependent rows and check
 * which rows were eliminated. These operations are useful when constructing a
 * cycle basis. From a graph we can represent the cycles as a binary vector of
 * incidence (edges). When processing cycles as these vectors we determine
 * whether a cycle can be made of other cycles in our basis. In the example
 * below each row can be made by XORing the other two rows.
 *
 * <blockquote><pre>
 * 1:   111000111   (can be made by 2 XOR 3)
 * 2:   111000000   (can be made by 1 XOR 3)
 * 3:   000000111   (can be made by 1 XOR 2)
 * </pre></blockquote>
 *
 * <blockquote><pre>
 * BitMatrix m = new BitMatrix(9, 3);
 * m.add(toBitSet("111000111"));
 * m.add(toBitSet("111000000"));
 * m.add(toBitSet("111000000"));
 * if (m.eliminate()){
 *   // rows are not independent
 * }
 * </pre></blockquote>
 *
 * @author John May
 * @cdk.module core
 * @cdk.githash
 */
@TestClass("org.openscience.cdk.graph.BitMatrixTest")
final class BitMatrix {

    /** rows of the matrix. */
    private final BitSet[] rows;

    /** keep track of row swaps. */
    private final int[] indices;

    /** maximum number of rows. */
    private final int max;

    /** number of columns. */
    private final int n;

    /** current number of rows. */
    private int m;

    /**
     * Create a new bit matrix with the given number of columns and rows. Note
     * the rows is the <i>maximum</i> number of rows we which to store. The
     * actual row count only increases with {@link #add(java.util.BitSet)}.
     *
     * @param columns number of columns
     * @param rows    number of rows
     */
    BitMatrix(final int columns, final int rows) {
        this.n = columns;
        this.max = rows;
        this.rows = new BitSet[rows];
        this.indices = new int[rows];
    }

    /**
     * Access the value at {@literal i}, {@literal j}.
     *
     * @param i the column index
     * @param j the row index
     * @return whether the value is set
     */
    private boolean get(int i, int j) {
        return rows[j].get(i);
    }

    /**
     * Swap the rows {@literal i} and {@literal j}, the swap is kept track of
     * internally allowing {@link #row(int)} and {@link #eliminated(int)} to
     * access the index of the original row.
     *
     * @param i row index
     * @param j row index
     */
    @TestMethod("swap") void swap(int i, int j) {
        BitSet row = rows[i];
        int k = indices[i];
        rows[i] = rows[j];
        indices[i] = indices[j];
        rows[j] = row;
        indices[j] = k;
    }

    /**
     * Find the current index of row {@literal j}.
     *
     * @param j original row index to find
     * @return the index now or < 0 if not found
     */
    private int rowIndex(int j) {
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == j)
                return i;
        }
        return -1;
    }

    /**
     * Access the row which was added at index {@literal j}.
     *
     * @param j index of row
     * @return the row which was added at index j
     */
    @TestMethod("swap")
    public BitSet row(int j) {
        return rows[rowIndex(j)];
    }

    /**
     * Check whether the row which was added at index {@literal j} has been
     * eliminated. {@link #eliminate()} should be invoked first.
     *
     * @param j row index
     * @return whether the row was eliminated
     * @see #eliminate()
     */
    @TestMethod("eliminate1,eliminate2,eliminate3")
    public boolean eliminated(int j) {
        return row(j).isEmpty();
    }

    /** Clear the matrix, setting the number of rows to 0. */
    @TestMethod("clear")
    public void clear() {
        m = 0;
    }

    /**
     * Add a row to the matrix.
     *
     * @param row the row
     */
    @TestMethod("swap,clear")
    public void add(BitSet row) {
        if (m >= max)
            throw new IndexOutOfBoundsException("initalise matrix with more rows");
        rows[m] = row;
        indices[m] = m;
        m++;
    }

    /**
     * Eliminate rows from the matrix which cannot be made by linearly combining
     * other rows.
     *
     * @return whether rows were eliminated
     * @see #eliminated(int)
     */
    @TestMethod("eliminate1,eliminate2,eliminate3")
    public boolean eliminate() {
        return eliminate(0, 0) != m;
    }

    /**
     * Gaussian elimination.
     *
     * @param x current column index
     * @param y current row index
     * @return the rank of the matrix
     */
    private int eliminate(int x, int y) {

        if (x < n && y < m) {

            int i = indexOf(x, y);

            // no row has x set, move to next column
            if (i < 0)
                return eliminate(x + 1, y);

            // reorder rows
            if (i != y)
                swap(i, y);

            // xor row with all vectors that have x set
            for (int j = 0; j < m; j++) {
                if (get(x, j) && j != y) {
                    rows[j] = xor(rows[j], rows[y]);
                }
            }

            return eliminate(x, y + 1);
        } else {
            return y;
        }
    }


    /**
     * Index of the the first row after {@literal y} where {@literal x} is set.
     *
     * @param x column index
     * @param y row index
     * @return the first index where {@literal x} is set, index is < 0 if none
     */
    @TestMethod("indexOf") int indexOf(int x, int y) {
        for (int j = y; j < m; j++) {
            if (rows[j].get(x))
                return j;
        }
        return -1;
    }

    /** @inheritDoc */
    @TestMethod("string")
    @Override public String toString() {
        StringBuilder sb = new StringBuilder((4 + n) * m);
        for (int j = 0; j < m; j++) {
            sb.append(indices[j]).append(": ");
            for (int i = 0; i < n; i++) {
                sb.append(get(i, j) ? '1' : '-');
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Utility method xors the vectors {@literal u} and {@literal v}. Neither
     * input is modified.
     *
     * @param u a bit set
     * @param v a bit set
     * @return the 'xor' of {@literal u} and {@literal v}
     */
    @TestMethod("xor")
    static BitSet xor(BitSet u, BitSet v) {
        BitSet w = (BitSet) u.clone();
        w.xor(v);
        return w;
    }
}
