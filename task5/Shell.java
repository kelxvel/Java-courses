import java.io.*;
import java.util.*;
import java.util.regex.*;
import javafx.util.Pair;
import java.nio.file.*;

public class Shell {
    public static final String initPath = System.getProperty("user.dir");
    public static final String homePath = System.getProperty("user.home");
    private File currentDir = new File(initPath);

    private PrintWriter err;

    private final InputStream is;
    private final OutputStream os;
    private final OutputStream eos;

    public static final String lastResult = "?";
    private Map<String, String> vars = new HashMap<>();

    public Shell(InputStream is, OutputStream os, OutputStream eos) throws Exception {
        this.is = is;
        this.os = os;
        this.eos = eos;

        err = new PrintWriter(eos, true);

        vars.put(lastResult, "");
    }

    public String getCurrentDirPath() {
        String path = currentDir.getAbsolutePath();
        if (path.startsWith(homePath))
            path = path.replaceFirst(homePath, "~");
        return path;
    }

    public void run(String str) {
        try {
            Parser parser = new Parser();
            for (Pair<Command, String> p : parser.parse(str)) {
                Command cmd = p.getKey();
                String param = p.getValue();
                int result = cmd.perform(param);
                vars.put(lastResult, Integer.toString(result));
                if (result != 0)
                    return;
            }
        } catch (Exception e) {
            err.println(e.getMessage());
        }
    }

    public class Parser {
        private List<Pair<Command, String>> list = new ArrayList<>();

        public List<Pair<Command, String>> parse(String str) throws Exception {
            list.clear();
            str = str.trim();
            vars.putAll(parseVars(str));
            str = substituteVars(str, vars);

            List<Pair<Command, String>> pipes = new ArrayList<>();
            Piper pipe = new Piper();
            String[] args = str.split(" && ");
            for (String a : args) {
                String[] pa = a.split(" \\| ");
                if (pa.length == 1)
                    list.add(parseCommand(pa[0]));
                else {
                    for (String p : pa)
                        pipes.add(parseCommand(p));
                }
                for (int i = 0; i < pipes.size() - 1; i++) {
                    Pair<Command, String> lhs = pipes.get(i);
                    Pair<Command, String> rhs = pipes.get(i+1);
                    pipe.pipe(lhs, rhs);
                }
                list.addAll(pipes);
                pipes.clear();
            }
            return list;
        }

        public Pair<Command, String> parseCommand(String str) throws Exception {
            if (str == null)
                return new Pair<>(Command.NULL, null);
            String[] args = str.trim().split(" ", 2);

            String scmd = args[0];
            Command cmd = CommandFactory.newCommand(Shell.this, scmd);

            String param = (args.length == 2) ? args[1] : null;
            if (param != null) {
                Redirector r = new Redirector();
                for (String s : param.split(" ")) {
                    if (s.contains(">>")) {
                        String from = s.substring(0, s.indexOf(">>"));
                        String to = s.substring(s.indexOf(">>") + 2);
                        r.redirect(cmd, from, to, true);
                        param = param.replace(s, "").trim();
                    } else if (s.contains(">")) {
                        String from = s.substring(0, s.indexOf(">"));
                        String to = s.substring(s.indexOf(">") + 1);
                        r.redirect(cmd, from, to, false);
                        param = param.replace(s, "").trim();
                    }
                }
                param = param.isEmpty() ? null : param;
            }
            return new Pair<>(cmd, param);
        }

        public Map<String, String> parseVars(String str) {
            Map<String, String> vars = new HashMap<>();
            for (String s : str.split(" ")) {
                if (s.contains("=")) {
                    String var = s.substring(0, s.indexOf('='));
                    String value = s.substring(s.indexOf('=') + 1);
                    vars.put(var, value);
                }
            }
            return vars;
        }

        public String substituteVars(String str, Map<String, String> vars) {
            StringBuilder sb = new StringBuilder();
            for (String s : str.split(" ")) {
                if (s.contains("$")) {
                    String key = s.replace("$", "");
                    if (!vars.containsKey(key))
                        continue;
                    s = vars.get(key);
                }
                sb.append(" " + s);
            }
            return sb.toString().trim();
        }

    }

    /**
     * Change the current directory.
     */
    public class Cd extends Command {
        public Cd() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Cd(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override public int perform(String path) throws Exception {
            File dir;
            if (path == null)
                dir = new File(homePath);
            else
                dir = path.startsWith("/") ? new File(path) : new File(currentDir, path);

            if (!dir.exists()) {
                err.println("cd: " + path + ": No such file or directory");
                return 1;
            }
            if (!dir.isDirectory()) {
                err.println("cd:" + path + ": Not a directory");
                return 2;
            }
            currentDir = dir;
            return 0;
        }
    }

