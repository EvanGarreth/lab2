import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.Color;
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

        this.table_model = new DefaultTableModel() {

            // don't allow the leftmost cells (colum 0) to be editable
            // every other cell should allow edits
            // this allows the user to see how changes propogate as per project description
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return (column != 0);
            }
        };

        TreeMap<Integer, Integer> row = node.get_row();
        this.table_model.addColumn("Node:");
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
        //node_table.setName();
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

        this.setTitle(String.format("Node %d", node.get_id()));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();

        // dest node is the column header, source node is the object in column 0 of the row, and value is the cell edited
        String column_name = model.getColumnName(column);
        int dest_node = Integer.parseInt(column_name);
        int source_node = (Integer) model.getValueAt(row, 0);
        int cost = (Integer) model.getValueAt(row, column);

        network.user_changed_link(source_node, dest_node, cost);
    }

    public void update_data()
    {
        TreeMap<Integer, Integer> row = node.get_row();
        TreeMap<Integer, TreeMap<Integer, Integer>> dv_table = node.get_dv_table();

        // loop  through each node's row in the table, getting the cost from that node to every other node n
        int i = 0;
        int j = 1;
        for (int node_n : dv_table.keySet())
        {
            // Since this is a table can use the row from the previous loop since it will have the same order
            for (int node_y: row.keySet())
            {
                Integer cost = dv_table.get(node_n).get(node_y);
                this.table_model.setValueAt(cost, i, j);
                j++;
            }
            i++;
            j = 1;
        }
    }
}