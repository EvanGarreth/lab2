import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class Network {
    // Maximum nodes in the graph
    final int MAX_NODES = 6;
    // Maximum links a single node can have
    final int MAX_LINKS = 4;

    // used to store the file contents on the heap for the second loop
    // since the file size is limited to 6 nodes and 4 links (as per project description,
    // storing it in memory isn't a concern
    ArrayList<String> file_lines = new ArrayList<String>();

    // used to store all the node ids in the graph. Needed before generating each node
    TreeSet<Integer> node_ids = new TreeSet<Integer>();

    // used to store all the nodes for running through a graph cycle
    TreeMap<Integer, Node> nodes = new TreeMap<Integer, Node>();

    // the number of cycles taken to reach a stable state
    int cycles = 0;

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

        this.load_gui();
    }

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

    public void next_cycle()
    {
        for (int node_id : this.node_ids)
        {
            // Get the node and check to see if it has an update to propogate to the other nodes
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

            // let the node know that its information has been propogated
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
        }
    }

    public void run_until_stable()
    {
        // Cycle until a stable state is reached
        while (!this.is_stable())
        {
            this.next_cycle();
            ++this.cycles;
        }

        System.out.printf("\n\n\nSteady State reached @ %d cycles\n", this.cycles);
    }

    public void load_gui()
    {
        for (int node_id : this.node_ids)
        {
            Node node = this.nodes.get(node_id);
            GUI node_gui = new GUI(node);

        }
    }
}
