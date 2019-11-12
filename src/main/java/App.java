/*
Evan Campbell

1000921278
*/

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
        // Constructs nodes with gathered information
        network.setup();
        // launches all the node windows and the main window
        network.load_gui();
    }
}
