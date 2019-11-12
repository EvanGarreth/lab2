/*
Evan Campbell

1000921278
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class Network {
    // Maximum nodes in the graph
    final int MAX_NODES = 6;
    // Maximum links a single node can have
    final int MAX_LINKS = 4;

    private TreeMap<Integer, GUI> node_guis = new TreeMap<Integer, GUI>();

    // used to store the file contents on the heap for the second loop
    // since the file size is limited to 6 nodes and 4 links (as per project description,
    // storing it in memory isn't a concern
    private ArrayList<String> file_lines = new ArrayList<String>();

    // used to store all the node ids in the graph. Needed before generating each node
    private TreeSet<Integer> node_ids = new TreeSet<Integer>();

    // used to store all the nodes for running through a graph cycle
    private TreeMap<Integer, Node> nodes = new TreeMap<Integer, Node>();

    private JFrame main_gui;
    private JLabel cycle_label;
    private JButton cycle;
    private JButton run_to_stable;

    // the number of cycles taken to reach a stable state
    private int cycles = 0;

    // when we started to go from a non stable to stable state
    private int start_cycle = 0;

    void load_file(String filename)
    {
        ClassLoader classLoader = App.class.getClassLoader();
        URL resource = classLoader.getResource(filename);

        System.out.printf("Opening file %s\n", filename);
        File file = new File(resource.getFile());
        try
        {
            FileReader in = new FileReader(file);
            BufferedReader reader = new BufferedReader(in);
            String line = null;

            while ((line = reader.readLine()) != null)
            {
                // append the line for node generation after
                this.file_lines.add(line);

                // each line is formatted as "source dest cost"
                String[] parsed = line.split(" ");

                Integer source = Integer.parseInt(parsed[0]);
                Integer dest = Integer.parseInt(parsed[1]);

                // since this is a set, can blindly add nodes here without worrying about duplicates
                this.node_ids.add(source);
                this.node_ids.add(dest);
            }

            reader.close();
            in.close();
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }

    // perform setup steps such as initializing needed vars and creating nodes with links
    public void setup()
    {
        // loop through node ids, creating a new node and adding to the node list
        for(int id : node_ids)
        {
            // create a new node entry for this id
            Node new_node = new Node(id, this.node_ids);
            this.nodes.put(id, new_node);
        }

        // loop through the file again, adding the links between the nodes
        for(String line : this.file_lines)
        {
            String[] parsed = line.split(" ");
            int source = Integer.parseInt(parsed[0]);
            int dest = Integer.parseInt(parsed[1]);
            int cost = Integer.parseInt(parsed[2]);

            Node source_node = this.nodes.get(source);
            Node dest_node = this.nodes.get(dest);

            source_node.add_link(dest, cost);
            dest_node.add_link(source, cost);
        }
    }

    // alerts the program to whether or not the nodes are in a stable state
    public boolean is_stable()
    {
        for(int node_id : this.node_ids)
        {
            Node node = this.nodes.get(node_id);
            if (node.has_update_to_send())
            {
                return false;
            }
        }
        return true;
    }

    // ticks the next cycle of the network. Sends out updates to relevant nodes and performs the DV algorithm
    public void next_cycle()
    {
        for (int node_id : this.node_ids)
        {
            // Get the node and check to see if it has an update to propagate to the other nodes
            Node node = this.nodes.get(node_id);
            if (!node.has_update_to_send())
            {
                continue;
            }

            // If an update exists, get the relevant row from the node and send it to every other node
            TreeMap<Integer, Integer> node_row = node.get_row();
            for (int id_to_update : this.node_ids)
            {
                Node node_to_update = this.nodes.get(id_to_update);
                node_to_update.update_row(node_id, node_row);
            }

            // let the node know that its information has been propagated
            node.update_sent();
        }

        // loop again through all the nodes,
        // this time running the distance vector algorithm to check for new shortest routes
        for (int node_id : this.node_ids)
        {
            Node node = this.nodes.get(node_id);
            if(!node.has_pending_update())
            {
                continue;
            }

            // Bellman-Ford is the most popular DV algorithm
            node.bellman_ford();
            this.node_guis.get(node_id).update_data();
        }

        ++this.cycles;

        if(is_stable())
        {
            int num_cycles = this.cycles - this.start_cycle;

            // Disable the cycle buttons and tell the user how many cycles it took to reach the steady state
            System.out.printf("\n\n\nStable State reached at cycle #%d, took %d cycles", this.cycles, num_cycles);
            this.cycle_label.setText(String.format("Stable State reached at cycle #%d, took %d cycles", this.cycles, num_cycles));
            this.cycle_label.setForeground(Color.red);
            this.cycle.setEnabled(false);
            this.run_to_stable.setEnabled(false);
        }
        else
        {
            this.cycle_label.setText(String.format("Not stable state, at cycle #%d", this.cycles));
        }
    }

    // loads each node's window along with the main window with the action buttons
    public void load_gui()
    {
        // init gui for each node
        for (int node_id : this.node_ids)
        {
            Node node = this.nodes.get(node_id);
            GUI node_gui = new GUI(node, this);
            this.node_guis.put(node_id, node_gui);
        }

        // create the master node with buttons for cycling and running until stable state is reached
        this.main_gui = new JFrame("Master Node");
        this.main_gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.main_gui.setSize(150, 150);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        this.cycle_label = new JLabel(String.format("Not stable state, at cycle #%d", this.cycles));

        // add cycle button and relevant callback
        this.cycle = new JButton("Next Cycle");
        this.cycle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                next_cycle();
            }
        });

        // add stable state button and relevant callback
        this.run_to_stable = new JButton("Run until Stable State");
        this.run_to_stable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Cycle until a stable state is reached
                while (!is_stable())
                {
                    next_cycle();
                }
            }
        });

        panel.add(this.cycle_label); // Components Added using Flow Layout
        //panel.add(label); // Components Added using Flow Layout
        panel.add(this.cycle);
        panel.add(this.run_to_stable);

        //Adding Components to the frame.
        this.main_gui.getContentPane().add(BorderLayout.SOUTH, panel);
        this.main_gui.setVisible(true);
    }

    // called by the GUI whenever a user enters a new value into a cell. Alerts the relevant nodes
    public void user_changed_link(int source_node, int dest_node, int cost)
    {
        Node source = this.nodes.get(source_node);
        Node dest = this.nodes.get(dest_node);

        if(is_stable())
        {
            // Reenable the cycle buttons
            System.out.printf("\n\n\nStable State ended @ %d cycles\n", this.cycles);
            this.cycle_label.setText(String.format("Not stable state, at cycle #%d", this.cycles));
            this.cycle_label.setForeground(Color.black);
            this.cycle.setEnabled(true);
            this.run_to_stable.setEnabled(true);

            this.start_cycle = this.cycles;
        }

        source.adjust_cost(dest_node, cost);
        dest.adjust_cost(source_node, cost);
        System.out.println("change detected!");
    }
}
