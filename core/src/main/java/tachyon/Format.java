package tachyon;

import java.io.IOException;

import tachyon.conf.CommonConf;
import tachyon.conf.MasterConf;
import tachyon.conf.WorkerConf;
import tachyon.util.CommonUtils;

/**
 * Format Tachyon File System.
 */
public class Format {
  private static final String USAGE = "java -cp target/tachyon-" + Version.VERSION
      + "-jar-with-dependencies.jar tachyon.Format <MASTER/WORKER>";

  private static boolean formatFolder(String name, String folder) throws IOException {
    UnderFileSystem ufs = UnderFileSystem.get(folder);
    System.out.println("Formatting " + name + ": " + folder);
    if (ufs.exists(folder) && !ufs.delete(folder, true)) {
      System.out.println("Failed to remove " + name + ": " + folder);
      return false;
    }
    if (!ufs.mkdirs(folder, true)) {
      System.out.println("Failed to create " + name + ": " + folder);
      return false;
    }
    return true;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println(USAGE);
      System.exit(-1);
    }

    if (args[0].toUpperCase().equals("MASTER")) {
      MasterConf masterConf = MasterConf.get();

      if (!formatFolder("JOURNAL_FOLDER", masterConf.JOURNAL_FOLDER)) {
        System.exit(-1);
      }

      CommonConf commonConf = CommonConf.get();
      if (!formatFolder("UNDERFS_DATA_FOLDER", commonConf.UNDERFS_DATA_FOLDER)
          || !formatFolder("UNDERFS_WORKERS_FOLDER", commonConf.UNDERFS_WORKERS_FOLDER)) {
        System.exit(-1);
      }

      CommonUtils.touch(masterConf.JOURNAL_FOLDER + masterConf.FORMAT_FILE_PREFIX
          + System.currentTimeMillis());
    } else if (args[0].toUpperCase().equals("WORKER")) {
      WorkerConf workerConf = WorkerConf.get();
      String localFolder = workerConf.DATA_FOLDER;
      UnderFileSystem ufs = UnderFileSystem.get(localFolder);
      System.out.println("Removing local data under folder: " + localFolder);
      if (ufs.exists(localFolder)) {
        String[] files = ufs.list(localFolder);
        for (String file : files) {
          ufs.delete(CommonUtils.concat(localFolder, file), true);
        }
      }
    } else {
      System.out.println(USAGE);
      System.exit(-1);
    }
  }
}