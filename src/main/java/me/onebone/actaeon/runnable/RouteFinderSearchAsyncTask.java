package me.onebone.actaeon.runnable;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;
import me.onebone.actaeon.route.RouteFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * RouteFinderSearchAsyncTask
 * ===============
 * author: boybook
 * EaseCation Network Project
 * nukkit
 * ===============
 */
public class RouteFinderSearchAsyncTask extends AsyncTask {

    private long startTime;
    private RouteFinder route;
    private int retryTimes = 0;
    private Level level;
    private Vector3 start;
    private Vector3 dest;
    private AxisAlignedBB bb;

    private static Map<RouteFinder, RouteFinderSearchAsyncTask> taskMap = new HashMap<>();

    public static String dumpRouteFinderTask() {
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(taskMap.size()).append(" 个寻路任务进行中");
        taskMap.forEach(((finder, task) -> {
            sb.append("\n");
            sb.append("[").append(finder.getEntity().getNameTag()).append("] ").append(finder.getStart()).append(" => ").append(finder.getDestination());
            long usedTime = System.currentTimeMillis() - task.startTime;
            sb.append("  retryTimes=").append(task.retryTimes).append(" usedTime=").append(usedTime).append("ms");
        }));
        return sb.toString();
    }

    public static int getTaskSize() {
        return taskMap.size();
    }

    public RouteFinderSearchAsyncTask(RouteFinder route) {
        this(route, null, null, null, null);
    }

    public RouteFinderSearchAsyncTask(RouteFinder route, Level level, Vector3 start, Vector3 dest, AxisAlignedBB bb) {
        this.startTime = System.currentTimeMillis();
        this.route = route;
        this.level = level;
        this.start = start;
        this.dest = dest;
        this.bb = bb;
        if (taskMap.containsKey(route)) {  //在此之前已有一个本寻路对象的寻路计划
            taskMap.get(route).cancel();
        }
        taskMap.put(route, this);
    }

    /*
     * 如果正在寻路，无影响，如果处于等待寻路中，自动取消寻路计划。
     */
    public void cancel() {
        this.retryTimes = 1000;
    }

    @Override
    public void onRun() {
        try {
            while (this.retryTimes < 100) {
                if (!this.route.isSearching()) {
                    if (this.level != null) this.route.setPositions(this.level, this.start, this.dest, this.bb);
                    this.route.search();
                    //Server.getInstance().getLogger().notice("异步寻路线程-" + this.getTaskId() + " 开始寻路");
                    return;
                } else {
                    this.retryTimes++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            }
            if (this.retryTimes < 1000) {
                Server.getInstance().getLogger().warning("异步寻路线程-" + this.getTaskId() + " 超过等待限制");
                this.route.forceStop();
            } else {
                //Server.getInstance().getLogger().warning(TextFormat.LIGHT_PURPLE + "异步寻路线程-" + this.getTaskId() + " 取消寻路计划");
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().logException(e);
            if (taskMap.containsKey(this.route) && taskMap.get(this.route) == this) {
                taskMap.remove(this.route);
            }
        }
    }

    @Override
    public void onCompletion(Server server) {
        if (taskMap.containsKey(this.route) && taskMap.get(this.route) == this) {
            taskMap.remove(this.route);
        }
    }
}
