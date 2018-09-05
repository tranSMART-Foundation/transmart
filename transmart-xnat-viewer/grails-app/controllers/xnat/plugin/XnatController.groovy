package xnat.plugin

import org.apache.http.HttpResponse
import org.transmart.xnat.XNATREST

import java.util.logging.Level
import java.util.logging.Logger

class XnatController {

    def ScanService;

    def download() {

        XNATREST xnat = new XNATREST((String) ScanService.getDomain(), (String) ScanService.getUsername(), (String) ScanService.getPassword());

        HttpResponse data = xnat.fetchData(params.url);

        InputStream input = null;
        OutputStream output = response.getOutputStream();
        byte[] buffer = new byte[1024];

        try {
            input = data.getEntity().getContent();

            for (int length; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
            }

        } catch (IOException ex) {
            Logger.getLogger(XNATREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(XNATREST.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException logOrIgnore) {
                }
            }
        }

    }

    def image() {
        XNATREST xnat = new XNATREST((String) ScanService.getDomain(), (String) ScanService.getUsername(), (String) ScanService.getPassword());

        HttpResponse data = xnat.fetchData(params.url);

        InputStream input = null;
        OutputStream output = response.getOutputStream();
        byte[] buffer = new byte[1024];

        try {
            input = data.getEntity().getContent();

            for (int length; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
            }

        } catch (IOException ex) {
            Logger.getLogger(XNATREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(XNATREST.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException logOrIgnore) {
                }
            }
        }
    }

    def info() {
        String line = "";
        XNATREST xnat = new XNATREST((String) ScanService.getDomain(), (String) ScanService.getUsername(), (String) ScanService.getPassword());

        HttpResponse data = xnat.fetchData(params.url);

        PrintWriter out = response.getWriter();
        InputStream input = data.getEntity().getContent();
        BufferedReader rd = new BufferedReader(new InputStreamReader(input));

        boolean escape = false;
        // Read response until the end
        while ((line = rd.readLine()) != null) {

            if (line.toLowerCase().contains("layout_content")) {
                escape = true;
            }

            if (line.toLowerCase().contains("mylogger")) {
                escape = false;
            }

            if (escape) {
                render line.trim();

            }

        }
    }
}