    /**
     * List information about the files
     */
    public class Ls extends Command {
        public Ls() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Ls(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override public int perform(String param) throws Exception {
            out = new PrintWriter(os, true);
            boolean printHidden = false;
            if (param == null) {
                for (File file : currentDir.listFiles())
                    printFileName(file, printHidden);
                return 0;
            }

            String[] args = param.split(" ");
            for (String path : args) {
                File dir = path.startsWith("/") ? new File(path) : new File(currentDir, path);
                if (!dir.exists()) {
                    err.println("ls: cannot access " + path + ": No such file or directory");
                    return 1;
                }

                if (dir.isDirectory()) {
                    if (args.length > 1)
                        out.println(dir.getName() + ":");
                    for (File file : dir.listFiles()) {
                        printFileName(file, printHidden);
                    }
                } else {
                    out.println(dir.getName()); // print even if a file is hidden
                }
            }
            closeInputStream();
            closeOutputStream();
            return 0;
        }

        private void printFileName(File file, boolean printHidden) {
            if (file.isHidden() && !printHidden)
                return;
            out.println(file.getName());
        }
    }

    /**
     * Print the absolute pathname of current directory.
     */
    public class Pwd extends Command {
        public Pwd() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Pwd(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override public int perform(String param) throws Exception {
            out = new PrintWriter(os, true);
            out.println(currentDir.getAbsolutePath());
            closeOutputStream();
            return 0;
        }
    }

    /**
     * Summarize disk usage of each file, recursively for directories.
     */
    public class Du extends Command {
        public Du() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Du(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        private Set<String> dirs = new TreeSet<>();

        /**
         * Print apparent size of directory (as du with key '--apparent-size' in bash).
         */
        @Override public int perform(String param) throws Exception {
            File dir = (param == null) ? currentDir : new File(currentDir, param);
            printDirSize(dir);
            return 0;
        }

        private void printDirSize(File file) throws IOException {
            String path = file.getPath();
            if (dirs.contains(path))
                return;

            File[] fs = file.listFiles();
            if (fs != null) {
                for (File f : fs)
                    if (f.isDirectory())
                        printDirSize(f);
            }

            long size = (long) Math.round(getDirSize(file));
            out.printf("%-10d %s\n", size, path);
            dirs.add(path);
        }

        private double getDirSize(File file) {
            File[] fs = file.listFiles();
            double size = 4;
            if (fs != null) {
                for (File f : fs) {
                    if (f.isFile())
                        size += f.length() / 1024.0;
                    else
                        size += getDirSize(f);
                }
            }
            return size;
        }
    }

    /**
     * Report file system disk space usage
     */
    public class Df extends Command {
        public Df() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Df(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override
        public int perform(String param) throws Exception {
            Set<String> mountPoints = new HashSet<>();
            for (FileStore fs : FileSystems.getDefault().getFileStores()) {
                String fstr = fs.toString();
                String mp = fstr.substring(0, fstr.indexOf('(')).trim();
                mountPoints.add(mp);
            }
            if (param != null && !mountPoints.contains(param)) {
                err.println("df: '" + param + "': No such file or directory");
                return 1;
            }

            out.printf("Filesystem\t1K-blocks      Used Available Use%% Mounted on\n");
            for (FileStore fs : FileSystems.getDefault().getFileStores()) {
                String fstr = fs.toString();
                String mountPoint = fstr.substring(0, fstr.indexOf('(')).trim();

                if (param != null && !mountPoint.equals(param))
                    continue;

                String filesystem = fs.name();

                long total = fs.getTotalSpace() / 1024,
                        used = total - (fs.getUnallocatedSpace() / 1024),
                        available = fs.getUsableSpace() / 1024,
                        usePercent = (long) ((used / (double) total) * 100);
                if (used == 0 && available == 0)
                    continue;

                out.printf("%-10s\t%9d %9d %9d %3d%% %s\n", filesystem, total, used, available, usePercent, mountPoint);
            }
            return 0;
        }
    }

    /**
     * Print the first 10 lines of each file.
     */
    public class Head extends Command {
        public Head() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Head(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override public int perform(String path) throws IOException {
            if (path == null) {
                for (int i = 0; in.hasNextLine() && i < 10; i++) {
                    out.println(in.nextLine());
                }
                return 0;
            }

            for (String p : path.split(" ")) {
                File file = p.startsWith("/") ? new File(p) : new File(currentDir, p);

                if (!file.exists()) {
                    err.println("head: cannot open '" + p + "' for reading: No such file or directory");
                    return 1;
                }
                if (!file.canRead()) {
                    err.println("head: cannot open '" + p + "' for reading: Permission denied");
                    return 2;
                }

                try (Scanner scanner = new Scanner(file)) {
                    for (int i = 0; scanner.hasNextLine() && i < 10; i++) {
                        out.println(scanner.nextLine());
                    }
                }
            }
            return 0;
        }
    }

