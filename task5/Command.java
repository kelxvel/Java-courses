import java.io.*;
import java.util.Scanner;

public abstract class Command {
    protected Scanner in;
    protected PrintWriter out;
    protected PrintWriter err;

    protected InputStream is;
    protected OutputStream os;
    protected OutputStream eos;

    public static final Command NULL = new Command(System.in, System.out, System.err) {
        @Override public int perform(String param) throws Exception {
            return 0;
        }
    };

    public Command(InputStream is, OutputStream os, OutputStream eos) {
        setInputStream(is);
        setOutputStream(os);
        setErrorOutputStream(eos);
    }

    abstract public int perform(String str) throws Exception;

    public void setInputStream(InputStream is) {
        closeInputStream();
        this.is = is;
        in = new Scanner(is);
    }

    public void setOutputStream(OutputStream os) {
        closeOutputStream();
        this.os = os;
        out = new PrintWriter(os, true);
    }

    public void setErrorOutputStream(OutputStream eos) {
        closeErrorOutputStream();
        this.eos = eos;
        err = new PrintWriter(eos, true);
    }

    public InputStream getInputStream() { return is; }
    public OutputStream getOutputStream() { return os; }
    public OutputStream getErrorOutputStream() { return eos; }

    public void closeInputStream() {
        try {
            if (is != null && !is.equals(System.in))
                is.close();
        } catch (Exception e) { }
    }

    public void closeOutputStream() {
        try {
            if (os != null && !os.equals(System.out) && !os.equals(System.err))
                os.close();
        } catch (Exception e) {}
    }

    public void closeErrorOutputStream() {
        try {
            if (eos != null && !eos.equals(System.err))
                eos.close();
        } catch (Exception e) {}
    }
}


class CommandFactory {
    public static Command newCommand(Shell shell, String cmd) throws Exception {
        Command command = Command.NULL;
        switch (cmd) {
            case "cd":
                command = shell.new Cd();
                break;
            case "ls":
                command = shell.new Ls();
                break;
            case "pwd":
                command = shell.new Pwd();
                break;
            case "df":
                command = shell.new Df();
                break;
            case "du":
                command = shell.new Du();
                break;
            case "head":
                command = shell.new Head();
                break;
            case "tail":
                command = shell.new Tail();
                break;
            case "grep":
                command = shell.new Grep();
                break;
            default:
                throw new Exception(cmd + ": command not found");
        }
        return command;
    }
}