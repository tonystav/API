package com.rest.API.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ButtonRenderer extends JButton implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	public ButtonRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
													boolean hasFocus, int row, int column) {
		try {
			if ((null == value) || (value.toString().isEmpty()) || (value.toString().isBlank())) {
				setForeground(Color.BLACK);
				setBackground(Color.WHITE);
				setBorderPainted(false);
				setEnabled(false);
			}
			else {
				setForeground(new JButton().getForeground());
				setBackground(new JButton().getBackground());
				setBorder(new JButton().getBorder());
				setEnabled(true);
			}

			setText((value == null) ? "" : value.toString());

			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					System.out.println("Value: " + value.toString());
					System.out.println("ActionEvent: " + ae.getActionCommand());
					}
				}
			);
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
		}

		return this;
	}
}
