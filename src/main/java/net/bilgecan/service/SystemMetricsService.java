package net.bilgecan.service;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.Optional;

@Service
public class SystemMetricsService {

    private final Optional<OperatingSystemMXBean> osBean;

    public SystemMetricsService() {
        // com.sun.management.OperatingSystemMXBean has extra methods (process + physical memory)
        OperatingSystemMXBean bean = null;
        try {
            bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        this.osBean = Optional.ofNullable(bean);
    }

    // ---- RAM in MB ----
    public long getJvmUsedMemoryMb() {
        Runtime rt = Runtime.getRuntime();
        long usedBytes = rt.totalMemory() - rt.freeMemory();
        return bytesToMb(usedBytes);
    }

    public long getJvmMaxMemoryMb() {
        long maxBytes = Runtime.getRuntime().maxMemory();
        return bytesToMb(maxBytes);
    }

    public long getSystemTotalMemoryMb() {
        return osBean.map(b -> bytesToMb(b.getTotalMemorySize())).orElse(-1L);
    }

    public long getSystemFreeMemoryMb() {
        return osBean.map(b -> bytesToMb(b.getFreeMemorySize())).orElse(-1L);
    }

    // ---- CPU ----

    /**
     * @return process CPU load in percent (0..100), or -1 if unavailable
     */
    public double getProcessCpuLoadPercent() {
        return osBean.map(b -> sanitizePercent(b.getProcessCpuLoad() * 100.0)).orElse(-1d);
    }

    /**
     * @return system CPU load in percent (0..100), or -1 if unavailable
     */
    public double getSystemCpuLoadPercent() {
        return osBean.map(b -> sanitizePercent(b.getCpuLoad() * 100.0)).orElse(-1d);
    }

    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private static long bytesToMb(long bytes) {
        // compute MB = bytes / 1024 / 1024 (integer division)
        long kb = bytes / 1024L;
        return kb / 1024L;
    }

    private static double sanitizePercent(double v) {
        if (Double.isNaN(v) || v < 0.0) return -1d;
        if (v > 100.0) return 100.0;
        return Math.round(v * 10.0) / 10.0; // keep one decimal place
    }
}
