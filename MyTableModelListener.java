package com.rest.API;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class MyTableModelListener implements TableModelListener {
	JTable table;

	MyTableModelListener(JTable table) {
		this.table = table;
	}

	public void tableChanged(TableModelEvent tme) {
		int firstRow = tme.getFirstRow(), lastRow = tme.getLastRow(), column = tme.getColumn(), type = tme.getType();
		String value = tme.getSource().toString();

		switch (tme.getType()) {
			case TableModelEvent.UPDATE:
				//System.out.println("TableModelEvent:: type: " + type + ", row: " + firstRow + ", column: " + column + ", value: ~" + value + "~");
				break;
		}
	}
}
