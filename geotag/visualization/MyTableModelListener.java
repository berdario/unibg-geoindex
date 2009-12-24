/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * 
 * @author Giorgio Ghisalberti
 */
public class MyTableModelListener implements TableModelListener {
        JTable table;
    
        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        MyTableModelListener(JTable table) {
            this.table = table;
        }
    
        

    public void tableChanged(TableModelEvent e) {
         int firstRow = e.getFirstRow();
            int lastRow = e.getLastRow();
            int mColIndex = e.getColumn();
            
            int row = e.getFirstRow();
            int column = e.getColumn();
            
            TableModel model = (TableModel)e.getSource();
            String columnName = model.getColumnName(column);
            Object data = model.getValueAt(row, column);
            
            System.out.println(" < " + data + " >");
            
            
            switch (e.getType()) {
              case TableModelEvent.INSERT:
                // The inserted rows are in the range [firstRow, lastRow]
                for (int r=firstRow; r<=lastRow; r++) {
                    // Row r was inserted
                }
                break;
              case TableModelEvent.UPDATE:
                if (firstRow == TableModelEvent.HEADER_ROW) {
                    if (mColIndex == TableModelEvent.ALL_COLUMNS) {
                        // A column was added
                    } else {
                        // Column mColIndex in header changed
                    }
                } else {
                    // The rows in the range [firstRow, lastRow] changed
                    for (int r=firstRow; r<=lastRow; r++) {
                        // Row r was changed
    
                        if (mColIndex == TableModelEvent.ALL_COLUMNS) {
                            // All columns in the range of rows have changed
                        } else {
                            // Column mColIndex changed
                        }
                    }
                }
                break;
              case TableModelEvent.DELETE:
                // The rows in the range [firstRow, lastRow] changed
                for (int r=firstRow; r<=lastRow; r++) {
                    // Row r was deleted
                }
                break;
            }
            
        }
    
    public int toModel(JTable table, int vColIndex) {
        if (vColIndex >= table.getColumnCount()) {
            return -1;
        }
        return table.getColumnModel().getColumn(vColIndex).getModelIndex();
    }
 }