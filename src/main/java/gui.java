import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.TreeSet;
import java.util.Vector;

public class gui {
    private int num_nodes;
    private Vector<JTable> node_tables;
    private JButton cycleButton;
    private JButton stabilizeButton;
    //private data;

    gui(TreeSet<Node> nodes)
    {
        this.num_nodes = nodes.size();
        for (int i = 0; i < num_nodes; i++)
        {
            DefaultTableModel table_model = new DefaultTableModel();
            for (int j = 0; j < num_nodes; j++)
            {
                table_model.addColumn(String.format("%d", i));
            }

            JTable node_table = new JTable(table_model);
            node_table.setName(String.format("Node %d", i));
            node_tables.add(node_table);
        }
    }
}
