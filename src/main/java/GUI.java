import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class GUI extends JFrame implements TableModelListener
{
    //private JButton cycleButton;
    //private JButton stabilizeButton;

    GUI(Node node)
    {
        DefaultTableModel table_model = new DefaultTableModel();

        TreeMap<Integer, Integer> row = node.get_row();
        for (int node_id : row.keySet())
        {
            table_model.addColumn(String.format("%d", node_id));
        }

        TreeMap<Integer, TreeMap<Integer, Integer>> dv_table = node.get_dv_table();

        // loop  through each node's row in the table, getting the cost from that node to every other node n
        for (int node_n : dv_table.keySet())
        {
            Vector<Integer> row_data = new Vector<>();
            // Since this is a table can use the row from the previous loop since it will have the same order
            for (int node_y: row.keySet())
            {
                Integer cost = dv_table.get(node_n).get(node_y);
                row_data.add(cost);
            }
            table_model.addRow(row_data);
        }
        JTable node_table = new JTable(table_model);
        this.add(new JScrollPane(node_table));
        //node_table.setName();
        node_table.getModel().addTableModelListener(this);

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