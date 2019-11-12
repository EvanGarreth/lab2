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

    GUI(Node node)
    {
        DefaultTableModel table_model = new DefaultTableModel();

        TreeMap<Integer, Integer> row = node.get_row();
        table_model.addColumn("Node:");
        for (int node_id : row.keySet())
        {
            table_model.addColumn(String.format("%d", node_id));
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
            table_model.addRow(row_data);
        }

        JTable node_table = new JTable(table_model) {
            private static final long serialVersionUID = 1L;

            // don't allow the leftmost cells (colum 0) to be editable
            // every other cell should allow edits
            // this allows the user to see how changes propogate as per project description
            public boolean isCellEditable(int row, int column) {                
                return (column == 0);               
            }
        };
    
        this.add(new JScrollPane(node_table));
        //node_table.setName();
        node_table.getModel().addTableModelListener(this);

        // Change the colors of the header and column 0 to denote that they are all headers
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        Color fg = Color.white;
        Color bg = Color.black;
        render.setForeground(fg);
        render.setBackground(bg);
        node_table.getColumnModel().getColumn(0).setCellRenderer(render);

        node_table.getTableHeader().setBackground(bg);
        node_table.getTableHeader().setForeground(fg);

        this.setTitle(String.format("Node %d", node.get_id()));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
    }
}