import java.util.*;


class Process {
    String pid;
    int arrivalTime;
    int burstTime;
    int priority;

    int completionTime;
    int turnaroundTime;
    int waitingTime;
    int remainingTime; 

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    public void reset() {
        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
        this.remainingTime = burstTime;
    }

    @Override
    public String toString() {
        return pid;
    }
}
class GanttEntry {
    String pid;
    int start;
    int end;

    GanttEntry(String pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }
}


class Scheduler {

    //First Come First Serve

    public static List<GanttEntry> fcfs(List<Process> processes) {
       
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                              .thenComparing(p -> p.pid));

        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;

        for (Process p : sorted) {
           
            if (currentTime < p.arrivalTime) {
                gantt.add(new GanttEntry("IDLE", currentTime, p.arrivalTime));
                currentTime = p.arrivalTime;
            }
            int start = currentTime;
            currentTime += p.burstTime;
            p.completionTime = currentTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime    = p.turnaroundTime - p.burstTime;
            gantt.add(new GanttEntry(p.pid, start, currentTime));
        }
        return gantt;
    }

    //Shortest Job First

    public static List<GanttEntry> sjf(List<Process> processes) {
        List<Process> remaining = new ArrayList<>(processes);
        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;
        int completed   = 0;
        int n           = processes.size();

        while (completed < n) {
            List<Process> available = new ArrayList<>();
            for (Process p : remaining) {
                if (p.arrivalTime <= currentTime) available.add(p);
            }

            if (available.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : remaining) nextArrival = Math.min(nextArrival, p.arrivalTime);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            available.sort(Comparator.comparingInt((Process p) -> p.burstTime)
                                     .thenComparingInt(p -> p.arrivalTime)
                                     .thenComparing(p -> p.pid));

            Process chosen = available.get(0);
            int start = currentTime;
            currentTime += chosen.burstTime;
            chosen.completionTime = currentTime;
            chosen.turnaroundTime = chosen.completionTime - chosen.arrivalTime;
            chosen.waitingTime    = chosen.turnaroundTime - chosen.burstTime;
            gantt.add(new GanttEntry(chosen.pid, start, currentTime));
            remaining.remove(chosen);
            completed++;
        }
        return gantt;
    }

   //Round Robin

    public static List<GanttEntry> roundRobin(List<Process> processes, int quantum) {
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                              .thenComparing(p -> p.pid));

        Queue<Process> readyQueue = new LinkedList<>();
        List<GanttEntry> gantt   = new ArrayList<>();
        int currentTime  = 0;
        int idx          = 0;    
        int completed    = 0;
        int n            = sorted.size();

        while (idx < n && sorted.get(idx).arrivalTime <= currentTime) {
            readyQueue.add(sorted.get(idx++));
        }

        while (completed < n) {
            if (readyQueue.isEmpty()) {
                int nextArrival = sorted.get(idx).arrivalTime;
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                while (idx < n && sorted.get(idx).arrivalTime <= currentTime) {
                    readyQueue.add(sorted.get(idx++));
                }
                continue;
            }

            Process current = readyQueue.poll();
            int runTime = Math.min(quantum, current.remainingTime);
            int start   = currentTime;
            currentTime += runTime;
            current.remainingTime -= runTime;
            gantt.add(new GanttEntry(current.pid, start, currentTime));

            while (idx < n && sorted.get(idx).arrivalTime <= currentTime) {
                readyQueue.add(sorted.get(idx++));
            }

            if (current.remainingTime == 0) {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime    = current.turnaroundTime - current.burstTime;
                completed++;
            } else {
                readyQueue.add(current); 
            }
        }
        return gantt;
    }
}
public class CPUSchedulingSimulator {
 
    public static void main(String[] args) {
        // Sample processes for testing
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P0", 0, 4, 2));
        processes.add(new Process("P1", 2, 3, 1));
        processes.add(new Process("P2", 4, 2, 3));
 
        // FCFS
        List<GanttEntry> gantt1 = Scheduler.fcfs(processes);
        printGantt("First-Come, First-Served (FCFS)", gantt1);
 
        // SJF
        for (Process p : processes) p.reset();
        List<GanttEntry> gantt2 = Scheduler.sjf(processes);
        printGantt("Shortest Job First (SJF)", gantt2);
 
        // Round Robin
        for (Process p : processes) p.reset();
        List<GanttEntry> gantt3 = Scheduler.roundRobin(processes, 2);
        printGantt("Round Robin (RR) - Quantum = 2", gantt3);
    }
 
   // Print sa Gantt chart
 
    static void printGantt(String title, List<GanttEntry> gantt) {
        System.out.println("\nAlgorithm: " + title);
        System.out.println();
 
        // Execution Order
        System.out.println("Execution Order:");
        StringBuilder order = new StringBuilder();
        boolean first = true;
        for (GanttEntry e : gantt) {
            if (e.pid.equals("IDLE")) continue;
            if (!first) order.append(" -> ");
            order.append(e.pid);
            first = false;
        }
        System.out.println(order);
 
        // Gantt Chart Row: | P0 | P1 | P2 |
        StringBuilder ganttRow = new StringBuilder();
        for (GanttEntry e : gantt) {
            ganttRow.append("| ").append(e.pid).append(" ");
        }
        ganttRow.append("|");
        System.out.println(ganttRow);
 
        // Time Markers below each cell
        StringBuilder timeRow = new StringBuilder();
        for (GanttEntry e : gantt) {
            String t         = String.valueOf(e.start);
            int    cellWidth = e.pid.length() + 3;
            timeRow.append(t);
            int pad = cellWidth - t.length();
            if (pad > 0) timeRow.append(" ".repeat(pad));
        }
        timeRow.append(gantt.get(gantt.size() - 1).end);
        System.out.println(timeRow);
        System.out.println();
    }
}
