package com.example.mywatchdog.demo;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HashedWheelTimerTest {

    /**
     *  HashedWheelTimer 是来自于 Netty 的工具类，在 netty-common 包中。它用于实现延时任务  异步的
     */

    // 构造一个 Timer 实例
    static Timer timer = new HashedWheelTimer();

    public static void main(String[] args) throws IOException {


        // 提交一个任务，让它在 5s 后执行
        Timeout timeout1 = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                System.out.println("5s 后执行该任务");
            }
        }, 5, TimeUnit.SECONDS);

        // 再提交一个任务，让它在 10s 后执行
        Timeout timeout2 = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                System.out.println("10s 后执行该任务");
            }
        }, 10, TimeUnit.SECONDS);

        // System.in.read();

    }

}
