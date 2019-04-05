package com.recomdata.transmart.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils

import java.nio.channels.Channels
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@CompileStatic
class FileDownloadService {

    static transactional = false

    String getFilename(String fileURIStr) {
        URI fileURI = new URI(fileURIStr)
        String filename = null
	if (StringUtils.equalsIgnoreCase('file', fileURI.scheme)) {
	    filename = new File(fileURI.toString()).name
        }
        else {
	    if (fileURI) {
		if (fileURIStr) {
                    int loc = fileURIStr.lastIndexOf('/')
                    if (loc == fileURIStr.length() - 1) {
                        loc = (fileURIStr.substring(0, loc - 1)).lastIndexOf('/')
                    }
                    filename = fileURIStr.substring(loc + 1, fileURIStr.length())
                }
            }
        }

	filename
    }

    void getFiles(List<String> fileURLs, String dirToDownloadTo) {
	int nThreads = fileURLs?.size()
	if (!nThreads) {
	    return
	}

	ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads)

	for (url in fileURLs) {
	    pool.submit new FileDownload(url, getFilename(url), dirToDownloadTo)
	}

        // Wait for the poolclose when all threads are completed
	while (pool.activeCount > 0)
	    pool.shutdown()
    }
}

@CompileStatic
@Slf4j('logger')
class FileDownload extends Thread {
    private String fileURI
    private String filename
    private String fileContainerDir

    FileDownload(String uri, String name, String dir) {
	fileURI = uri
	filename = name
	fileContainerDir = dir
    }

    void run() {
	if (!fileURI) {
	    return
	}
        FileOutputStream fos = null
        try {
	    URL fileURL = new URL(fileURI)
	    fos = new FileOutputStream(new File(fileContainerDir, filename))
	    fos.channel.transferFrom Channels.newChannel(fileURL.openStream()), 0, 1 << 24
        }
        catch (MalformedURLException e) {
	    logger.error 'Invalid File URL', e
        }
        catch (IOException e) {
	    logger.error 'IO failure during file download', e
        }
        finally {
            try {
		fos?.close()
            }
	    catch (ignored) {}
        }
    }
}
