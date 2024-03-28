import java.util.List;
import java.util.Map;

public class MyPojo {
    private Map<String, Database> databases;

    public Map<String, Database> getDatabases() {
        return databases;
    }

    public void setDatabases(Map<String, Database> databases) {
        this.databases = databases;
    }

    public static class Database {
        private Map<String, List<String>> tables;
        private String serverURL;
        private String Region;
        private String uname;
        private String pass;

        public Map<String, List<String>> getTables() {
            return tables;
        }

        public void setTables(Map<String, List<String>> tables) {
            this.tables = tables;
        }

        public String getServerURL() {
            return serverURL;
        }

        public void setServerURL(String serverURL) {
            this.serverURL = serverURL;
        }

        public String getRegion() {
            return Region;
        }

        public void setRegion(String region) {
            Region = region;
        }

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }
    }
}