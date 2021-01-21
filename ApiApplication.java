package com.rest.API;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rest.API.GUI.ButtonRenderer;
import com.rest.API.model.Equation;

@SpringBootApplication
public class ApiApplication extends JFrame {
	private static final long serialVersionUID = 1L;
	private static JFrame equationFrame;
	private static JPanel equationPanel;
	private static JTable equationTable = new JTable();
	private static JScrollPane listScrollPane;
	private static int screenWidth = 1500, screenHeight = 1000;
	private static String[] headers = null;
	private static String[][] equationList = null;
	private static DefaultTableModel equationModel = new DefaultTableModel();

	public ApiApplication() {
		super();
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);

		// Create and display the window.
	    equationFrame = new JFrame();
		equationFrame.setTitle("Equation Access");
		equationFrame.setPreferredSize(new Dimension(screenWidth, screenHeight));
		equationFrame.setSize(screenWidth, screenHeight);
		equationFrame.setLayout(new BorderLayout());
		equationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		equationPanel = createEquationListScreen();
		equationFrame.add(equationPanel, BorderLayout.CENTER);
		equationFrame.pack();
		equationFrame.setVisible(true);
	}

	private static String[] getColumns() {
		String[] columnList = new String[1];

		@SuppressWarnings("rawtypes")
		Class Equation = Equation.class;
		Field[] equationColumns = Equation.getDeclaredFields();
		String columns = new String();
		for (Field field : equationColumns) {
			if (field != null) {
				columns += StringUtils.capitalize(field.getName());
				columns += ",";
			}
		}
		columnList = columns.split(",");

		return columnList;
	}

	private static String[][] getEquations(String equation, String description, String advice, String category) {
		String[] equations = new String[1], fields = new String[1];
		String[][] eqnList = null;

		try {
			if ((null == equation) || (equation.isBlank()) || (equation.isEmpty())) { equation = "%"; }
			if ((null == description) || (description.isBlank()) || (description.isEmpty())) { description = "%"; }
			if ((null == advice) || (advice.isBlank()) || (advice.isEmpty())) { advice = "%"; }
			if ((null == category) || (category.isBlank()) || (category.isEmpty())) { category = "%"; }

			URL url = new URL("http://localhost:8080/equations/filter/" + equation + "/" + description + "/" + advice + "/" + category);

;			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
			    throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}

			// Get all entries from database
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String eqns = br.readLine().replace(java.util.regex.Pattern.quote("["), "").replace(java.util.regex.Pattern.quote("]"), "");
			equations = eqns.split("\\{");	// Doesn't consider empty or singleton results, which don't have "{" or "}"

			eqnList = new String[equations.length+1][10];

			for (int nextEqn=1; nextEqn<equations.length; nextEqn++) {
				fields = equations[nextEqn].split(",\"");

				for (int nextFld=0; nextFld<fields.length; nextFld++) {
					String thisFld = cleanupString(fields[nextFld]),
							thisKey = cleanupString(StringUtils.substringBefore(thisFld, ":")),
							thisValue = cleanupString(StringUtils.substringAfter(thisFld, ":"));

					// Assign explicitly database fields to table columns (home grown MVC)
					switch (thisKey) {
						case "id":
							eqnList[nextEqn][0] = String.format("%03d", Integer.parseInt(thisValue));
							break;
						case "equation":
							eqnList[nextEqn][1] = cleanupString(thisValue);
							break;
						case "description":
							eqnList[nextEqn][2] = cleanupString(thisValue);
							break;
						case "advice":
							eqnList[nextEqn][3] = cleanupString(thisValue);
							break;
						case "category":
							if (StringUtils.isNumeric(thisValue)) {eqnList[nextEqn][4] = cleanupString(getCategoryName(Integer.parseInt(thisValue))); }
							else { eqnList[nextEqn][4] = cleanupString(thisValue); }
							break;
						case "edit":
							eqnList[nextEqn][5] = cleanupString(thisValue);
							break;
					}
				}
			}

			// Create empty row for list end, for new entries
			int lastRow = eqnList.length-1;

			for (int lastFld=0; lastFld<10; lastFld++) {
				eqnList[lastRow][lastFld] = " ";
			}

			// First array element not valid, so remove it
			eqnList = ArrayUtils.remove(eqnList, 0);

			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return eqnList;
	}

	private static JPanel createEquationListScreen() {
		// Set up the window.
        equationPanel = new JPanel();
		equationPanel.setPreferredSize(new Dimension(screenWidth, screenHeight));
		equationPanel.setSize(screenWidth, screenHeight);
        equationPanel.setBounds(0, 0, screenWidth, screenHeight);
        equationPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        equationPanel.setLayout(new BorderLayout());

		// Include window elements
        JPanel topPanel = new JPanel(new GridLayout(0, 1));
        JLabel textLabel = new JLabel("Equation List", JLabel.CENTER);
		topPanel.add(textLabel);

		// Search filters section
		JPanel filterPanel = new JPanel();
		JLabel filterLabel = new JLabel("Filter By:", JLabel.CENTER);
		filterPanel.add(filterLabel, BorderLayout.NORTH);

		JPanel filters = new JPanel(new GridLayout(1, 5));
		JLabel equationLabel = new JLabel("Equation:", JLabel.CENTER);
		filters.add(equationLabel);
		JTextField equationFilter = new JTextField();
		filters.add(equationFilter);

		JLabel descriptionLabel = new JLabel("Description:", JLabel.CENTER);
		filters.add(descriptionLabel);
		JTextField descriptionFilter = new JTextField();
		filters.add(descriptionFilter);

		JLabel adviceLabel = new JLabel("Advice:", JLabel.CENTER);
		filters.add(adviceLabel);
		JTextField adviceFilter = new JTextField();
		filters.add(adviceFilter);

		JLabel categoryLabel = new JLabel("Category:", JLabel.CENTER);
		filters.add(categoryLabel);
		DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<String>();
		JComboBox<String> jcb = new JComboBox<String>(dcbm);
		dcbm.addAll(getAllCategories());
		jcb.setModel(dcbm);
		filters.add(jcb);

		JLabel editLabel = new JLabel("Edit:", JLabel.CENTER);
		filters.add(editLabel);
		JTextField editFilter = new JTextField();
		filters.add(editFilter);

		JButton filterSearch = new JButton("Search");
		filterSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setupEquationDisplay(prepareValue(equationFilter.getText()),
									prepareValue(descriptionFilter.getText()),
									prepareValue(adviceFilter.getText()),
									prepareValue(jcb.getItemAt(jcb.getSelectedIndex())));

				// Remove & replace previous contents with next contents
				equationPanel.remove(listScrollPane);
				listScrollPane = new JScrollPane(equationTable);
				listScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
				equationPanel.add(listScrollPane, BorderLayout.CENTER);

				// Refresh entire hierarchy of display elements
				equationTable.revalidate();
				equationTable.setVisible(true);
				equationTable.repaint();
				listScrollPane.revalidate();
				listScrollPane.setVisible(true);
				listScrollPane.repaint();
				equationPanel.revalidate();
				equationPanel.setVisible(true);
				equationPanel.repaint();
			}
        });

		filters.add(filterSearch);

		JButton filterClear = new JButton("Clear");
		filterClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				equationFilter.setText(null);
				descriptionFilter.setText(null);
				adviceFilter.setText(null);
				jcb.setSelectedIndex(-1);
				editFilter.setText(null);
			}
		});
		filters.add(filterClear);
		filterPanel.add(filters);

		topPanel.add(filterPanel);
        topPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        equationPanel.add(topPanel, BorderLayout.NORTH);

        // Retrieve database results only if don't already have them: prevents overwrite of filter searching
        if (equationTable.getRowCount() <= 0) {
	        setupEquationDisplay(prepareValue(equationFilter.getText()),
								prepareValue(descriptionFilter.getText()),
								prepareValue(adviceFilter.getText()),
								prepareValue(jcb.getItemAt(jcb.getSelectedIndex())));
        }

		listScrollPane = new JScrollPane(equationTable);
		listScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		equationPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel fillerPanel = new JPanel();
        fillerPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        equationPanel.add(fillerPanel, BorderLayout.SOUTH);

		return equationPanel;
	}

	private static void setupEquationDisplay(String equation, String description, String advice, String category) {
		// Get object attributes here & use results to populate table headers & body
		headers = getColumns();
		equationList = getEquations(prepareValue(equation), prepareValue(description), prepareValue(advice), prepareValue(category));

		equationModel = new DefaultTableModel(equationList, headers) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				if (column == 0) { return Integer.class; }

				return super.getColumnClass(column);
			}
		};
		equationModel.fireTableDataChanged();

		// Model View Controller pattern: Table MUST depend on table model for dynamic table refresh to work properly
		equationTable = new JTable(equationModel);

		equationModel.addTableModelListener(new MyTableModelListener(equationTable));

		JTableHeader equationHeader = equationTable.getTableHeader();
		Font equationfont = equationHeader.getFont();
		equationHeader.setFont(new Font(equationfont.getFontName(), Font.BOLD, equationfont.getSize()));

		// Allow selection of only 1 row at a time
		ListSelectionModel cellSelectionModel = equationTable.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Sort on column header feature here
		equationTable.setAutoCreateRowSorter(true);

		// Place button in 'Edit' column to enable insert/update/delete for user-created entries, otherwise show message "Cannot Edit"
		buttonCheck(equationTable);

		equationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				try {
					int whatToDo;
					int rowIndex = equationTable.getSelectedRow();
					int columnIndex = equationTable.getSelectedColumn();

					String editable = equationTable.getValueAt(rowIndex, 5).toString();

					if (editable.isEmpty() || editable.isBlank() || null == editable) {
						JOptionPane.showMessageDialog(new JButton(),
								"Cannot Edit:\n" + equationTable.getValueAt(rowIndex, 1),
								"Cannot Edit", JOptionPane.ERROR_MESSAGE);
					}
					else {
						if (columnIndex == 5) {
							if (editable.contains("Remove")) {
								Object[] options = {"Update", "Remove", "Cancel"};
								whatToDo = JOptionPane.showOptionDialog(null,
																		equationTable.getValueAt(rowIndex, 1),
																		"Update/Remove",
																		JOptionPane.YES_NO_CANCEL_OPTION,
																		JOptionPane.QUESTION_MESSAGE,
																		null,
																		options,
																		options[2]);
								//System.out.println("Choice: " + whatToDo);

								if (whatToDo == 0) {
									updateEquation(equationTable, equationTable.getSelectedRow());
								}
								else if (whatToDo == 1) {
									deleteEquation(equationTable.getValueAt(rowIndex, 0).toString());
									removeRow(rowIndex, equationTable, equationModel);
								}
							}
							else if (editable.contains("New")) {
								whatToDo = popupChoice(equationTable.getValueAt(rowIndex, 1), "New");

								if (whatToDo == 0) {
									Equation newEquation = createEquation(equationTable, equationTable.getSelectedRow());
									insertRow(newEquation, rowIndex, equationTable, equationModel);
								}
							}
						}
					}
				}
				catch (ArrayIndexOutOfBoundsException aioobe) {
					aioobe.printStackTrace();
				}
				catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			}
		});
	}

	// Popup Yes or No choice for current action
	private static int popupChoice(Object cellValue, String action) {
		int whatToDo = JOptionPane.showConfirmDialog(new JButton(),
													action + ":\n" + cellValue + "?",
													action, JOptionPane.YES_NO_OPTION);

		String choice = (whatToDo == 0) ? "Yes" : "No";
		return whatToDo;
	}

	// Remove any unnecessary characters from given string
	private static String cleanupString(String stringToClean) {
		return stringToClean.replace("{", "").replace("}",  "")
							.replace("[", "").replace("]",  "")
							.replace("\"", "").replace(",", "");
	}

	private static String prepareValue(String input) {
		System.out.print("prepareValue:: input: ~" + input + "~");
		// Default value is percent character because it's SQL wildcard
		String output = new String("%");

		try {
			// Encode space characters to ensure correct search results
			input = input.trim().replaceAll(" ", "%20");

			if ((input != null) && (!input.isEmpty()) && (!input.isBlank())) {
				// Input already encoded, so use it as is
				if ((input.contains("%")) && (input.length() > 1)) {
					output = input;
				}
				else {
					// Need to encode all non-alphanumeric characters, to ensure correct search results
					output = URLEncoder.encode(input, "UTF-8");
				}
			}
		}
		// Should be able to do nothing, since output value defaults to null
		catch (NullPointerException | UnsupportedEncodingException npe) {}

		System.out.println("prepareValue:: output: ~" + output + "~");
		return output;
	}

	private static void buttonCheck(JTable equationTable) {
		try {
			if (equationTable.getCellEditor() != null) {
				equationTable.getCellEditor().stopCellEditing();
			}

			for (int i = 0; i < equationTable.getRowCount(); i++) {
				for (int j = 0; j < equationTable.getColumnCount(); j++) {
					if (null != equationTable.getValueAt(i, j)) {
						String value = equationTable.getValueAt(i, j).toString();
						String name = equationTable.getColumnName(j);

						equationTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());

						if (name.equalsIgnoreCase("Edit")) {
							if (value.contains("true")) {
								equationTable.setValueAt("", i, j);
							}
							else if (value.contains("false")) {
								equationTable.setValueAt("Update/Remove Entry", i, j);
							}
							else if (value.contains(" ")) {
								equationTable.setValueAt("New Entry", i, j);
							}
						}
					}
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException aioobe) {
			aioobe.printStackTrace();
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	private static Long getNewId() {
		Long newId = null;

		try {
			// Set up connection
			URL url = new URL("http://localhost:8080/equations/newid");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			// Retrieve value
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String rl = br.readLine();
			newId = Long.parseLong(rl);

			// Check & report response
			if (conn.getResponseCode() != 200) {
			    throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return newId;
	}

	private static List<String> getAllCategories() {
		String[] ctgrs = new String[1];
		List<String> categories = new ArrayList<String>();

		try {
			// Set up connection
			URL url = new URL("http://localhost:8080/equationTypes/category/all");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			// Retrieve value
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String cats = br.readLine().replace("[", "").replace("]", "").replace("\"", "");
			ctgrs = cats.split("\\,");
			categories = Arrays.asList(ctgrs);

			// Check & report response
			if (conn.getResponseCode() != 200) {
			    throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return categories;
	}

	private static String getCategoryName(int categoryCode) {
		String ctgrNm = null;

		try {
			// Set up connection
			URL url = new URL("http://localhost:8080/equationTypes/category/code/" + categoryCode);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			// Retrieve value
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			ctgrNm = br.readLine();

			// Check & report response
			if (conn.getResponseCode() != 200) {
			    throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return ctgrNm;
	}

	private static Long getCategoryCode(String categoryName) {
		Long ctgrId = 0L;

		try {
			// Set up connection
			URL url = new URL("http://localhost:8080/equationTypes/category/name/" + prepareValue(cleanupString(categoryName)));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			// Retrieve value
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			ctgrId = Long.parseLong(cleanupString(br.readLine()));

			// Check & report response
			if (conn.getResponseCode() != 200) {
			    throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return ctgrId;
	}

	private static Equation createEquation(JTable equationTable, int selectedRow) {
		Equation newEquation = null;

		try {
			// Set up connection
			URL url = new URL("http://localhost:8080/equations/create");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setDoOutput(true);

			// Prepare item for insert
			// Add security check here (maybe character escaping), to prevent SQL injection
			String	formula		= (String)equationTable.getValueAt(selectedRow, 1),
					description	= (String)equationTable.getValueAt(selectedRow, 2),
					advice		= (String)equationTable.getValueAt(selectedRow, 3);
			Long	category	= getCategoryCode((String)equationTable.getValueAt(selectedRow, 4).toString().trim());
			Boolean	edit		= Boolean.parseBoolean((String) equationTable.getValueAt(selectedRow, 5).toString().trim());
			newEquation = new Equation(formula, description, advice, category, edit);
			newEquation.setId(getNewId());
			String equationJson = newEquation.EquationAsJson();

			// Perform insert
			OutputStream os = conn.getOutputStream();
			byte[] input = equationJson.getBytes("utf-8");
			os.write(input, 0, input.length);

			// Check & report response
			if (conn.getResponseCode() != 200) {
			    throw new RuntimeException("Failed: URL: ~"+ conn.getURL() +"~,\nHTTP Error code: ~" + conn.getResponseCode() + "~");
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return newEquation;
	}

	private static void insertRow(Equation newEquation, int rowIndex, JTable equationTable, DefaultTableModel equationModel) {
		// Create new row contents
		Vector<Object> vctr = new Vector<>();
		vctr.add(newEquation.getId());
		vctr.add(newEquation.getEquation());
		vctr.add(newEquation.getDescription());
		vctr.add(newEquation.getAdvice());
		vctr.add(newEquation.getCategory());
		vctr.add("Update / Remove Entry");

		// Insert row & signal insertion to table model
		equationModel.addRow(vctr);
		equationModel.fireTableRowsInserted(rowIndex, rowIndex);

		// Reset New Entry row
		for (int currentColumn=0; currentColumn<equationModel.getColumnCount()-1; currentColumn++) {
			equationModel.setValueAt(null, rowIndex, currentColumn);
		}

		// Use 'moveRow' here to reposition 'New Entry' row to below newly created row
		equationModel.moveRow(rowIndex, rowIndex, rowIndex+1);

		// Refresh table
		equationTable.validate();
		equationTable.repaint();
	}

	private static int updateEquation(JTable equationTable, int selectedRow) {
		int status = 0;
		Equation updatedEquation = null;

		try {
			// Prepare item for insert
			// Add security check here (maybe character escaping), to prevent SQL injection
			String	formula		= (String)equationTable.getValueAt(selectedRow, 1),
					description	= (String)equationTable.getValueAt(selectedRow, 2),
					advice		= (String)equationTable.getValueAt(selectedRow, 3);
			Long	id			= Long.parseLong(equationTable.getValueAt(selectedRow, 0).toString()),
					category	= getCategoryCode((String)equationTable.getValueAt(selectedRow, 4).toString().trim());
			Boolean	edit		= Boolean.parseBoolean((String) equationTable.getValueAt(selectedRow, 5).toString().trim());
			updatedEquation = new Equation(formula, description, advice, category, edit);
			updatedEquation.setId(id);
			String equationJson = updatedEquation.EquationAsJson();

			// Set up connection
			URL url = new URL("http://localhost:8080/equations/update/" + id);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setDoOutput(true);

			// Perform update
			OutputStream os = conn.getOutputStream();
			byte[] input = equationJson.getBytes("utf-8");
			os.write(input, 0, input.length);

			// Check & report response
			status = conn.getResponseCode();
			if (status != 200) {
			    throw new RuntimeException("Failed: URL: ~"+ conn.getURL() +"~,\nHTTP Error code: ~" + conn.getResponseCode() + "~");
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return status;
	}

	private static int deleteEquation(String equationId) {
		int status = 0;

		try {
			// Set up connection
			URL url = new URL("http://localhost:8080/equations/delete/" + equationId);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");

			// Perform delete
			conn.connect();

			// Check & report response
			status = conn.getResponseCode();
			if (status != 200) {
			    throw new RuntimeException("Failed: URL: ~"+ conn.getURL() +"~,\nHTTP Error code: ~" + conn.getResponseCode() + "~");
			}

			// Clean up connection
			conn.disconnect();
		}
		catch (MalformedURLException murle) {
			murle.printStackTrace();
		}
		catch (ProtocolException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return status;
	}

	private static void removeRow(int rowIndex, JTable equationTable, DefaultTableModel equationModel) {
		// Delete row & signal deletion to table model
		equationModel.removeRow(rowIndex);
		equationModel.fireTableRowsDeleted(rowIndex, rowIndex);

		// Refresh table
		equationTable.validate();
		equationTable.repaint();
	}
}
