/*
Evan Campbell

1000921278
*/

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.TreeMap;
import java.util.Vector;

public class GUI extends JFrame implements TableModelListener
{
    //private JButton cycleButton;
    //private JButton stabilizeButton;
    private Node node;
    private Network network;
    private DefaultTableModel table_model;
    private JTable node_table;

    GUI(Node node, Network network)
    {
        this.node = node;
        this.network = network;

        recreate_table();

        this.setTitle(String.format("Node %d", node.get_id()));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void recreate_table()
    {
        this.table_model = new DefaultTableModel() {

            // don't allow the leftmost cells (colum 0) to be editable
            // every other cell should allow edits
            // this allows the user to see how changes propagate as per project description
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return (column != 0);
            }
        };

        TreeMap<Integer, Integer> row = node.get_row();
        this.table_model.addColumn("Node [" + this.node.get_id() + "]:");
        for (int node_id : row.keySet())
        {
            this.table_model.addColumn(String.format("%d", node_id));
        }

        TreeMap<Integer, TreeMap<Integer, Integer>> dv_table = node.get_dv_table();

        // loop  through each node's row in the table, getting the cost from that node to every other node n
        for (int node_n : dv_table.keySet())
        {
            Vector<Integer> row_data = new Vector<>();

            // add current node id to the leftmost column
            row_data.add(node_n);

            // Since this is a table can use the row from the previous loop since it will have the same order
            for (int node_y: row.keySet())
            {
                Integer cost = dv_table.get(node_n).get(node_y);
                row_data.add(cost);
            }
            this.table_model.addRow(row_data);
        }

        this.node_table = new JTable(this.table_model);

        this.add(new JScrollPane(this.node_table));
        /*this.node_table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int row = node_table.getSelectedRow();
                    int column = node_table.getSelectedColumn();
                    String column_name = node_table.getColumnName(column);

                    int dest_node = Integer.parseInt(column_name);
                    int source_node = (Integer) node_table.getValueAt(row, 0);
                    int cost = (Integer) node_table.getValueAt(row, column);
                    System.out.printf("Source: %d, Dest: %d, Cost: %d\n", source_node, dest_node, cost);

                    network.user_changed_link(source_node, dest_node, cost);
                }
            }

        });*/

        this.node_table.getModel().addTableModelListener(this);

        // Change the colors of the header and column 0 to denote that they are all headers
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        Color fg = Color.white;
        Color bg = Color.black;
        render.setForeground(fg);
        render.setBackground(bg);
        this.node_table.getColumnModel().getColumn(0).setCellRenderer(render);

        this.node_table.getTableHeader().setBackground(bg);
        this.node_table.getTableHeader().setForeground(fg);

        this.node_table.setRowSelectionAllowed(false);
    }

    // update the table whenever a node has computed new costs
    public void update_data()
    {
        this.node_table.getModel().removeTableModelListener(this);
        this.table_model.setRowCount(0);
        TreeMap<Integer, TreeMap<Integer, Integer>> dv_table = node.get_dv_table();
        TreeMap<Integer, Integer> row = node.get_row();

        // loop  through each node's row in the table, getting the cost from that node to every other node n
        for (int node_n : dv_table.keySet())
        {
            Vector<Integer> row_data = new Vector<>();

            // add current node id to the leftmost column
            row_data.add(node_n);

            // Since this is a table can use the row from the previous loop since it will have the same order
            for (int node_y: row.keySet())
            {
                Integer cost = dv_table.get(node_n).get(node_y);
                row_data.add(cost);
            }
            this.table_model.addRow(row_data);
        }
        this.node_table.getModel().addTableModelListener(this);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();

        String column_name = this.node_table.getColumnName(column);

        int dest_node = Integer.parseInt(column_name);
        int source_node = (Integer) this.node_table.getValueAt(row, 0);

        // user input is considered a string so convert to int
        int cost = Integer.parseInt((String) node_table.getValueAt(row, column));

        network.user_changed_link(source_node, dest_node, cost);
    }
}