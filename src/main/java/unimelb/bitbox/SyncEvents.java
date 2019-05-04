package unimelb.bitbox;

import unimelb.bitbox.util.FileSystemManager;

import java.util.ArrayList;
import java.util.TimerTask;

public class SyncEvents extends TimerTask
{
    private ServerMain f;

    public SyncEvents(ServerMain f)
    {
        this.f = f;
    }

    public void run()
    {
        ArrayList<FileSystemManager.FileSystemEvent> sync = f.fileSystemManager.generateSyncEvents();
        FileSystemManager.FileSystemEvent currentEvent = null;
        System.out.println("-----Synchronizing Events-----");
        while (sync.size() > 0)
        {
            currentEvent = sync.remove(0);
            f.processFileSystemEvent(currentEvent);
        }
    }


}
