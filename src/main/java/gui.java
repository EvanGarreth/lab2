import javax.swing.*;
import java.util.Vector;

public class gui {
    private int num_nodes;
    private Vector<JTable> node_tables;
    private JButton cycleButton;
    private JButton stabilizeButton;

    gui(int nodes)
    {
        this.num_nodes = nodes;
        for (int i = 0; i < num_nodes; i++)
        {
            JTable node_table = new JTable();
            node_table.setName(String.format("Node %d", i));
            for (int j = 0; j < num_nodes; j++)
            {
                node_table.addColumn();
            }
            node_tables.add(node_table);
        }
    }
}