    /**
     * Print the last 10 lines of each file.
     */
    public class Tail extends Command {
        public Tail() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Tail(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override public int perform(String path) throws IOException {
            Queue<String> lines = new ArrayDeque<>(10);
            if (path != null) {
                for (String p : path.split(" ")) {
                    File file = p.startsWith("/") ? new File(p) : new File(currentDir, p);

                    if (!file.exists()) {
                        err.println("tail: cannot open '" + p + "' for reading: No such file or directory");
                        return 1;
                    }
                    if (!file.canRead()) {
                        err.println("tail: cannot open '" + p + "' for reading: Permission denied");
                        return 2;
                    }

                    try (Scanner scanner = new Scanner(file)) {
                        while (scanner.hasNextLine()) {
                            if (lines.size() > 10)
                                lines.poll();
                            lines.offer(scanner.nextLine());
                        }
                    }

                    for (String s : lines)
                        out.println(s);
                }
            } else  {
                while (in.hasNextLine()) {
                    if (lines.size() > 10)
                        lines.poll();
                    lines.offer(in.nextLine());
                }
                for (String s : lines)
                    out.println(s);
                return 0;
            }
            return 0;
        }
    }

    /**
     * Print the matching lines.
     */
    public class Grep extends Command {
        public Grep() {
            super(Shell.this.is, Shell.this.os, Shell.this.eos);
        }

        public Grep(InputStream is, OutputStream os, OutputStream eos) {
            super(is, os, eos);
        }

        @Override public int perform(String str) throws IOException {
            in = new Scanner(is);
            out = new PrintWriter(os, true);
            if (str == null || str.isEmpty()) {
                err.println("Usage: grep PATTERN [FILE]...");
                return 1;
            }

            Pattern p = Pattern.compile("\\\'(.*?)\\\'|\\\"(.*?)\\\"");
            Matcher m = p.matcher(str);

            String pattern = null;
            String pathes = null;

            if (m.find()) {
                pattern = m.group();
                pathes = str.replaceFirst(pattern, "").trim();
                pattern = pattern.replaceAll("\'", "").replaceAll("\"", "");
                out.println(pattern);
            }
            else {
                String[] args = str.split(" ", 2);
                pattern = args[0];
                pathes = (args.length == 2) ? args[1] : null;
            }

            if (pathes != null) {
                for (String path : pathes.split(" ")) {
                    File file = path.startsWith("/") ? new File(path) : new File(currentDir, path);
                    if (!file.exists()) {
                        err.println("grep: " + path + ": No such file or directory");
                        return 2;
                    }
                    if (file.isDirectory()) {
                        err.println("grep: " + path + ": Is a directory");
                        return 3;
                    }

                    try (Scanner s = new Scanner(file)) {
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            if (line.contains(pattern))
                                out.println(line);
                        }
                    }
                }
            } else {
                while (in.hasNextLine()) {
                    String line = in.nextLine();
                    if (line.contains(pattern))
                        out.println(line);
                }
            }
            closeInputStream();
            closeOutputStream();
            return 0;
        }
    }


    public class Piper {
        private final String bufferPath = homePath + "/.shell_buffer.tmp";
        private boolean bufferFlag = true;

        private File buffer1 = new File(bufferPath + "1");
        private File buffer2 = new File(bufferPath + "2");
        {
            try {
                buffer1.createNewFile();
                buffer2.createNewFile();
            } catch (Exception e) {}
        }

        public int pipe(Pair<Command, String> lhs,
                        Pair<Command, String> rhs) throws IOException {
            Command lCmd = lhs.getKey();
            Command rCmd = rhs.getKey();

            File buffer = (bufferFlag == true) ? buffer1 : buffer2;

            buffer.delete();
            buffer.createNewFile();

            OutputStream os = new FileOutputStream(buffer);
            InputStream is = new FileInputStream(buffer);

            lCmd.setOutputStream(os);
            rCmd.setInputStream(is);

            bufferFlag = !bufferFlag;
            return 0;
        }
    }

    public class Redirector {
        public void redirect(Command cmd, String from, String to, boolean append) throws IOException {
            if (from.equals("&") && to.startsWith("&"))
                throw new IOException("shell: syntax error near unexpected token `&'");

            OutputStream os = null;
            switch (to) {
                case "&":
                    throw new IOException("shell: syntax error near unexpected token `newline'");
                case "&1":
                    os = cmd.getOutputStream();
                    break;
                case "&2":
                    os = cmd.getErrorOutputStream();
                    break;
                default:
                    File file = new File(currentDir, to);
                    os = (append == true) ? new FileOutputStream(file, true) : new FileOutputStream(file);
            }

            switch (from) {
                case "1":
                    cmd.setOutputStream(os);
                    break;
                case "2":
                    cmd.setErrorOutputStream(os);
                    break;
                case "&":
                    cmd.setOutputStream(os);
                    cmd.setErrorOutputStream(os);
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Shell shell = new Shell(System.in, System.out, System.err);
        Scanner in = new Scanner(System.in);

        String username = System.getProperty("user.name");
        String hostname = null;
        try {
            hostname = "@" + java.net.InetAddress.getLocalHost().getHostName();
        } catch(java.net.UnknownHostException e) {
            hostname = "";
        }
        System.out.printf("%s%s:%s> ", username, hostname, shell.getCurrentDirPath());
        while (in.hasNextLine()) {
            shell.run(in.nextLine());
            System.out.printf("%s%s:%s> ", username, hostname, shell.getCurrentDirPath());
        }
    }
}