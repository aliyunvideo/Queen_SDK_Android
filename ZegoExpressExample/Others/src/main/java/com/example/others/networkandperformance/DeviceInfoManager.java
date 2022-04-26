package com.example.others.networkandperformance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * DeviceInfoManager 设备信息管理
 * <p>
 * 主要用于获取系统cpu 内存 唯一标示等一些系统信息
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

    /**
     * 计算已使用内存的百分比，并返回。
     * Calculate and return the percentage of used memory.
     *
     * @param context 可传入应用程序上下文。 Application Context
     * @return 已使用内存的百分比，以字符串形式返回。Percentage of used memory, returned as a string.
     */
    public static String getUsedPercentValue(Context context) {
        long totalMemorySize = getTotalMemory();
        long availableSize = getAvailableMemory(context) / 1024;
        int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
        return percent + "%";
    }

    public static long getUsedValue(Context context){
        long totalMemorySize = getTotalMemory();
        long availableSize = getAvailableMemory(context) / 1024;
        return totalMemorySize - availableSize;
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
    public static long getFreeMem(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        // 单位Byte
        return info.availMem;
    }
    public static String getSystemMemoryRate(Context context){
        return 1-((getFreeMem(context)/1024)/(float)getTotalMemory())+"%";
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
     * 获取当前进程的CPU使用率
     * Get the CPU usage of the current process
     *
     * @return CPU的使用率 CPU usage
     */
    public static float getCurProcessCpuRate() {

        float cpuRate = 0;
            cpuRate = getAppCpuTop();
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

}
