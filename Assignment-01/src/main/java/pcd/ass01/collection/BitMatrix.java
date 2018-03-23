package pcd.ass01.collection;

import java.util.BitSet;

/**
 * BitMatrix - two dimensional indexed collection of bit values;
 * for use with book
 * <a href="http://www.cs.orst.edu/~budd/books/jds/">Classic Data Structures
 * in Java</a>
 * by <a href="http://www.cs.orst.edu/~budd">Timothy A Budd</a>,
 * published by <a href="http://www.awl.com">Addison-Wesley</a>, 2001.
 *
 * @author Timothy A. Budd
 * @version 1.1 September 1999
 */

public class BitMatrix {

    private int rowsNumber;
    private int columnsNumber;

    /**
     * initialize a newly created matrix of bit values
     *
     * @param numRows number of rows in new matrix
     * @param numColumns number of columns in new matrix
     */
    public BitMatrix (int numRows, int numColumns) {
        this.rowsNumber = numRows;
        this.columnsNumber = numColumns;
        rows = new BitSet[numRows];
        for (int i = 0; i < numRows; i++)
            rows[i] = new  BitSet(numColumns);
    }

    protected BitSet [ ] rows;


    /**
     * clear a value in the bit matrix
     *
     * @param i row index
     * @param j column index
     */
    public void clear (int i, int j) { rows[i].clear(j); }

    /**
     * get a value from the bit matrix
     *
     * @param i row index
     * @param j column index
     * @return true if the bit is set, false otherwise
     */
    public boolean get (int i, int j) { return rows[i].get(j); }

    /**
     * set a value in the bit matrix
     *
     * @param i row index
     * @param j column index
     */
    public void setTrue (int i, int j) { rows[i].set(j); }

    public void set (int i, int j, boolean value) { rows[i].set(j, value); }

    public int getRowsNumber() {
        return rowsNumber;
    }

    public int getColumnsNumber() {
        return columnsNumber;
    }
}
