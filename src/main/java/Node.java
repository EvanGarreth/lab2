import java.util.Collections;
import java.util.TreeSet;
import java.util.TreeMap;

public class Node {
    // as defined by project description
    private final int INF = 16;
    // the number given to this node from the parameter file
    private int id;

    // used to track all distance/value costs for all possible paths to each node
    private TreeMap<Integer, TreeMap<Integer, Integer>> dv_table;
    // current node to take when going to the destination node
    // (key, value) -> (dest, next node from this node)
    private TreeMap<Integer, Integer> routing_table;

    // keep track of all node ids. Needed when adding a new neighbor node
    private TreeSet<Integer> node_ids;

    // this Nodes own row in its dv_table (called a lot so storing it here)
    private TreeMap<Integer, Integer> dv_row;

    // whether or not another node has sent updated information to this one
    private boolean pending_update;
    // whether or not this node needs to update its neighbors
    private boolean update_to_send;

    public Node(Integer node_id, TreeSet<Integer> node_ids)
    {
        this.id = node_id;
        this.node_ids = node_ids;

        // Initialize empty graph
        this.dv_table = new TreeMap<Integer, TreeMap<Integer, Integer>>();
        // Add row for this node
        dv_table.put(id, new TreeMap<Integer, Integer>());
        // store the row for this node for easy retrieval
        this.dv_row = this.dv_table.get(this.id);

        // Initialize empty routing table
        this.routing_table = new TreeMap<Integer, Integer>();

        // add rows for each node to the dv_table
        // set the path to each node to INF, except for the path to the current node
        TreeMap<Integer, Integer> row = this.dv_row;
        for(Integer id : node_ids)
        {
            // distance to each node is infinity, except 0 to itself
            Integer val = (this.id == id) ? 0 : INF;
            row.put(id, val);

            // No next path (INF) is set for all nodes except this node
            // (best path to this node from this node is this node)
            Integer next_node = (this.id == id) ? this.id : INF;
            routing_table.put(id, next_node);
        }

        // there are no new updates at creation time
        this.pending_update = false;
        // however, the node needs to update the other nodes of its links provided by the file
        this.update_to_send = true;
    }

    public int get_id() { return this.id; };
    public boolean has_pending_update() { return pending_update; };
    public boolean has_update_to_send() { return update_to_send; };

    // Return a clone of the row so changes in other nodes don't affect this one
    public TreeMap<Integer, Integer> get_row() { return (TreeMap<Integer, Integer>) this.dv_row.clone(); };

    // called by the "network" on initialization to populating the empty graph with the links provided by the input file
    public void add_link(int neighbor, int cost)
    {
        // set cost from this node to the new neighbor
        this.dv_row.put(neighbor, cost);

        // insert new empty row for this neighbor in the graph
        dv_table.put(neighbor, new TreeMap<Integer, Integer>());

        // initialize the starting state for this row
        for(Integer id : this.node_ids)
        {
            // distance to each node is infinity, except 0 to itself
            Integer val = (id == neighbor) ? 0 : INF;
            TreeMap<Integer, Integer> row = dv_table.get(neighbor);
            row.put(id, val);
        }
    }

    // called by the "network" whenever pending_update is true for this node
    // performs the Bellman Ford algorithm to determine the best cost link
    public void bellman_ford()
    {
        // reset the update flag
        pending_update = false;

        // get the row for this node and get the keys to iterate upon
        TreeMap<Integer, Integer> this_row = this.dv_row;

        // loop through each neighbor, adjusting the costs based upon the new value(s)
        for(int dest_node_id : this.node_ids)
        {
            // used to store the new cost to each node
            TreeMap<Integer, Integer> updated_costs = new TreeMap<Integer, Integer>();

            // loop through all the nodes, calculating the cost from this node to the currently selected destination node
            for(Integer node_id : node_ids)
            {
                // current class node to inner loop node
                int this_to_id = this_row.get(node_id);

                TreeMap<Integer, Integer> node_id_row = dv_table.get(node_id);
                // inner loop node to dest_node, as seen by class node
                int node_id_to_neighbor = node_id_row.get(dest_node_id);

                int total_cost = this_to_id + node_id_to_neighbor;

                // we want to sort by the least cost node, so put total cost as the key and node_id as the value
                // TreeMaps are sorted by key values, so the lowest valued key will be first
                updated_costs.put(total_cost, node_id);
            }

            // TreeMaps are sorted by ascending key values, so the first entry is the minimum
            int min_cost = updated_costs.firstEntry().getKey();
            //int min_cost = Collections.min(updated_costs.keySet());
            int prev_min_cost = this_row.get(dest_node_id);

            // if a new minimum is found, update and raise the flag saying an update has occurred in this node
            if(min_cost < prev_min_cost)
            {
                this_row.put(dest_node_id, min_cost);
                update_to_send = true;
            }
        }
    }

    // called by the "network" when another node broadcasts an update to the table
    // replaces the stored row with the new one and marks an update has occurred, but
    // doesn't do any further computations until the next update cycle
    public void update_row(int source, TreeMap<Integer, Integer> val)
    {
        pending_update = true;
        dv_table.put(source, val);
    }

    // alert the node that its update has been propogated
    public void update_sent()
    {
        this.update_to_send = false;
    }

    // used for when the user manually updates the cost of this node to another
    // marks that an update has to be sent out to be triggered in the next cycle
    public void adjust_cost(int neighbor, int cost)
    {
        // adjust the link to the desired value and set the flag to notify other nodes of a change
        this.dv_row.put(neighbor, cost);
        this.update_to_send = true;
    }

    public void print()
    {
        System.out.printf("Node %d\n", this.id);
        String line = "  ";
        for(int did : this.dv_row.keySet())
        {
            line += String.format("%2d|", did);
        }
        line += "\n";

        for(int oid : this.dv_table.keySet())
        {
            line += oid + "|";
            for (int did : this.dv_row.keySet())
            {
                line += String.format("%2d|", dv_table.get(oid).get(did));
            }
            line += "\n";
        }
        System.out.println(line);
    }
}
