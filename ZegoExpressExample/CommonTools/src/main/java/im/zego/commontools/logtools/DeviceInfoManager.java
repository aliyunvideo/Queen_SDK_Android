package im.zego.commontools.logtools;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


/**
 * DeviceInfoManager 设备信息管理
 * <p>
 * 主要用于获取系统cpu、内存等一些系统信息
 * Mainly used to obtain some system information such as system and cpu.
 */
public class DeviceInfoManager {

    private static ActivityManager activityManager;

    public synchronized static ActivityManager getActivityManager(Context context) {
        if (activityManager == null) {
            activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return activityManager;
    }

    public static int getScreenWidth(Context context) {
        int screenWith = -1;
        try {
            screenWith = context.getResources().getDisplayMetrics().widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenWith;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     * Calculate and return the percentage of used memory.
     *
     * @param context 可传入应用程序上下文。 Application Context
     * @return 已使用内存的百分比，以字符串形式返回。 Percentage of used memory, returned as a string.
     */
    public static String getUsedPercentValue(Context context) {
        long totalMemorySize = getTotalMemory();
        long availableSize = getAvailableMemory(context) / 1024;
        int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
        return percent + "%";
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     * Get the current available memory, and the returned data is measured by byte.
     *
     * @param context 可传入应用程序上下文。Application Context
     * @return 当前可用内存。current available memory
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取系统总内存,返回字节单位为KB
     * Get the total memory of the system, the returned data is measured by KB
     *
     * @return 系统总内存 Total memory of the system.
     */
    public static long getTotalMemory() {
        long totalMemorySize = 0;
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            //将非数字的字符替换为空
            // Replace non-numeric characters with empty
            totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalMemorySize;
    }

    /**
     * 获取顶层activity的包名
     * Get the package name of the top-level activity
     *
     * @param context
     * @return activity 的包名 PackageName of activity
     */
    public static String getTopActivityPackageName(Context context) {
        ActivityManager activityManager = getActivityManager(context);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        return runningTasks.get(0).topActivity.getPackageName();
    }

    /**
     * 获取当前进程的CPU使用率
     * Get the CPU usage of the current process
     *
     * @return CPU的使用率 CPU usage
     */
    public static float getCurProcessCpuRate() {

        float cpuRate = 0;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            try {
                float totalCpuTime1 = getTotalCpuTime();
                float processCpuTime1 = getAppCpuTime();

                Thread.sleep(360);

                float totalCpuTime2 = getTotalCpuTime();
                float processCpuTime2 = getAppCpuTime();
                cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                        / (totalCpuTime2 - totalCpuTime1);

            } catch (Exception e) {

            }
        } else {
            cpuRate = getAppCpuTop();
        }

        return cpuRate;
    }

    /**
     * 通过top 命令获取 cpu 当前使用率
     * Get the current cpu usage rate using the top command
     *
     * @return 当前进程的CPU使用时间 Current CPU Usage Rate
     */

    public static float getAppCpuTop() {

        int pid = android.os.Process.myPid();
        float cpuUsage = 0;
        try {
            @SuppressLint("DefaultLocale") Process pp = Runtime.getRuntime().exec(String.format("top -p %d -n 1", pid));
            pp.waitFor();
            InputStream fis = pp.getInputStream();

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String cpuUsageStr = "0";
            String cpuNumberStr = "0";
            String line = null;
            while ((line = br.readLine()) != null) {
                int cpuIndex = line.indexOf("%cpu");
                int cpuUsageIndex = line.indexOf(String.valueOf(pid));
                if (cpuIndex != -1) {
                    cpuNumberStr = line.substring(0, cpuIndex);
                }

                if (cpuUsageIndex != -1) {
                    String[] cpuInfo = line.split("\\s");
                    cpuUsageStr = checkCpuInfo(cpuInfo);

                }
            }

            cpuUsage = Float.valueOf(cpuUsageStr) / (Float.valueOf(cpuNumberStr) / 100);

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return cpuUsage;
    }

    private static String checkCpuInfo(String[] cpuInfo) {
        int index = 0;
        for (String info : cpuInfo) {
            if (!" ".equals(info) && !"".equals(info)) {
                if (index == 8) {
                    return info;
                }
                index++;
            }
        }
        return "0";
    }


    static long bootTime() {
        return SystemClock.elapsedRealtime() / 1000;
    }

    /**
     * 获取总的CPU使用率
     * Get the total CPU usage
     *
     * @return CPU使用率 Get the total CPU usage
     */
    public static float getTotalCpuRate() {
        float cpuRate = 0;
        try {
            float totalCpuTime1 = getTotalCpuTime();
            float totalUsedCpuTime1 = totalCpuTime1 - sStatus.idletime;

            Thread.sleep(360);

            float totalCpuTime2 = getTotalCpuTime();
            float totalUsedCpuTime2 = totalCpuTime2 - sStatus.idletime;
            cpuRate = 100 * (totalUsedCpuTime2 - totalUsedCpuTime1)
                    / (totalCpuTime2 - totalCpuTime1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cpuRate;
    }

    /**
     * Get the total CPU usage time of the system
     * 获取系统总CPU使用时间
     *
     * @return 系统CPU总的使用时间 Total usage time of system CPU
     */
    public static long getTotalCpuTime() {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {

        }
        sStatus.usertime = Long.parseLong(cpuInfos[2]);
        sStatus.nicetime = Long.parseLong(cpuInfos[3]);
        sStatus.systemtime = Long.parseLong(cpuInfos[4]);
        sStatus.idletime = Long.parseLong(cpuInfos[5]);
        sStatus.iowaittime = Long.parseLong(cpuInfos[6]);
        sStatus.irqtime = Long.parseLong(cpuInfos[7]);
        sStatus.softirqtime = Long.parseLong(cpuInfos[8]);
        return sStatus.getTotalTime();
    }

    /**
     * Get the CPU usage time of the current process
     *
     * @return CPU usage time of the current process
     */
    public static long getAppCpuTime() {
        // 获取应用占用的CPU时间
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    static Status sStatus = new Status();

    static class Status {
        public long usertime;
        public long nicetime;
        public long systemtime;
        public long idletime;
        public long iowaittime;
        public long irqtime;
        public long softirqtime;

        public long getTotalTime() {
            return (usertime + nicetime + systemtime + idletime + iowaittime
                    + irqtime + softirqtime);
        }
    }


}
