import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;


public class App
{
    public static void main( String[] args )
    {
        Network network = new Network();

        // load the user provided file or use the default
        String filename;
        if (args.length != 1)
        {
            System.out.println("No file provided for input, using default \"sampleparams.txt\"");
            filename =  "sampleparams.txt";
        }
        else
        {
            filename = args[0];
        }

        // reads the file and populates file_lines and node_ids
        network.load_file(filename);
        network.setup();
        //network.run_until_stable();
    }
}
