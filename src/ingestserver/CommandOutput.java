package ingestserver;

/**
 * Ecapsulates the stderr and stdout of a shell command.
 * 
 * @author rombus
 */
public class CommandOutput {
    public String stderr;
    public String stdout;
    
    public CommandOutput(String stdout, String stderr){
        this.stdout = stdout;
        this.stderr = stderr;
    }
}
