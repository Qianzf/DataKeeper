package io.github.qianzf.netty.proxy.test;

import java.lang.management.ManagementFactory;
import java.util.Scanner;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Created by 18041910 on 2020/9/14.
 */
public class Test {

    public static void main(String[] args) {

//        ArrayList<Integer> a = new ArrayList<>();
//        a.ensureCapacity(10000000);
//        for (int i = 0; i < 10000000; i++) a.add(i);

        IntArrayList intList = new IntArrayList();
        intList.ensureCapacity(10000000);
        for (int i = 0; i < 10000000; i++) intList.add(i);

        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);
        // get pid
        String pid = name.split("@")[0];
        System.out.println("Pid is:" + pid);
        System.out.println("执行完成");
        Scanner ms = new Scanner(System.in);
        ms.nextInt();
    }
}
